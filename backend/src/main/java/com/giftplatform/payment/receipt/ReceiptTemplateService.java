package com.giftplatform.payment.receipt;

import com.giftplatform.admin.GrowthSettingsService;
import com.giftplatform.branding.SafeBrandImage;
import com.giftplatform.security.ActorContext;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

/**
 * Host-designed guest receipts, per event, behind the platform switch {@code CUSTOM_RECEIPTS}.
 * Every guest payment report gets a receipt number unique within its event (e.g. ASHA30-000017),
 * assigned once and never changed, whether or not a custom design is enabled.
 */
@Service
public class ReceiptTemplateService {
    public static final String SWITCH = "CUSTOM_RECEIPTS";
    public static final Set<String> LAYOUTS = Set.of("CLASSIC", "ELEGANT", "FESTIVE", "MINIMAL");
    public static final String DEFAULT_PREFIX = "GIFT";

    private final JdbcTemplate db;
    private final ActorContext actors;
    private final GrowthSettingsService growth;

    public ReceiptTemplateService(JdbcTemplate db, ActorContext actors, GrowthSettingsService growth) {
        this.db = db; this.actors = actors; this.growth = growth;
    }

    public record Template(boolean platformEnabled, boolean enabled, String layout, String accentColor, String title,
                           String headerText, String headerSubtext, String thankYouMessage, String footerText, String signatureName,
                           String referencePrefix, boolean showGuestName, boolean showGiftName, boolean showEventDate,
                           boolean showPaymentMethod, boolean showPaymentCode, String logoDataUrl, long version, String nextReceiptNumber) {}
    public record Input(boolean enabled, String layout, String accentColor, String title, String headerText, String headerSubtext,
                        String thankYouMessage, String footerText, String signatureName, String referencePrefix,
                        boolean showGuestName, boolean showGiftName, boolean showEventDate, boolean showPaymentMethod,
                        boolean showPaymentCode, long version) {}

    public boolean platformEnabled() { return growth.enabled(SWITCH); }

    @Transactional(readOnly = true)
    public Template get(UUID eventId) {
        Map<String, Object> event = owned(eventId);
        return read(eventId, event, true);
    }

    @Transactional
    public Template save(UUID eventId, Input in) {
        Map<String, Object> event = owned(eventId);
        requirePlatform();
        if (in == null) throw bad("Receipt details are required");
        String layout = in.layout() == null ? "" : in.layout().strip().toUpperCase(Locale.ROOT);
        if (!LAYOUTS.contains(layout)) throw bad("Choose a receipt layout");
        String color = in.accentColor() == null ? "" : in.accentColor().strip();
        if (!color.matches("#[0-9A-Fa-f]{6}")) throw bad("Choose a colour like #6336B5");
        String prefix = in.referencePrefix() == null ? "" : in.referencePrefix().strip().toUpperCase(Locale.ROOT);
        if (!prefix.matches("[A-Z0-9]{2,12}")) throw bad("Receipt number prefix must be 2–12 letters or numbers");
        String title = text(in.title(), 60, "Receipt title");
        if (title == null) throw bad("Add a receipt title");
        Object[] values = {in.enabled(), layout, color.toUpperCase(Locale.ROOT), title, text(in.headerText(), 120, "Header"),
            text(in.headerSubtext(), 200, "Header subtitle"), text(in.thankYouMessage(), 400, "Thank-you message"),
            text(in.footerText(), 400, "Footer"), text(in.signatureName(), 80, "Signature"), prefix,
            in.showGuestName(), in.showGiftName(), in.showEventDate(), in.showPaymentMethod(), in.showPaymentCode(),
            actors.requireAuthenticatedUserId()};
        Long current = db.query("SELECT version FROM event_receipt_templates WHERE event_id=? FOR UPDATE",
            (rs, i) -> rs.getLong(1), eventId).stream().findFirst().orElse(null);
        if (current == null) {
            if (in.version() != 0) throw conflict();
            Object[] row = new Object[values.length + 1]; row[0] = eventId; System.arraycopy(values, 0, row, 1, values.length);
            db.update("""
                INSERT INTO event_receipt_templates(event_id,enabled,layout,accent_color,title,header_text,header_subtext,thank_you_message,
                  footer_text,signature_name,reference_prefix,show_guest_name,show_gift_name,show_event_date,show_payment_method,show_payment_code,
                  updated_by,version) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,1)""", row);
        } else {
            if (current != in.version()) throw conflict();
            Object[] row = Arrays.copyOf(values, values.length + 1); row[values.length] = eventId;
            db.update("""
                UPDATE event_receipt_templates SET enabled=?,layout=?,accent_color=?,title=?,header_text=?,header_subtext=?,thank_you_message=?,
                  footer_text=?,signature_name=?,reference_prefix=?,show_guest_name=?,show_gift_name=?,show_event_date=?,show_payment_method=?,
                  show_payment_code=?,updated_by=?,version=version+1,updated_at=now() WHERE event_id=?""", row);
        }
        return read(eventId, event, true);
    }

    @Transactional
    public Template uploadLogo(UUID eventId, byte[] bytes) {
        Map<String, Object> event = owned(eventId);
        requirePlatform();
        byte[] png;
        try { png = SafeBrandImage.png(bytes, 320); } catch (IllegalArgumentException e) { throw bad(e.getMessage()); }
        if (png.length > 400_000) throw bad("This logo is too detailed. Try a simpler PNG or JPEG.");
        UUID host = actors.requireAuthenticatedUserId();
        db.update("""
            INSERT INTO event_receipt_templates(event_id,logo_png,updated_by,version) VALUES(?,?,?,1)
            ON CONFLICT(event_id) DO UPDATE SET logo_png=EXCLUDED.logo_png,updated_by=EXCLUDED.updated_by,version=event_receipt_templates.version+1,updated_at=now()""",
            eventId, png, host);
        return read(eventId, event, true);
    }

