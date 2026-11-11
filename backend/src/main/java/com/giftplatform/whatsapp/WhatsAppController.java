package com.giftplatform.whatsapp;

import com.giftplatform.cohost.EventAccess;
import com.giftplatform.event.entity.Event;
import com.giftplatform.security.ActorContext;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.*;

/** Host-only WhatsApp messaging for one event: guest list, previews, sending and replies. */
@RestController
@RequestMapping("/api/v1/events/{id}/whatsapp")
public class WhatsAppController {
    public static final String MODULE = "WHATSAPP_MESSAGING";

    private final JdbcTemplate db;
    private final EventAccess access;
    private final ActorContext actor;
    private final WhatsAppService wa;

    public WhatsAppController(JdbcTemplate db, EventAccess access, ActorContext actor, WhatsAppService wa) {
        this.db = db; this.access = access; this.actor = actor; this.wa = wa;
    }

    public record Contact(UUID id, String name, String phone, boolean optedOut, String replyAttendance, Integer replyPartySize,
                          OffsetDateTime repliedAt, String lastKind, String lastStatus, OffsetDateTime lastAt) {}
    public record Stats(int contacts, int optedOut, int yes, int maybe, int no, int noReply, int guestsComing, int sent, int delivered, int read, int failed, int queued) {}
    public record Overview(String provider, boolean live, boolean published, List<Contact> contacts, Stats stats, Map<String, String> previews) {}
    public record ImportInput(String text) {}
    public record SendInput(String kind, String audience, List<UUID> contactIds) {}
    public record DevReply(UUID contactId, String text) {}

    @GetMapping
    public ResponseEntity<Overview> overview(@PathVariable UUID id) {
        Event e = access.requireOwner(id, MODULE);
        List<Contact> contacts = db.query("""
            SELECT c.*, m.kind AS last_kind, m.status AS last_status, m.created_at AS last_at FROM event_guest_contacts c
            LEFT JOIN LATERAL (SELECT kind,status,created_at FROM whatsapp_messages WHERE contact_id=c.id ORDER BY created_at DESC LIMIT 1) m ON TRUE
            WHERE c.event_id=? ORDER BY c.name""", (rs, i) -> new Contact(rs.getObject("id", UUID.class), rs.getString("name"), rs.getString("phone"),
                rs.getObject("opted_out_at") != null, rs.getString("reply_attendance"), (Integer) rs.getObject("reply_party_size"),
                rs.getObject("replied_at", OffsetDateTime.class), rs.getString("last_kind"), rs.getString("last_status"), rs.getObject("last_at", OffsetDateTime.class)), id);
        Map<String, Integer> byStatus = new HashMap<>();
        db.query("SELECT status,count(*) FROM whatsapp_messages WHERE event_id=? GROUP BY status", rs -> { byStatus.put(rs.getString(1), rs.getInt(2)); }, id);
        int yes = 0, maybe = 0, no = 0, none = 0, coming = 0, opted = 0;
        for (Contact c : contacts) {
            if (c.optedOut()) opted++;
            if (c.replyAttendance() == null) none++;
            else switch (c.replyAttendance()) {
                case "YES" -> { yes++; coming += c.replyPartySize() == null ? 1 : c.replyPartySize(); }
                case "MAYBE" -> maybe++;
                default -> no++;
            }
        }
        Stats stats = new Stats(contacts.size(), opted, yes, maybe, no, none, coming, byStatus.getOrDefault("SENT", 0),
            byStatus.getOrDefault("DELIVERED", 0), byStatus.getOrDefault("READ", 0), byStatus.getOrDefault("FAILED", 0),
            byStatus.getOrDefault("QUEUED", 0) + byStatus.getOrDefault("SENDING", 0));
        Map<String, String> previews = new LinkedHashMap<>();
        for (String kind : List.of("INVITATION", "REMINDER", "THANK_YOU")) previews.put(kind, WhatsAppService.body(kind, wa.params(kind, e, "Asha")));
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new Overview(wa.gateway().name(), wa.gateway().live(),
            e.getStatus() == com.giftplatform.event.entity.EventStatus.PUBLISHED, contacts, stats, previews));
    }

    @PostMapping("/contacts/import")
    public WhatsAppService.ImportResult importContacts(@PathVariable UUID id, @RequestBody ImportInput in) {
        access.requireOwner(id, MODULE);
        return wa.importContacts(id, in == null ? null : in.text());
    }

    @PostMapping("/contacts/{contactId}/delete")
    public Map<String, Object> delete(@PathVariable UUID id, @PathVariable UUID contactId) {
        access.requireOwner(id, MODULE);
        if (db.update("DELETE FROM event_guest_contacts WHERE id=? AND event_id=?", contactId, id) == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return Map.of("deleted", true);
    }

    @PostMapping("/send")
    public WhatsAppService.QueueResult send(@PathVariable UUID id, @RequestBody SendInput in) {
        Event e = access.requireOwner(id, MODULE);
        if (in == null) throw WhatsAppService.bad("Choose a message and who should receive it");
        return wa.queue(e, in.kind() == null ? "" : in.kind().toUpperCase(Locale.ROOT), in.audience(), in.contactIds(), actor.requireAuthenticatedUserId());
    }

    /** Practice mode only: simulate a guest's reply so hosts can see how RSVPs arrive. */
    @PostMapping("/dev-reply")
    public Map<String, Object> devReply(@PathVariable UUID id, @RequestBody DevReply in) {
        access.requireOwner(id, MODULE);
        if (wa.gateway().live()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        if (in == null || in.contactId() == null) throw WhatsAppService.bad("Choose a guest");
        var phone = db.query("SELECT phone FROM event_guest_contacts WHERE id=? AND event_id=?", (rs, i) -> rs.getString(1), in.contactId(), id);
        if (phone.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        String result = wa.inbound("dev-in-" + UUID.randomUUID(), phone.getFirst().substring(1), in.text());
        return Map.of("result", result);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> reason(ResponseStatusException ex) {
        int code = ex.getStatusCode().value();
        if ((code == 400 || code == 409) && ex.getReason() != null) return ResponseEntity.status(code).body(Map.of("error", ex.getReason()));
        return ResponseEntity.status(ex.getStatusCode()).build();
    }
}
