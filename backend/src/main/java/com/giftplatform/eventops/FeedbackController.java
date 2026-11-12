package com.giftplatform.eventops;

import com.giftplatform.admin.EffectiveModulePermissionService;
import com.giftplatform.cohost.EventAccess;
import com.giftplatform.cohost.EventAccess.Capability;
import com.giftplatform.event.entity.Event;
import com.giftplatform.event.entity.EventStatus;
import com.giftplatform.event.repository.EventRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Guest feedback after the event: star ratings (overall, food, venue, arrangements, hospitality), a comment and one
 * optional question of the host's own. Guests answer from the invitation while the host has feedback open.
 * Owner and co-hosts with the GUESTS capability see the results.
 */
@RestController
public class FeedbackController extends EventOpsSupport {
    public static final String MODULE = "GUEST_FEEDBACK";
    static final List<String> ASPECTS = List.of("food", "venue", "arrangements", "hospitality");

    private final JdbcTemplate db;
    private final EventAccess access;
    private final EventRepository events;
    private final EffectiveModulePermissionService permissions;

    public FeedbackController(JdbcTemplate db, EventAccess access, EventRepository events, EffectiveModulePermissionService permissions) {
        this.db = db; this.access = access; this.events = events; this.permissions = permissions;
    }

    public record Settings(boolean collecting, String question) {}
    public record Response(UUID id, String guestName, int overall, Integer food, Integer venue, Integer arrangements, Integer hospitality,
                           String comment, String answer, boolean hidden, OffsetDateTime createdAt) {}
    public record Summary(int count, Double overall, Map<String, Double> aspects, int[] stars) {}
    public record Results(Settings settings, Summary summary, List<Response> responses, String publicPath) {}
    public record PublicForm(String eventTitle, LocalDate eventDate, String hostName, boolean open, String question) {}
    public record Submit(String guestName, Integer overall, Integer food, Integer venue, Integer arrangements, Integer hospitality, String comment, String answer) {}
    public record Hide(boolean hidden) {}

