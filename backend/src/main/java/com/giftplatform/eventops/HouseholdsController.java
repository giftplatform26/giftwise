package com.giftplatform.eventops;

import com.giftplatform.cohost.EventAccess;
import com.giftplatform.cohost.EventAccess.Capability;
import com.giftplatform.register.GiftPeople;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Guest list organised by household (family): who is invited, how many people, which side, invite and RSVP status,
 * and each member's age group and meal. Households feed the seating planner, guest passes and check-in.
 * Owner and co-hosts with the GUESTS capability can manage it.
 */
@RestController
@RequestMapping("/api/v1/events/{id}/households")
public class HouseholdsController extends EventOpsSupport {
    public static final String MODULE = "GUEST_HOUSEHOLDS";
    static final List<String> SIDES = List.of("BRIDE", "GROOM", "HOST", "FAMILY", "FRIENDS", "WORK", "OTHER");
    static final Set<String> AGES = Set.of("ADULT", "CHILD", "INFANT");
    static final Set<String> MEALS = Set.of("VEG", "JAIN", "NON_VEG", "VEGAN");
    static final Set<String> RSVPS = Set.of("PENDING", "YES", "NO", "MAYBE");
    static final int MAX_HOUSEHOLDS = 3000, MAX_MEMBERS = 30, MAX_IMPORT = 1000;

    private final JdbcTemplate db;
    private final EventAccess access;

    public HouseholdsController(JdbcTemplate db, EventAccess access) { this.db = db; this.access = access; }

    public record MemberInput(String name, String ageGroup, String meal, Boolean attending) {}
    public record Input(String name, String side, String group, String phone, String email, String city, Boolean outstation, Integer expectedSize,
                        String inviteStatus, String rsvp, String note, List<MemberInput> members, Long version) {}
    public record Member(UUID id, String name, String ageGroup, String meal, boolean attending) {}
    public record Household(UUID id, String name, String side, String group, String phone, String email, String city, boolean outstation,
                            int expectedSize, int party, int adults, int children, int infants, String inviteStatus, String rsvp, String note,
                            long version, List<Member> members) {}
    public record SideTotal(String side, int households, int people) {}
    public record Totals(int households, int people, int adults, int children, int infants, int yes, int no, int maybe, int pending,
                         int invited, int outstation, Map<String, Integer> meals, int yesPeople) {}
    public record Book(List<Household> households, Totals totals, List<SideTotal> sides, List<String> groups, List<String> sideChoices) {}
    public record Status(String inviteStatus, String rsvp) {}
    public record ImportText(String text) {}
    public record ImportError(int line, String message) {}
    public record ImportResult(int created, int skipped, List<ImportError> errors) {}

