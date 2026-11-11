package com.giftplatform.growth;

import com.giftplatform.admin.EffectiveModulePermissionService;
import com.giftplatform.event.entity.Event;
import com.giftplatform.event.entity.OccasionType;
import com.giftplatform.event.repository.EventRepository;
import com.giftplatform.security.ActorContext;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * "It's almost that time again": birthdays and anniversaries hosted last year are suggested ~2 months ahead,
 * and become a new private draft (same details, same WhatsApp guest list if wanted) in one tap.
 */
@RestController
@RequestMapping("/api/v1/me/rehost")
public class YearlyRehostController {
    public static final String MODULE = "YEARLY_REHOST";
    static final Set<String> RECURRING = Set.of("BIRTHDAY", "ANNIVERSARY");

    private final JdbcTemplate db;
    private final EventRepository events;
    private final ActorContext actor;
    private final EffectiveModulePermissionService permissions;

    public YearlyRehostController(JdbcTemplate db, EventRepository events, ActorContext actor, EffectiveModulePermissionService permissions) {
        this.db = db; this.events = events; this.actor = actor; this.permissions = permissions;
    }

    public record Suggestion(UUID eventId, String title, String occasion, LocalDate lastDate, LocalDate nextDate, long daysAway, String suggestedTitle, int guests) {}
    public record RepeatInput(String title, Boolean copyGuests) {}

    @GetMapping
    public ResponseEntity<List<Suggestion>> suggestions() {
        UUID me = me();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        List<Suggestion> out = new ArrayList<>();
        db.query("""
            SELECT e.id, e.title, e.occasion_type, e.event_date, (SELECT count(*) FROM event_guest_contacts c WHERE c.event_id=e.id AND c.opted_out_at IS NULL) AS guests
            FROM events e WHERE e.owner_id=? AND e.occasion_type IN ('BIRTHDAY','ANNIVERSARY') AND e.event_date IS NOT NULL
              AND e.event_date < ?::date - 180 AND e.status <> 'CANCELLED'
              AND NOT EXISTS (SELECT 1 FROM events n WHERE n.repeated_from=e.id AND n.created_at > now() - interval '300 days')
              AND NOT EXISTS (SELECT 1 FROM events later WHERE later.owner_id=e.owner_id AND later.repeated_from IS NOT NULL
                              AND later.title=e.title AND later.created_at > now() - interval '300 days')
            ORDER BY e.event_date DESC LIMIT 50""", rs -> {
            LocalDate last = rs.getObject("event_date", LocalDate.class);
            LocalDate next = next(today, last);
            long days = ChronoUnit.DAYS.between(today, next);
            if (days > 60 || out.stream().anyMatch(s -> s.title().equals(safeTitle(rs)))) return;
            String title = rs.getString("title");
            out.add(new Suggestion(rs.getObject("id", UUID.class), title, rs.getString("occasion_type"), last, next, days,
                retitle(title, last.getYear(), next.getYear()), rs.getInt("guests")));
        }, me, today);
        out.sort(Comparator.comparingLong(Suggestion::daysAway));
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(out);
    }

    @PostMapping("/{eventId}")
    @Transactional
    public Map<String, Object> repeat(@PathVariable UUID eventId, @RequestBody(required = false) RepeatInput in) {
        UUID me = me();
        Event source = events.findById(eventId).filter(e -> me.equals(e.getOwnerId()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        if (!RECURRING.contains(source.getOccasionType().name()) || source.getEventDate() == null) throw bad("Only dated birthdays and anniversaries can repeat yearly");
        Boolean done = db.queryForObject("SELECT EXISTS(SELECT 1 FROM events WHERE repeated_from=? AND created_at>now()-interval '300 days')", Boolean.class, eventId);
        if (Boolean.TRUE.equals(done)) throw new ResponseStatusException(HttpStatus.CONFLICT, "You already planned this year's celebration");
        LocalDate next = next(LocalDate.now(ZoneId.of("Asia/Kolkata")), source.getEventDate());
        String title = in == null || in.title() == null || in.title().isBlank() ? retitle(source.getTitle(), source.getEventDate().getYear(), next.getYear()) : in.title().strip();
        if (title.length() > 200) throw bad("Title must be at most 200 characters");
        Event e = new Event(title, source.getOccasionType());
        e.assignOwner(me);
        e.setHostDisplayName(source.getHostDisplayName());
        e.setEventContact(source.getAddressLine1(), source.getCity(), source.getState(), source.getPostalCode(), source.getCountry(),
            source.getContactName(), source.getContactPhone(), source.getContactEmail());
        e.setDeliveryAddress(source.isDeliverySameAsEvent(), source.getDeliveryAddress());
        e.update(title, source.getDescription(), source.getOccasionType(), next);
        e.setGiftingEnabled(source.isGiftingEnabled());
        events.saveAndFlush(e);
        db.update("UPDATE events SET repeated_from=? WHERE id=?", eventId, e.getId());
        int guests = 0;
        if (in == null || in.copyGuests() == null || in.copyGuests())
            guests = db.update("INSERT INTO event_guest_contacts(id,event_id,name,phone) SELECT gen_random_uuid(),?,name,phone FROM event_guest_contacts WHERE event_id=? AND opted_out_at IS NULL",
                e.getId(), eventId);
        return Map.of("id", e.getId(), "guestsCopied", guests, "eventDate", next.toString());
    }

    static LocalDate next(LocalDate today, LocalDate last) {
        MonthDay md = MonthDay.from(last);
        LocalDate d = md.isValidYear(today.getYear()) ? md.atYear(today.getYear()) : LocalDate.of(today.getYear(), 2, 28);
        if (d.isBefore(today)) d = md.isValidYear(today.getYear() + 1) ? md.atYear(today.getYear() + 1) : LocalDate.of(today.getYear() + 1, 2, 28);
        return d;
    }
    /** "Asha turns 5 · 2025" → "Asha turns 5 · 2026"; otherwise appends the new year. */
    static String retitle(String title, int oldYear, int newYear) {
        String y = String.valueOf(oldYear);
        if (title.contains(y)) return title.replace(y, String.valueOf(newYear));
        String t = title + " " + newYear;
        return t.length() > 200 ? title : t;
    }
    private static String safeTitle(java.sql.ResultSet rs) { try { return rs.getString("title"); } catch (java.sql.SQLException e) { return ""; } }
    private UUID me() { UUID me = actor.requireAuthenticatedUserId(); permissions.require(me, MODULE); return me; }
    private static ResponseStatusException bad(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, m); }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> reason(ResponseStatusException ex) {
        int code = ex.getStatusCode().value();
        if ((code == 400 || code == 409) && ex.getReason() != null) return ResponseEntity.status(code).body(Map.of("error", ex.getReason()));
        return ResponseEntity.status(ex.getStatusCode()).build();
    }
}