    @GetMapping("/api/v1/events/{id}/feedback")
    public ResponseEntity<Results> results(@PathVariable UUID id) {
        var a = access.require(id, Capability.GUESTS, MODULE);
        List<Response> all = db.query("SELECT * FROM event_feedback WHERE event_id=? ORDER BY created_at DESC LIMIT 5000", (rs, i) -> new Response(rs.getObject("id", UUID.class),
            rs.getString("guest_name"), rs.getInt("overall"), (Integer) intOrNull(rs, "food"), intOrNull(rs, "venue"), intOrNull(rs, "arrangements"), intOrNull(rs, "hospitality"),
            rs.getString("comment"), rs.getString("answer"), rs.getBoolean("hidden"), rs.getObject("created_at", OffsetDateTime.class)), id);
        List<Response> counted = all.stream().filter(r -> !r.hidden()).toList();
        int[] stars = new int[5];
        counted.forEach(r -> stars[r.overall() - 1]++);
        Map<String, Double> aspects = new LinkedHashMap<>();
        aspects.put("food", avg(counted.stream().map(Response::food).toList()));
        aspects.put("venue", avg(counted.stream().map(Response::venue).toList()));
        aspects.put("arrangements", avg(counted.stream().map(Response::arrangements).toList()));
        aspects.put("hospitality", avg(counted.stream().map(Response::hospitality).toList()));
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new Results(settings(id),
            new Summary(counted.size(), avg(counted.stream().map(Response::overall).toList()), aspects, stars), all, "/e/" + a.event().getPublicToken() + "/feedback"));
    }

    @PutMapping("/api/v1/events/{id}/feedback/settings")
    public Settings save(@PathVariable UUID id, @RequestBody Settings in) {
        access.require(id, Capability.GUESTS, MODULE);
        if (in == null) throw bad("Settings are required");
        db.update("INSERT INTO event_feedback_settings(event_id,collecting,question) VALUES(?,?,?) ON CONFLICT(event_id) DO UPDATE SET collecting=EXCLUDED.collecting,question=EXCLUDED.question",
            id, in.collecting(), text(in.question(), 200, "Your question"));
        return settings(id);
    }

    /** Hidden responses stay in the list for the host but are left out of the averages (e.g. spam or a test). */
    @PostMapping("/api/v1/events/{id}/feedback/{responseId}/hide")
    public Map<String, Object> hide(@PathVariable UUID id, @PathVariable UUID responseId, @RequestBody Hide in) {
        access.require(id, Capability.GUESTS, MODULE);
        boolean h = in != null && in.hidden();
        if (db.update("UPDATE event_feedback SET hidden=? WHERE id=? AND event_id=?", h, responseId, id) == 0) throw notFound();
        return Map.of("hidden", h);
    }

    @GetMapping(value = "/api/v1/events/{id}/feedback/export.csv", produces = "text/csv")
    public ResponseEntity<byte[]> export(@PathVariable UUID id) {
        access.require(id, Capability.GUESTS, MODULE);
        StringBuilder b = new StringBuilder("Date,Name,Overall,Food,Venue,Arrangements,Hospitality,Comment,Answer,Hidden\n");
        db.query("SELECT * FROM event_feedback WHERE event_id=? ORDER BY created_at", rs -> {
            b.append(String.join(",", rs.getObject("created_at", OffsetDateTime.class).toLocalDate().toString(), HouseholdsController.Csv.cell(rs.getString("guest_name")),
                String.valueOf(rs.getInt("overall")), n(rs, "food"), n(rs, "venue"), n(rs, "arrangements"), n(rs, "hospitality"),
                HouseholdsController.Csv.cell(rs.getString("comment")), HouseholdsController.Csv.cell(rs.getString("answer")), rs.getBoolean("hidden") ? "Yes" : "No")).append('\n');
        }, id);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"guest-feedback.csv\"").body(("﻿" + b).getBytes(StandardCharsets.UTF_8));
    }

    // ---- Guests ----

    @GetMapping("/api/public/celebrations/{token}/feedback")
    public ResponseEntity<PublicForm> form(@PathVariable UUID token) {
        Event e = published(token);
        Settings s = settings(e.getId());
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new PublicForm(e.getTitle(), e.getEventDate(), e.getHostDisplayName(), s.collecting(), s.collecting() ? s.question() : null));
    }

    @PostMapping("/api/public/celebrations/{token}/feedback")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> submit(@PathVariable UUID token, @RequestBody Submit in, HttpServletRequest http) {
        Event e = published(token);
        if (!settings(e.getId()).collecting()) throw conflict("The hosts are not collecting feedback right now");
        if (in == null || in.overall() == null) throw bad("Choose an overall rating");
        int overall = star(in.overall(), "Overall");
        Integer food = opt(in.food(), "Food"), venue = opt(in.venue(), "Venue"), arr = opt(in.arrangements(), "Arrangements"), hosp = opt(in.hospitality(), "Hospitality");
        String name = text(in.guestName(), 80, "Name"), comment = text(in.comment(), 1000, "Comment"), answer = text(in.answer(), 500, "Answer");
        limit(db, e.getId(), http, "FEEDBACK", 5);
        db.update("INSERT INTO event_feedback(id,event_id,guest_name,overall,food,venue,arrangements,hospitality,comment,answer) VALUES(?,?,?,?,?,?,?,?,?,?)",
            UUID.randomUUID(), e.getId(), name, overall, food, venue, arr, hosp, comment, answer);
        return Map.of("message", "Thank you for your feedback!");
    }

    // ---- Helpers ----

    private Event published(UUID token) {
        Event e = events.findByPublicToken(token).filter(x -> x.getStatus() == EventStatus.PUBLISHED).orElseThrow(EventOpsSupport::notFound);
        if (!permissions.eventEnabled(e.getOwnerId(), e.getId(), MODULE)) throw notFound();
        return e;
    }
    private Settings settings(UUID event) {
        return db.query("SELECT collecting, question FROM event_feedback_settings WHERE event_id=?", (rs, i) -> new Settings(rs.getBoolean(1), rs.getString(2)), event)
            .stream().findFirst().orElse(new Settings(false, null));
    }
    private static int star(int v, String label) { if (v < 1 || v > 5) throw bad(label + " rating must be 1 to 5 stars"); return v; }
    private static Integer opt(Integer v, String label) { return v == null || v == 0 ? null : star(v, label); }
    private static Double avg(List<Integer> values) {
        var list = values.stream().filter(Objects::nonNull).mapToInt(Integer::intValue).toArray();
        return list.length == 0 ? null : Math.round(Arrays.stream(list).average().orElse(0) * 10) / 10.0;
    }
    private static Integer intOrNull(java.sql.ResultSet rs, String c) throws java.sql.SQLException { int v = rs.getInt(c); return rs.wasNull() ? null : v; }
    private static String n(java.sql.ResultSet rs, String c) throws java.sql.SQLException { Integer v = intOrNull(rs, c); return v == null ? "" : v.toString(); }
}