    @Transactional
    public Template removeLogo(UUID eventId) {
        Map<String, Object> event = owned(eventId);
        db.update("UPDATE event_receipt_templates SET logo_png=NULL,version=version+1,updated_at=now() WHERE event_id=?", eventId);
        return read(eventId, event, true);
    }

    /** Next number for a new guest payment report in this event. Call inside the claim's transaction. */
    public String assignNumber(UUID eventId) {
        Long n = db.queryForObject("""
            INSERT INTO event_receipt_counters(event_id,last_number) VALUES(?,1)
            ON CONFLICT(event_id) DO UPDATE SET last_number=event_receipt_counters.last_number+1 RETURNING last_number""", Long.class, eventId);
        return prefix(eventId) + "-" + String.format("%06d", n);
    }

    /** Design to apply on the public receipt, only when the platform switch and the event's receipt option are both on. */
    @Transactional(readOnly = true)
    public Optional<Template> forPublicReceipt(UUID eventId) {
        if (!platformEnabled()) return Optional.empty();
        var event = db.queryForList("SELECT id,title,occasion_type FROM events WHERE id=?", eventId);
        if (event.isEmpty()) return Optional.empty();
        Template t = read(eventId, event.getFirst(), false);
        return t.enabled() ? Optional.of(t) : Optional.empty();
    }

    private Template read(UUID eventId, Map<String, Object> event, boolean includeNext) {
        boolean platform = platformEnabled();
        var rows = db.queryForList("SELECT * FROM event_receipt_templates WHERE event_id=?", eventId);
        String next = null;
        if (includeNext) {
            Long last = db.query("SELECT last_number FROM event_receipt_counters WHERE event_id=?", (rs, i) -> rs.getLong(1), eventId)
                .stream().findFirst().orElse(0L);
            next = (rows.isEmpty() ? DEFAULT_PREFIX : (String) rows.getFirst().get("reference_prefix")) + "-" + String.format("%06d", last + 1);
        }
        if (rows.isEmpty()) {
            String occasion = String.valueOf(event.get("occasion_type"));
            return new Template(platform, false, defaultLayout(occasion), defaultColor(occasion), "Gift receipt",
                String.valueOf(event.get("title")), null, "Thank you for your thoughtful gift.",
                "Sent directly to the host. Keep this receipt for your records.", null, DEFAULT_PREFIX,
                true, true, true, true, true, null, 0, next);
        }
        var r = rows.getFirst();
        byte[] logo = (byte[]) r.get("logo_png");
        return new Template(platform, (Boolean) r.get("enabled"), (String) r.get("layout"), ((String) r.get("accent_color")).trim(),
            (String) r.get("title"), (String) r.get("header_text"), (String) r.get("header_subtext"), (String) r.get("thank_you_message"),
            (String) r.get("footer_text"), (String) r.get("signature_name"), (String) r.get("reference_prefix"),
            (Boolean) r.get("show_guest_name"), (Boolean) r.get("show_gift_name"), (Boolean) r.get("show_event_date"),
            (Boolean) r.get("show_payment_method"), (Boolean) r.get("show_payment_code"),
            logo == null ? null : "data:image/png;base64," + Base64.getEncoder().encodeToString(logo),
            ((Number) r.get("version")).longValue(), next);
    }

    private String prefix(UUID eventId) {
        return db.query("SELECT reference_prefix FROM event_receipt_templates WHERE event_id=?", (rs, i) -> rs.getString(1), eventId)
            .stream().findFirst().orElse(DEFAULT_PREFIX);
    }
    static String defaultLayout(String occasion) {
        return switch (occasion) {
            case "WEDDING", "ANNIVERSARY", "RETIREMENT" -> "ELEGANT";
            case "BIRTHDAY", "BABY_SHOWER", "REUNION" -> "FESTIVE";
            case "FAREWELL" -> "MINIMAL";
            default -> "CLASSIC";
        };
    }
    static String defaultColor(String occasion) {
        return switch (occasion) {
            case "WEDDING", "ANNIVERSARY" -> "#9A6B2F";
            case "BIRTHDAY" -> "#D9467A";
            case "BABY_SHOWER" -> "#3A8DBF";
            case "HOUSEWARMING" -> "#2F7D5B";
            default -> "#6336B5";
        };
    }
    private Map<String, Object> owned(UUID eventId) {
        var rows = db.queryForList("SELECT id,title,occasion_type FROM events WHERE id=? AND owner_id=?", eventId, actors.requireAuthenticatedUserId());
        if (rows.size() != 1) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
        return rows.getFirst();
    }
    private void requirePlatform() {
        if (!platformEnabled()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Custom receipts are not enabled on this platform yet");
    }
    private static String text(String value, int max, String label) {
        if (value == null || value.isBlank()) return null;
        String v = value.strip();
        if (v.length() > max) throw bad(label + " must be at most " + max + " characters");
        if (v.chars().anyMatch(c -> Character.isISOControl(c) && c != '\n')) throw bad(label + " contains unsupported characters");
        return v;
    }
    private static ResponseStatusException bad(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, m); }
    private static ResponseStatusException conflict() { return new ResponseStatusException(HttpStatus.CONFLICT, "The receipt design changed in another tab. Reload and try again."); }
}
