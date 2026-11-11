package com.giftplatform.register;

import com.giftplatform.admin.EffectiveModulePermissionService;
import com.giftplatform.security.ActorContext;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.giftplatform.register.GiftPeople.*;

/** Dates worth remembering (birthdays, anniversaries) with a "coming up" feed and what you gave last time. */
@RestController
@RequestMapping("/api/v1/me/reminders")
public class OccasionRemindersController {
    public static final String MODULE = "OCCASION_REMINDERS";
    static final ZoneId INDIA = ZoneId.of("Asia/Kolkata");

    private final JdbcTemplate db;
    private final ActorContext actor;
    private final EffectiveModulePermissionService permissions;

    public OccasionRemindersController(JdbcTemplate db, ActorContext actor, EffectiveModulePermissionService permissions) {
        this.db = db; this.actor = actor; this.permissions = permissions;
    }

    public record Input(String personName, String occasion, Integer month, Integer day, Integer sinceYear, String note) {}
    public record Reminder(UUID id, String personName, String occasion, int month, int day, Integer sinceYear, String note,
                           LocalDate next, long daysAway, Integer turning, Long lastGivenPaise, LocalDate lastGivenOn) {}
    public record Feed(List<Reminder> upcoming, List<Reminder> all) {}

    @GetMapping
    public ResponseEntity<Feed> feed() {
        UUID me = me();
        LocalDate today = LocalDate.now(INDIA);
        List<Reminder> all = db.query("""
            SELECT r.*, g.amount_paise AS last_paise, g.occasion_date AS last_on FROM user_occasion_reminders r
            LEFT JOIN LATERAL (SELECT amount_paise, occasion_date FROM user_gifts_given WHERE user_id=r.user_id AND recipient_key=r.person_key AND deleted_at IS NULL
                               ORDER BY occasion_date DESC NULLS LAST, created_at DESC LIMIT 1) g ON TRUE
            WHERE r.user_id=? ORDER BY r.month, r.day, r.person_name""", (rs, i) -> {
            int month = rs.getInt("month"), day = rs.getInt("day");
            Integer since = (Integer) rs.getObject("since_year");
            LocalDate next = next(today, month, day);
            return new Reminder(rs.getObject("id", UUID.class), rs.getString("person_name"), rs.getString("occasion"), month, day, since, rs.getString("note"),
                next, ChronoUnit.DAYS.between(today, next), since == null ? null : next.getYear() - since,
                (Long) rs.getObject("last_paise"), rs.getObject("last_on", LocalDate.class));
        }, me);
        List<Reminder> upcoming = all.stream().filter(r -> r.daysAway() <= 45).sorted(Comparator.comparingLong(Reminder::daysAway)).toList();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new Feed(upcoming, all));
    }

    @PostMapping
    public Reminder add(@RequestBody Input in) {
        UUID me = me();
        if (in == null || in.month() == null || in.day() == null) throw bad("Choose the date");
        try { MonthDay.of(in.month(), in.day()); } catch (DateTimeException e) { throw bad("That date does not exist"); }
        if (in.sinceYear() != null && (in.sinceYear() < 1900 || in.sinceYear() > LocalDate.now(INDIA).getYear())) throw bad("Check the year");
        String name = name(in.personName(), "Name");
        String occasion = text(in.occasion(), 40, "Occasion");
        if (occasion == null) throw bad("Choose the occasion");
        Integer count = db.queryForObject("SELECT count(*) FROM user_occasion_reminders WHERE user_id=?", Integer.class, me);
        if (count != null && count >= 500) throw bad("You can keep up to 500 dates");
        UUID id = UUID.randomUUID();
        db.update("INSERT INTO user_occasion_reminders(id,user_id,person_name,person_key,occasion,month,day,since_year,note) VALUES(?,?,?,?,?,?,?,?,?)",
            id, me, name, key(name), occasion, in.month(), in.day(), in.sinceYear(), text(in.note(), 200, "Note"));
        return feed().getBody().all().stream().filter(r -> r.id().equals(id)).findFirst().orElseThrow();
    }

    @PostMapping("/{id}/delete")
    public Map<String, Object> delete(@PathVariable UUID id) {
        if (db.update("DELETE FROM user_occasion_reminders WHERE id=? AND user_id=?", id, me()) == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return Map.of("deleted", true);
    }

    /** Next occurrence on or after today; 29 February falls back to 28 February in other years. */
    static LocalDate next(LocalDate today, int month, int day) {
        LocalDate d = safe(today.getYear(), month, day);
        return d.isBefore(today) ? safe(today.getYear() + 1, month, day) : d;
    }
    private static LocalDate safe(int year, int month, int day) {
        return MonthDay.of(month, day).isValidYear(year) ? LocalDate.of(year, month, day) : LocalDate.of(year, month, 28);
    }
    private UUID me() { UUID me = actor.requireAuthenticatedUserId(); permissions.require(me, MODULE); return me; }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> reason(ResponseStatusException ex) {
        int code = ex.getStatusCode().value();
        if ((code == 400 || code == 409) && ex.getReason() != null) return ResponseEntity.status(code).body(Map.of("error", ex.getReason()));
        return ResponseEntity.status(ex.getStatusCode()).build();
    }
}
