package com.giftplatform.eventops;

import com.giftplatform.register.GiftPeople;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Everyone expected at a celebration: "yes" RSVPs, schedule answers and the WhatsApp guest list (unless they said no). */
final class ExpectedGuests {
    record Guest(String key, String name, int party, String source) {}
    private ExpectedGuests() {}

    /** Keyed by the normalised name; when a name appears in several lists the largest party size wins. */
    static Map<String, Guest> of(JdbcTemplate db, UUID eventId) {
        Map<String, Guest> out = new LinkedHashMap<>();
        db.query("""
            SELECT guest_name, party_size, 'RSVP' AS src FROM event_rsvps WHERE event_id=? AND attendance='YES'
            UNION ALL SELECT r.guest_name, max(r.party_size), 'SCHEDULE' FROM event_function_rsvps r JOIN event_functions f ON f.id=r.function_id
              WHERE f.event_id=? AND r.attendance='YES' GROUP BY r.guest_name
            UNION ALL SELECT name, COALESCE(reply_party_size,1), 'GUEST_LIST' FROM event_guest_contacts WHERE event_id=? AND COALESCE(reply_attendance,'YES')<>'NO'
            LIMIT 5000""", rs -> {
            String name = rs.getString(1), key = GiftPeople.key(name);
            if (key.isEmpty()) return;
            int party = Math.max(1, rs.getInt(2));
            Guest g = out.get(key);
            if (g == null || g.party() < party) out.put(key, new Guest(key, name, party, rs.getString(3)));
        }, eventId, eventId, eventId);
        return out;
    }
}
