package com.giftplatform.eventops;

import com.giftplatform.register.GiftPeople;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Everyone expected at a celebration: "yes" RSVPs, schedule answers and the WhatsApp guest list (unless they said no),
 * plus the household guest list when GUEST_HOUSEHOLDS is on for the event (platform + host + event).
 */
final class ExpectedGuests {
    record Guest(String key, String name, int party, String source) {}
    private ExpectedGuests() {}

    /**
     * Keyed by the normalised name. A household on the guest list is authoritative for its name (the host curated it);
     * otherwise, when a name appears in several lists, the largest party size wins.
     */
    static Map<String, Guest> of(JdbcTemplate db, UUID eventId) {
        Map<String, Guest> out = new LinkedHashMap<>();
        db.query("""
            SELECT name, party, src FROM (
              SELECT h.name, CASE WHEN EXISTS(SELECT 1 FROM event_household_members m WHERE m.household_id=h.id)
                                  THEN (SELECT count(*) FROM event_household_members m WHERE m.household_id=h.id AND m.attending)::int
                                  ELSE h.expected_size END AS party, 'HOUSEHOLD' AS src
                FROM event_households h JOIN events e ON e.id=h.event_id
               WHERE h.event_id=? AND h.rsvp<>'NO'
                 AND EXISTS(SELECT 1 FROM platform_module_controls p JOIN platform_host_module_grants hg ON hg.module_code=p.module_code
                            JOIN platform_event_module_grants eg ON eg.module_code=p.module_code AND eg.event_id=e.id
                            WHERE p.module_code='GUEST_HOUSEHOLDS' AND p.enabled AND hg.host_id=e.owner_id AND hg.enabled AND eg.enabled)
              UNION ALL SELECT guest_name, party_size, 'RSVP' FROM event_rsvps WHERE event_id=? AND attendance='YES'
              UNION ALL SELECT r.guest_name, max(r.party_size), 'SCHEDULE' FROM event_function_rsvps r JOIN event_functions f ON f.id=r.function_id
                WHERE f.event_id=? AND r.attendance='YES' GROUP BY r.guest_name
              UNION ALL SELECT name, COALESCE(reply_party_size,1), 'GUEST_LIST' FROM event_guest_contacts WHERE event_id=? AND COALESCE(reply_attendance,'YES')<>'NO'
            ) x WHERE party > 0 OR src<>'HOUSEHOLD' ORDER BY (src='HOUSEHOLD') DESC LIMIT 5000""", rs -> {
            String name = rs.getString(1), key = GiftPeople.key(name);
            if (key.isEmpty()) return;
            int party = Math.max(1, rs.getInt(2));
            String source = rs.getString(3);
            Guest g = out.get(key);
            if (g != null && g.source().equals("HOUSEHOLD")) return;
            if (g == null || g.party() < party || source.equals("HOUSEHOLD")) out.put(key, new Guest(key, name, party, source));
        }, eventId, eventId, eventId, eventId);
        return out;
    }
}
