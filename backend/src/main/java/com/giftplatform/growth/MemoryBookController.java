package com.giftplatform.growth;

import com.giftplatform.admin.EffectiveModulePermissionService;
import com.giftplatform.cohost.EventAccess;
import com.giftplatform.event.entity.Event;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/**
 * Printable keepsake for the host: schedule, approved wishes and photos, guest counts and the people who gave gifts.
 * Amounts are left out by default; the host chooses to include them. Rendered and printed (or saved as PDF) in the browser.
 */
@RestController
public class MemoryBookController {
    public static final String MODULE = "MEMORY_BOOK";
    private final JdbcTemplate db;
    private final EventAccess access;
    private final EffectiveModulePermissionService permissions;

    public MemoryBookController(JdbcTemplate db, EventAccess access, EffectiveModulePermissionService permissions) {
        this.db = db; this.access = access; this.permissions = permissions;
    }

    public record Fn(String name, LocalDate date, LocalTime start, String venue) {}
    public record Wish(UUID id, String guestName, String message, String photoUrl) {}
    public record Giver(String name, Long amountPaise, String item) {}
    public record Book(String title, String hostName, String occasion, LocalDate eventDate, String description, List<Fn> functions,
                       List<Wish> wishes, long guestsAttending, long rsvpResponses, List<Giver> givers, boolean amountsIncluded) {}

    @GetMapping("/api/v1/events/{id}/memory-book")
    public ResponseEntity<Book> book(@PathVariable UUID id, @RequestParam(defaultValue = "false") boolean amounts) {
        Event e = access.requireOwner(id, MODULE);
        UUID owner = e.getOwnerId();
        List<Fn> functions = permissions.eventEnabled(owner, id, "EVENT_FUNCTIONS") ? db.query(
            "SELECT name,function_date,start_time,venue_name FROM event_functions WHERE event_id=? ORDER BY sort_order",
            (rs, i) -> new Fn(rs.getString(1), rs.getObject(2, LocalDate.class), rs.getObject(3, LocalTime.class), rs.getString(4)), id) : List.of();
        String photo = "/backend/api/v1/events/" + id + "/wishes/";
        List<Wish> wishes = permissions.enabled(owner, "GUESTBOOK") ? db.query(
            "SELECT id,guest_name,message,(photo_path IS NOT NULL) AS p FROM event_guestbook WHERE event_id=? AND status='APPROVED' ORDER BY created_at LIMIT 400",
            (rs, i) -> { UUID w = rs.getObject(1, UUID.class); return new Wish(w, rs.getString(2), rs.getString(3), rs.getBoolean(4) ? photo + w + "/photo" : null); }, id) : List.of();
        long[] rsvp = db.queryForObject("SELECT COALESCE(SUM(party_size) FILTER (WHERE attendance='YES'),0), count(*) FROM event_rsvps WHERE event_id=?",
            (rs, i) -> new long[]{rs.getLong(1), rs.getLong(2)}, id);
        List<Giver> givers = permissions.eventEnabled(owner, id, "SHAGUN_REGISTER") ? db.query("""
            SELECT giver_name, SUM(amount_paise)::bigint AS paise, string_agg(item_description, ', ') AS items FROM event_shagun_entries
            WHERE event_id=? AND deleted_at IS NULL GROUP BY giver_key, giver_name ORDER BY min(created_at)""",
            (rs, i) -> new Giver(rs.getString(1), amounts ? (Long) rs.getObject(2) : null, rs.getString(3)), id) : List.of();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new Book(e.getTitle(), e.getHostDisplayName(), e.getOccasionType().name(),
            e.getEventDate(), e.getDescription(), functions, wishes, rsvp[0], rsvp[1], givers, amounts));
    }
}
