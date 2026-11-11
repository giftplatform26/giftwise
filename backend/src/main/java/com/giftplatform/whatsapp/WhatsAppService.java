package com.giftplatform.whatsapp;

import com.giftplatform.event.entity.Event;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Guest contacts, message queue, delivery worker and reply handling for WhatsApp messaging. */
@Service
@EnableScheduling
public class WhatsAppService {
    public static final Set<String> KINDS = Set.of("INVITATION", "REMINDER", "THANK_YOU");
    public static final int MAX_CONTACTS = 2000;
    private static final Pattern REPLY = Pattern.compile(
        "^(YES|Y|HAAN|HAN|HA|ATTENDING|COMING|NO|N|NAHI|NAHIN|NOT COMING|MAYBE|MAY BE|PERHAPS|SHAYAD)\\b\\W*(\\d{1,2})?.*$");

    private final JdbcTemplate db;
    private final WhatsAppGateway gateway;
    private final String guestBaseUrl, defaultCountry;
    private final int batchSize;

    public WhatsAppService(JdbcTemplate db, WhatsAppGateway gateway,
                           @Value("${gift.guest-base-url:http://localhost:3000}") String guestBaseUrl,
                           @Value("${gift.whatsapp.default-country-code:91}") String defaultCountry,
                           @Value("${gift.whatsapp.batch-size:20}") int batchSize) {
        this.db = db; this.gateway = gateway; this.guestBaseUrl = guestBaseUrl.replaceAll("/+$", "");
        this.defaultCountry = defaultCountry; this.batchSize = Math.max(1, Math.min(batchSize, 200));
    }

    public WhatsAppGateway gateway() { return gateway; }

    // ---- Phones and contacts ----

    /** "98765 43210", "098765-43210", "+91 98765 43210" → "+919876543210"; null when not a valid mobile number. */
    public String phone(String raw) {
        if (raw == null) return null;
        String v = raw.strip();
        boolean plus = v.startsWith("+") || v.startsWith("00");
        String digits = v.replaceAll("[^0-9]", "");
        if (v.startsWith("00")) digits = digits.substring(2);
        else if (!plus) {
            digits = digits.replaceFirst("^0+", "");
            if (digits.length() == 10) digits = defaultCountry + digits;
        }
        return digits.matches("[1-9][0-9]{7,14}") ? "+" + digits : null;
    }

    public record ImportResult(int added, int updated, List<String> skipped) {}