    @GetMapping
    public ResponseEntity<Book> list(@PathVariable UUID id) {
        access.require(id, Capability.GUESTS, MODULE);
        List<Household> all = load(id, null);
        Map<String, int[]> bySide = new LinkedHashMap<>();
        Map<String, Integer> meals = new TreeMap<>();
        int people = 0, adults = 0, children = 0, infants = 0, yes = 0, no = 0, maybe = 0, pending = 0, invited = 0, out = 0, yesPeople = 0;
        for (Household h : all) {
            people += h.party(); adults += h.adults(); children += h.children(); infants += h.infants();
            switch (h.rsvp()) { case "YES" -> { yes++; yesPeople += h.party(); } case "NO" -> no++; case "MAYBE" -> maybe++; default -> pending++; }
            if ("SENT".equals(h.inviteStatus())) invited++;
            if (h.outstation()) out++;
            int[] t = bySide.computeIfAbsent(h.side() == null ? "UNASSIGNED" : h.side(), k -> new int[2]); t[0]++; t[1] += h.party();
            if (!"NO".equals(h.rsvp())) for (Member m : h.members()) if (m.attending() && m.meal() != null) meals.merge(m.meal(), 1, Integer::sum);
        }
        List<SideTotal> sides = bySide.entrySet().stream().map(e -> new SideTotal(e.getKey(), e.getValue()[0], e.getValue()[1])).toList();
        List<String> groups = all.stream().map(Household::group).filter(Objects::nonNull).distinct().sorted(String.CASE_INSENSITIVE_ORDER).toList();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new Book(all,
            new Totals(all.size(), people, adults, children, infants, yes, no, maybe, pending, invited, out, meals, yesPeople), sides, groups, SIDES));
    }

    @PostMapping
    @Transactional
    public Household create(@PathVariable UUID id, @RequestBody Input in) {
        access.require(id, Capability.GUESTS, MODULE);
        Clean c = clean(in);
        lockEvent(id);
        Integer n = db.queryForObject("SELECT count(*) FROM event_households WHERE event_id=?", Integer.class, id);
        if (n != null && n >= MAX_HOUSEHOLDS) throw bad("A guest list can have up to 3,000 households");
        if (exists(id, c.key, null)) throw conflict(c.name + " is already on the guest list");
        UUID hid = UUID.randomUUID();
        insert(id, hid, c);
        return load(id, hid).getFirst();
    }

    @PutMapping("/{householdId}")
    @Transactional
    public Household update(@PathVariable UUID id, @PathVariable UUID householdId, @RequestBody Input in) {
        access.require(id, Capability.GUESTS, MODULE);
        Clean c = clean(in);
        if (in.version() == null) throw changed();
        if (exists(id, c.key, householdId)) throw conflict(c.name + " is already on the guest list");
        int updated = db.update("""
            UPDATE event_households SET name=?,name_key=?,side=?,group_tag=?,phone=?,email=?,city=?,outstation=?,expected_size=?,invite_status=?,rsvp=?,note=?,version=version+1
            WHERE id=? AND event_id=? AND version=?""", c.name, c.key, c.side, c.group, c.phone, c.email, c.city, c.outstation, c.expected, c.invite, c.rsvp, c.note,
            householdId, id, in.version());
        if (updated == 0) throw Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM event_households WHERE id=? AND event_id=?)", Boolean.class, householdId, id)) ? changed() : notFound();
        db.update("DELETE FROM event_household_members WHERE household_id=?", householdId);
        members(id, householdId, c.members);
        return load(id, householdId).getFirst();
    }

    /** One-tap status changes from the list (invite sent, RSVP), without opening the editor. */
    @PostMapping("/{householdId}/status")
    public Household status(@PathVariable UUID id, @PathVariable UUID householdId, @RequestBody Status in) {
        access.require(id, Capability.GUESTS, MODULE);
        if (in == null || (in.inviteStatus() == null && in.rsvp() == null)) throw bad("Nothing to change");
        String invite = in.inviteStatus() == null ? null : invite(in.inviteStatus());
        String rsvp = in.rsvp() == null ? null : rsvp(in.rsvp());
        if (db.update("UPDATE event_households SET invite_status=COALESCE(?,invite_status), rsvp=COALESCE(?,rsvp), version=version+1 WHERE id=? AND event_id=?",
            invite, rsvp, householdId, id) == 0) throw notFound();
        return load(id, householdId).getFirst();
    }

    @PostMapping("/{householdId}/delete")
    public Map<String, Object> delete(@PathVariable UUID id, @PathVariable UUID householdId) {
        access.require(id, Capability.GUESTS, MODULE);
        if (db.update("DELETE FROM event_households WHERE id=? AND event_id=?", householdId, id) == 0) throw notFound();
        return Map.of("deleted", true);
    }

    /**
     * Paste from a spreadsheet: one household per line, columns in this order (only the name is required):
     * name, people, phone, side, group, city, email, outstation. A header row is detected and skipped.
     */
    @PostMapping("/import")
    @Transactional
    public ImportResult importText(@PathVariable UUID id, @RequestBody ImportText in) {
        access.require(id, Capability.GUESTS, MODULE);
        String text = in == null || in.text() == null ? "" : in.text();
        if (text.length() > 400_000) throw bad("That is too much to paste at once. Import up to 1,000 lines at a time.");
        List<List<String>> rows = Csv.parse(text);
        if (!rows.isEmpty() && !rows.getFirst().isEmpty() && rows.getFirst().getFirst().strip().toLowerCase(Locale.ROOT).matches("(household|family|guest)?\\s*name"))
            rows = rows.subList(1, rows.size());
        if (rows.size() > MAX_IMPORT) throw bad("Import up to 1,000 lines at a time");
        lockEvent(id);
        Integer existing = db.queryForObject("SELECT count(*) FROM event_households WHERE event_id=?", Integer.class, id);
        int room = MAX_HOUSEHOLDS - (existing == null ? 0 : existing);
        Set<String> seen = new HashSet<>(db.queryForList("SELECT name_key FROM event_households WHERE event_id=?", String.class, id));
        int created = 0, skipped = 0, line = 0;
        List<ImportError> errors = new ArrayList<>();
        for (List<String> r : rows) {
            line++;
            if (r.stream().allMatch(String::isBlank)) continue;
            try {
                String people = col(r, 1);
                Integer size = people == null ? null : parseSize(people);
                Clean c = clean(new Input(col(r, 0), sideFromText(col(r, 3)), col(r, 4), col(r, 2), col(r, 6), col(r, 5), yes(col(r, 7)), size, null, null, null, List.of(), null));
                if (!seen.add(c.key)) { skipped++; continue; }
                if (created >= room) { errors.add(new ImportError(line, "The guest list is full (3,000 households)")); break; }
                insert(id, UUID.randomUUID(), c);
                created++;
            } catch (org.springframework.web.server.ResponseStatusException ex) {
                if (errors.size() < 50) errors.add(new ImportError(line, ex.getReason()));
            }
        }
        return new ImportResult(created, skipped, errors);
    }

    /** Adds everyone already known from RSVPs and the WhatsApp guest list who isn't on the guest list yet. */
    @PostMapping("/import-existing")
    @Transactional
    public ImportResult importExisting(@PathVariable UUID id) {
        access.require(id, Capability.GUESTS, MODULE);
        lockEvent(id);
        Set<String> seen = new HashSet<>(db.queryForList("SELECT name_key FROM event_households WHERE event_id=?", String.class, id));
        Integer existing = db.queryForObject("SELECT count(*) FROM event_households WHERE event_id=?", Integer.class, id);
        int room = MAX_HOUSEHOLDS - (existing == null ? 0 : existing), created = 0, skipped = 0;
        List<Map<String, Object>> sources = db.queryForList("""
            SELECT guest_name AS name, NULL AS phone, guest_email AS email, attendance AS rsvp, party_size AS size FROM event_rsvps WHERE event_id=?
            UNION ALL SELECT name, phone, NULL, reply_attendance, reply_party_size FROM event_guest_contacts WHERE event_id=? AND opted_out_at IS NULL
            LIMIT 5000""", id, id);
        for (var s : sources) {
            String name = (String) s.get("name"), key = GiftPeople.key(name);
            if (key.isEmpty() || !seen.add(key)) { skipped++; continue; }
            if (created >= room) break;
            String r = s.get("rsvp") == null ? "PENDING" : String.valueOf(s.get("rsvp"));
            int size = s.get("size") == null ? 1 : Math.max(1, Math.min(30, ((Number) s.get("size")).intValue()));
            String email = (String) s.get("email");
            if (email != null && !email.matches("[^@\\s]{1,64}@[^@\\s]{1,120}\\.[^@\\s]{2,}")) email = null;
            String phone = (String) s.get("phone");
            insert(id, UUID.randomUUID(), new Clean(name.strip().length() > 120 ? name.strip().substring(0, 120) : name.strip(), key, null, null,
                phone, email, null, false, size, "SENT", RSVPS.contains(r) ? r : "PENDING", null, List.of()));
            created++;
        }
        return new ImportResult(created, skipped, List.of());
    }

    @GetMapping(value = "/export.csv", produces = "text/csv")
    public ResponseEntity<byte[]> export(@PathVariable UUID id) {
        access.require(id, Capability.GUESTS, MODULE);
        StringBuilder b = new StringBuilder("Household,People,Adults,Children,Infants,Side,Group,Phone,Email,City,Outstation,Invite,RSVP,Members,Note\n");
        for (Household h : load(id, null)) {
            String members = String.join("; ", h.members().stream().map(m -> m.name() + (m.attending() ? "" : " (not coming)") + (m.meal() == null ? "" : " [" + m.meal() + "]")).toList());
            b.append(String.join(",", Csv.cell(h.name()), String.valueOf(h.party()), String.valueOf(h.adults()), String.valueOf(h.children()), String.valueOf(h.infants()),
                Csv.cell(h.side()), Csv.cell(h.group()), Csv.cell(h.phone()), Csv.cell(h.email()), Csv.cell(h.city()), h.outstation() ? "Yes" : "No",
                "SENT".equals(h.inviteStatus()) ? "Sent" : "Not sent", h.rsvp(), Csv.cell(members), Csv.cell(h.note()))).append('\n');
        }
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"guest-list.csv\"").body(("﻿" + b).getBytes(StandardCharsets.UTF_8));
    }

    // ---- Helpers ----

    private record Clean(String name, String key, String side, String group, String phone, String email, String city, boolean outstation, int expected,
                         String invite, String rsvp, String note, List<MemberInput> members) {}

    private Clean clean(Input in) {
        if (in == null) throw bad("Household details are required");
        String name = required(in.name(), 120, "Household name");
        String key = GiftPeople.key(name);
        if (key.isEmpty()) throw bad("Enter a real household name");
        String side = in.side() == null || in.side().isBlank() ? null : in.side().strip().toUpperCase(Locale.ROOT);
        if (side != null && !SIDES.contains(side)) throw bad("Choose a side from the list");
        String email = text(in.email(), 160, "Email");
        if (email != null && !email.matches("[^@\\s]{1,64}@[^@\\s]{1,120}\\.[^@\\s]{2,}")) throw bad("Enter a valid email address, or leave it empty");
        int expected = in.expectedSize() == null ? 1 : in.expectedSize();
        if (expected < 1 || expected > MAX_MEMBERS) throw bad("People in a household must be between 1 and 30");
        List<MemberInput> members = in.members() == null ? List.of() : in.members();
        if (members.size() > MAX_MEMBERS) throw bad("A household can list up to 30 people");
        for (MemberInput m : members) {
            if (m == null) throw bad("Member details are required");
            required(m.name(), 120, "Member name");
            if (m.ageGroup() != null && !m.ageGroup().isBlank() && !AGES.contains(m.ageGroup().strip().toUpperCase(Locale.ROOT))) throw bad("Choose adult, child or infant");
            if (m.meal() != null && !m.meal().isBlank() && !MEALS.contains(m.meal().strip().toUpperCase(Locale.ROOT))) throw bad("Choose a meal from the list");
        }
        return new Clean(name, key, side, text(in.group(), 40, "Group"), GiftPeople.phone(in.phone()), email, text(in.city(), 80, "City"),
            Boolean.TRUE.equals(in.outstation()), expected, in.inviteStatus() == null ? "NOT_SENT" : invite(in.inviteStatus()),
            in.rsvp() == null ? "PENDING" : rsvp(in.rsvp()), text(in.note(), 300, "Note"), members);
    }

    private void insert(UUID event, UUID hid, Clean c) {
        db.update("""
            INSERT INTO event_households(id,event_id,name,name_key,side,group_tag,phone,email,city,outstation,expected_size,invite_status,rsvp,note)
            VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)""", hid, event, c.name, c.key, c.side, c.group, c.phone, c.email, c.city, c.outstation, c.expected, c.invite, c.rsvp, c.note);
        members(event, hid, c.members);
    }

    private void members(UUID event, UUID hid, List<MemberInput> members) {
        int order = 0;
        for (MemberInput m : members) {
            String age = m.ageGroup() == null || m.ageGroup().isBlank() ? "ADULT" : m.ageGroup().strip().toUpperCase(Locale.ROOT);
            String meal = m.meal() == null || m.meal().isBlank() ? null : m.meal().strip().toUpperCase(Locale.ROOT);
            db.update("INSERT INTO event_household_members(id,household_id,event_id,name,age_group,meal,attending,sort_order) VALUES(?,?,?,?,?,?,?,?)",
                UUID.randomUUID(), hid, event, m.name().strip(), age, meal, m.attending() == null || m.attending(), order++);
        }
    }

    private List<Household> load(UUID event, UUID only) {
        Map<UUID, List<Member>> members = new HashMap<>();
        db.query("SELECT * FROM event_household_members WHERE event_id=? AND (?::uuid IS NULL OR household_id=?) ORDER BY sort_order",
            rs -> { members.computeIfAbsent(rs.getObject("household_id", UUID.class), k -> new ArrayList<>()).add(new Member(rs.getObject("id", UUID.class),
                rs.getString("name"), rs.getString("age_group"), rs.getString("meal"), rs.getBoolean("attending"))); }, event, only, only);
        List<Household> out = db.query("SELECT * FROM event_households WHERE event_id=? AND (?::uuid IS NULL OR id=?) ORDER BY lower(name)", (rs, i) -> {
            UUID hid = rs.getObject("id", UUID.class);
            List<Member> list = members.getOrDefault(hid, List.of());
            int expected = rs.getInt("expected_size");
            int adults = (int) list.stream().filter(m -> m.attending() && m.ageGroup().equals("ADULT")).count();
            int children = (int) list.stream().filter(m -> m.attending() && m.ageGroup().equals("CHILD")).count();
            int infants = (int) list.stream().filter(m -> m.attending() && m.ageGroup().equals("INFANT")).count();
            int party = list.isEmpty() ? expected : adults + children + infants;
            if (list.isEmpty()) adults = expected;
            return new Household(hid, rs.getString("name"), rs.getString("side"), rs.getString("group_tag"), rs.getString("phone"), rs.getString("email"),
                rs.getString("city"), rs.getBoolean("outstation"), expected, party, adults, children, infants, rs.getString("invite_status"), rs.getString("rsvp"),
                rs.getString("note"), rs.getLong("version"), list);
        }, event, only, only);
        if (only != null && out.isEmpty()) throw notFound();
        return out;
    }

    private boolean exists(UUID event, String key, UUID except) {
        return Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM event_households WHERE event_id=? AND name_key=? AND (?::uuid IS NULL OR id<>?))",
            Boolean.class, event, key, except, except));
    }
    private void lockEvent(UUID id) { db.queryForList("SELECT id FROM events WHERE id=? FOR UPDATE", id); }
    private static String invite(String v) {
        String s = v.strip().toUpperCase(Locale.ROOT);
        if (!s.equals("SENT") && !s.equals("NOT_SENT")) throw bad("Invite status must be sent or not sent");
        return s;
    }
    private static String rsvp(String v) {
        String s = v.strip().toUpperCase(Locale.ROOT);
        if (!RSVPS.contains(s)) throw bad("RSVP must be pending, yes, no or maybe");
        return s;
    }
    private static String col(List<String> r, int i) { return i < r.size() && !r.get(i).isBlank() ? r.get(i).strip() : null; }
    private static Integer parseSize(String v) {
        try { return Integer.parseInt(v.replaceAll("[^0-9]", "")); } catch (NumberFormatException e) { throw bad("People must be a number"); }
    }
    private static Boolean yes(String v) { return v != null && Set.of("yes", "y", "true", "1", "outstation").contains(v.toLowerCase(Locale.ROOT)); }
    /** Accepts the codes or everyday words people type in a spreadsheet ("Bride side", "Friends"). */
    static String sideFromText(String v) {
        if (v == null) return null;
        String s = v.strip().toUpperCase(Locale.ROOT).replace("'S", "").replace(" SIDE", "").replace(' ', '_');
        if (SIDES.contains(s)) return s;
        return switch (s) { case "FRIEND" -> "FRIENDS"; case "OFFICE", "COLLEAGUES", "COLLEAGUE" -> "WORK"; case "RELATIVES", "RELATIVE" -> "FAMILY"; default -> "OTHER"; };
    }

    /** Small CSV reader for pasted spreadsheets: commas or tabs, double-quoted cells, doubled quotes inside quotes. */
    static final class Csv {
        private Csv() {}
        static List<List<String>> parse(String text) {
            List<List<String>> rows = new ArrayList<>();
            String body = text.replace("\r\n", "\n").replace('\r', '\n');
            char sep = body.lines().findFirst().map(l -> l.indexOf('\t') >= 0 ? '\t' : ',').orElse(',');
            List<String> row = new ArrayList<>();
            StringBuilder cell = new StringBuilder();
            boolean quoted = false;
            for (int i = 0; i < body.length(); i++) {
                char ch = body.charAt(i);
                if (quoted) {
                    if (ch == '"' && i + 1 < body.length() && body.charAt(i + 1) == '"') { cell.append('"'); i++; }
                    else if (ch == '"') quoted = false;
                    else cell.append(ch);
                } else if (ch == '"' && cell.isEmpty()) quoted = true;
                else if (ch == sep) { row.add(cell.toString()); cell.setLength(0); }
                else if (ch == '\n') { row.add(cell.toString()); cell.setLength(0); rows.add(row); row = new ArrayList<>(); }
                else cell.append(ch);
            }
            if (!cell.isEmpty() || !row.isEmpty()) { row.add(cell.toString()); rows.add(row); }
            return rows;
        }
        /** Quotes a cell for export and defuses spreadsheet formulas. */
        static String cell(String v) {
            if (v == null) return "";
            String s = v;
            if (!s.isEmpty() && "=+-@".indexOf(s.charAt(0)) >= 0) s = "'" + s;
            return s.matches(".*[\",\\n].*") || !s.equals(v) ? "\"" + s.replace("\"", "\"\"") + "\"" : s;
        }
    }
}