    /** One guest per line: "Name, phone" (comma, tab or semicolon). Opt-outs on the same phone are respected. */
    @Transactional
    public ImportResult importContacts(UUID eventId, String text) {
        if (text == null || text.isBlank()) throw bad("Paste at least one line like: Asha Rao, 98765 43210");
        String[] lines = text.split("\\R");
        if (lines.length > 1000) throw bad("Import up to 1,000 lines at a time");
        int added = 0, updated = 0;
        List<String> skipped = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].strip();
            if (line.isEmpty()) continue;
            String[] parts = line.split("\\s*[,;\\t]\\s*");
            String name = parts[0].strip(), phone = parts.length > 1 ? phone(parts[parts.length - 1]) : null;
            if (parts.length < 2 || name.isEmpty() || name.length() > 120 || name.chars().anyMatch(Character::isISOControl)) { skipped.add("Line " + (i + 1) + ": add a name and a phone number"); continue; }
            if (phone == null) { skipped.add("Line " + (i + 1) + ": “" + parts[parts.length - 1] + "” is not a valid mobile number"); continue; }
            int n = db.update("UPDATE event_guest_contacts SET name=? WHERE event_id=? AND phone=?", name, eventId, phone);
            if (n > 0) { updated++; continue; }
            Integer count = db.queryForObject("SELECT count(*) FROM event_guest_contacts WHERE event_id=?", Integer.class, eventId);
            if (count != null && count >= MAX_CONTACTS) { skipped.add("Line " + (i + 1) + ": guest list is full (" + MAX_CONTACTS + ")"); continue; }
            db.update("""
                INSERT INTO event_guest_contacts(id,event_id,name,phone,opted_out_at)
                VALUES(?,?,?,?,(SELECT max(opted_out_at) FROM event_guest_contacts WHERE phone=?))""", UUID.randomUUID(), eventId, name, phone, phone);
            added++;
        }
        return new ImportResult(added, updated, skipped);
    }

    // ---- Composing ----

    public String invitationUrl(Event e) { return guestBaseUrl + "/e/" + e.getPublicToken(); }

    /** Template parameters, in order, for each kind. Must match the approved WhatsApp templates. */
    public List<String> params(String kind, Event e, String guestName) {
        String host = e.getHostDisplayName() == null || e.getHostDisplayName().isBlank() ? "Your host" : e.getHostDisplayName();
        String date = e.getEventDate() == null ? "soon" : e.getEventDate().format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH));
        return switch (kind) {
            case "INVITATION" -> List.of(guestName, host, e.getTitle(), invitationUrl(e));
            case "REMINDER" -> List.of(guestName, e.getTitle(), date, invitationUrl(e));
            case "THANK_YOU" -> List.of(guestName, e.getTitle(), host);
            default -> throw bad("Choose a message type");
        };
    }

    /** What the guest will read; shown to the host before sending and stored with each message. */
    public static String body(String kind, List<String> p) {
        return switch (kind) {
            case "INVITATION" -> "Hi " + p.get(0) + "! " + p.get(1) + " invites you to " + p.get(2) + ". See the invitation: " + p.get(3)
                + "\n\nReply YES, NO or MAYBE (add how many are coming, e.g. YES 3). Reply STOP to stop messages.";
            case "REMINDER" -> "Hi " + p.get(0) + ", a reminder that " + p.get(1) + " is on " + p.get(2) + ". Details: " + p.get(3)
                + "\n\nReply YES, NO or MAYBE if you have not told us yet. Reply STOP to stop messages.";
            case "THANK_YOU" -> "Dear " + p.get(0) + ", thank you for celebrating " + p.get(1) + " with us and for your love and blessings. — " + p.get(2);
            default -> "";
        };
    }

    // ---- Queueing ----

    public record QueueResult(int queued, int skippedOptedOut, int skippedAlreadyToday) {}

    @Transactional
    public QueueResult queue(Event e, String kind, String audience, List<UUID> contactIds, UUID who) {
        if (!KINDS.contains(kind)) throw bad("Choose a message type");
        if (!kind.equals("THANK_YOU") && e.getStatus() != com.giftplatform.event.entity.EventStatus.PUBLISHED)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Publish your invitation before sending it on WhatsApp");
        String filter = switch (audience == null ? "" : audience) {
            case "ALL" -> "";
            case "NO_REPLY" -> " AND reply_attendance IS NULL";
            case "ATTENDING" -> " AND reply_attendance='YES'";
            case "SELECTED" -> {
                if (contactIds == null || contactIds.isEmpty() || contactIds.size() > MAX_CONTACTS) throw bad("Select at least one guest");
                yield " AND id = ANY(?)";
            }
            default -> throw bad("Choose who should receive the message");
        };
        Object[] args = audience.equals("SELECTED") ? new Object[]{e.getId(), contactIds.toArray(UUID[]::new)} : new Object[]{e.getId()};
        var contacts = db.queryForList("SELECT id,name,opted_out_at FROM event_guest_contacts WHERE event_id=?" + filter, args);
        if (contacts.isEmpty()) throw bad("No guests match. Add guests or choose a different group.");
        int queued = 0, optedOut = 0, already = 0;
        for (var c : contacts) {
            if (c.get("opted_out_at") != null) { optedOut++; continue; }
            List<String> p = params(kind, e, (String) c.get("name"));
            int n = db.update("""
                INSERT INTO whatsapp_messages(id,event_id,contact_id,kind,body,template_params,provider,queued_by)
                VALUES(?,?,?,?,?,?,?,?) ON CONFLICT DO NOTHING""",
                UUID.randomUUID(), e.getId(), c.get("id"), kind, body(kind, p), String.join("\u001F", p), gateway.name(), who);
            if (n == 0) already++; else queued++;
        }
        return new QueueResult(queued, optedOut, already);
    }

    // ---- Delivery worker ----

    @Scheduled(fixedDelayString = "${gift.whatsapp.worker-delay-ms:10000}", initialDelayString = "${gift.whatsapp.worker-initial-delay-ms:20000}")
    public void deliver() { deliverBatch(); }

    /** Claims and sends up to one batch; safe across several app instances. Returns how many were attempted. */
    public int deliverBatch() {
        db.update("UPDATE whatsapp_messages SET status='FAILED',error_code='GAVE_UP',updated_at=now() WHERE status IN ('QUEUED','SENDING') AND (attempts>=5 OR created_at<now()-interval '24 hours')");
        int done = 0;
        for (; done < batchSize; done++) {
            var rows = db.queryForList("""
                UPDATE whatsapp_messages SET status='SENDING',attempts=attempts+1,next_attempt_at=now()+interval '5 minutes',updated_at=now()
                WHERE id=(SELECT id FROM whatsapp_messages WHERE status IN ('QUEUED','SENDING') AND next_attempt_at<=now()
                          ORDER BY created_at FOR UPDATE SKIP LOCKED LIMIT 1)
                RETURNING id,contact_id,kind,template_params,attempts""");
            if (rows.isEmpty()) break;
            var m = rows.getFirst();
            UUID id = (UUID) m.get("id");
            var contact = db.queryForList("SELECT phone,opted_out_at FROM event_guest_contacts WHERE id=?", m.get("contact_id"));
            if (contact.isEmpty() || contact.getFirst().get("opted_out_at") != null) {
                db.update("UPDATE whatsapp_messages SET status='FAILED',error_code='OPTED_OUT',updated_at=now() WHERE id=?", id);
                continue;
            }
            List<String> params = List.of(((String) m.get("template_params")).split("\u001F", -1));
            WhatsAppGateway.Result r = gateway.send((String) contact.getFirst().get("phone"), (String) m.get("kind"), params);
            if (r.sent()) db.update("UPDATE whatsapp_messages SET status='SENT',provider_message_id=?,sent_at=now(),error_code=NULL,updated_at=now() WHERE id=?", r.providerMessageId(), id);
            else if (r.retryable()) db.update("UPDATE whatsapp_messages SET status='QUEUED',error_code=?,next_attempt_at=now()+(interval '1 minute' * power(2,attempts)),updated_at=now() WHERE id=?", r.errorCode(), id);
            else db.update("UPDATE whatsapp_messages SET status='FAILED',error_code=?,updated_at=now() WHERE id=?", r.errorCode(), id);
        }
        return done;
    }

    // ---- Replies and statuses ----

    public record Reply(String attendance, Integer partySize, boolean stop) {}

    /** "Yes 3", "haan", "NO", "maybe 2", "STOP". Returns null for anything else. */
    public static Reply parse(String text) {
        if (text == null) return null;
        String t = text.strip().toUpperCase(Locale.ROOT).replaceAll("[!.]+$", "");
        if (t.matches("STOP|UNSUBSCRIBE|STOP ALL|OPT OUT")) return new Reply(null, null, true);
        if (t.startsWith("हाँ") || t.startsWith("हां")) return new Reply("YES", digits(t), false);
        if (t.startsWith("नहीं")) return new Reply("NO", 0, false);
        Matcher m = REPLY.matcher(t);
        if (!m.matches()) return null;
        String w = m.group(1);
        String att = w.matches("NO|N|NAHI|NAHIN|NOT COMING") ? "NO" : w.matches("MAYBE|MAY BE|PERHAPS|SHAYAD") ? "MAYBE" : "YES";
        Integer party = att.equals("NO") ? Integer.valueOf(0) : m.group(2) == null ? null : Integer.valueOf(Math.min(10, Math.max(1, Integer.parseInt(m.group(2)))));
        return new Reply(att, party, false);
    }
    private static Integer digits(String t) {
        Matcher m = Pattern.compile("(\\d{1,2})").matcher(t);
        return m.find() ? Math.min(10, Math.max(1, Integer.parseInt(m.group(1)))) : null;
    }

    /**
     * Applies an inbound reply. STOP opts the number out everywhere. RSVP answers go to the guest entry that most
     * recently received an invitation or reminder from this number (within 60 days). Idempotent per provider message id.
     */
    @Transactional
    public String inbound(String providerMessageId, String fromDigits, String text) {
        String phone = phone("+" + (fromDigits == null ? "" : fromDigits.replaceAll("[^0-9]", "")));
        if (phone == null || providerMessageId == null || providerMessageId.isBlank() || providerMessageId.length() > 128) return "IGNORED";
        int fresh = db.update("INSERT INTO whatsapp_inbound(id,provider_message_id,phone,text) VALUES(?,?,?,?) ON CONFLICT (provider_message_id) DO NOTHING",
            UUID.randomUUID(), providerMessageId, phone, text == null ? null : text.substring(0, Math.min(500, text.length())));
        if (fresh == 0) return "DUPLICATE";
        Reply r = parse(text);
        if (r == null) return "UNRECOGNISED";
        if (r.stop()) {
            int n = db.update("UPDATE event_guest_contacts SET opted_out_at=now() WHERE phone=? AND opted_out_at IS NULL", phone);
            db.update("UPDATE whatsapp_inbound SET matched_contacts=? WHERE provider_message_id=?", n, providerMessageId);
            return "OPTED_OUT";
        }
        var target = db.query("""
            SELECT c.id FROM event_guest_contacts c JOIN whatsapp_messages m ON m.contact_id=c.id
            WHERE c.phone=? AND m.kind IN ('INVITATION','REMINDER') AND m.status IN ('SENT','DELIVERED','READ') AND m.sent_at>now()-interval '60 days'
            ORDER BY m.sent_at DESC LIMIT 1""", (rs, i) -> rs.getObject(1, UUID.class), phone);
        if (target.isEmpty()) return "NO_INVITATION";
        recordReply(target.getFirst(), r);
        db.update("UPDATE whatsapp_inbound SET matched_contacts=1 WHERE provider_message_id=?", providerMessageId);
        return "RSVP_" + r.attendance();
    }

    public void recordReply(UUID contactId, Reply r) {
        int party = r.attendance().equals("NO") ? 0 : r.partySize() == null ? 1 : r.partySize();
        db.update("UPDATE event_guest_contacts SET reply_attendance=?,reply_party_size=?,replied_at=now() WHERE id=?", r.attendance(), party, contactId);
    }

    /** Delivery receipts: only ever move forward (sent → delivered → read), or to failed. */
    public void status(String providerMessageId, String status) {
        String s = status == null ? "" : status.toUpperCase(Locale.ROOT);
        switch (s) {
            case "DELIVERED" -> db.update("UPDATE whatsapp_messages SET status='DELIVERED',updated_at=now() WHERE provider_message_id=? AND status IN ('SENT','SENDING')", providerMessageId);
            case "READ" -> db.update("UPDATE whatsapp_messages SET status='READ',updated_at=now() WHERE provider_message_id=? AND status IN ('SENT','SENDING','DELIVERED')", providerMessageId);
            case "FAILED" -> db.update("UPDATE whatsapp_messages SET status='FAILED',error_code='PROVIDER_FAILED',updated_at=now() WHERE provider_message_id=? AND status<>'READ'", providerMessageId);
            default -> { }
        }
    }

    static ResponseStatusException bad(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, m); }
}
