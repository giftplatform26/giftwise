package com.giftplatform.event.entity;

import java.util.*;

/**
 * Occasion-specific invitation details (bride/groom names, birthday age, ...).
 * The allowed keys per occasion mirror web/lib/occasion-details.mjs; unknown keys are rejected so the
 * stored JSON stays small and predictable. Values are short plain text shown on the invitation.
 */
public final class OccasionDetails {
    public static final int MAX_VALUE_LENGTH = 200;
    private static final List<String> COMMON = List.of("venueName", "startTime");
    private static final Map<OccasionType, List<String>> FIELDS = new EnumMap<>(OccasionType.class);
    static {
        FIELDS.put(OccasionType.WEDDING, List.of("brideName", "groomName", "brideFamily", "groomFamily", "muhurtham", "receptionDetails"));
        FIELDS.put(OccasionType.BIRTHDAY, List.of("celebrantName", "age", "theme"));
        FIELDS.put(OccasionType.BABY_SHOWER, List.of("parentNames", "dueMonth", "theme"));
        FIELDS.put(OccasionType.HOUSEWARMING, List.of("familyName", "homeName", "ceremony"));
        FIELDS.put(OccasionType.ANNIVERSARY, List.of("coupleNames", "years"));
        FIELDS.put(OccasionType.FAREWELL, List.of("honoreeName", "organisation", "nextChapter"));
        FIELDS.put(OccasionType.RETIREMENT, List.of("honoreeName", "organisation", "yearsOfService"));
        FIELDS.put(OccasionType.REUNION, List.of("groupName", "batchYear"));
        FIELDS.put(OccasionType.OTHER, List.of("honoreeName", "occasionName"));
    }
    private OccasionDetails() {}

    public static List<String> allowedKeys(OccasionType occasion) {
        var keys = new ArrayList<>(FIELDS.getOrDefault(occasion, List.of()));
        keys.addAll(COMMON);
        return List.copyOf(keys);
    }

    /**
     * Returns a cleaned copy containing only non-blank values for this occasion, in a stable order.
     * Keys that belong to a different occasion are dropped (the host may have switched occasion);
     * unknown keys or oversized/unsafe values are rejected.
     */
    public static Map<String, String> normalize(OccasionType occasion, Map<String, String> input) {
        if (input == null || input.isEmpty()) return Map.of();
        if (input.size() > 30) throw new IllegalArgumentException("Too many occasion details");
        Set<String> known = new HashSet<>(COMMON);
        FIELDS.values().forEach(known::addAll);
        for (String key : input.keySet())
            if (key == null || !known.contains(key)) throw new IllegalArgumentException("Unsupported occasion detail: " + key);
        Map<String, String> out = new LinkedHashMap<>();
        for (String key : allowedKeys(occasion)) {
            String value = input.get(key);
            if (value == null) continue;
            value = value.strip();
            if (value.isEmpty()) continue;
            if (value.length() > MAX_VALUE_LENGTH || value.codePoints().anyMatch(Character::isISOControl) || value.contains("<") || value.contains(">"))
                throw new IllegalArgumentException("Invalid value for " + key);
            out.put(key, value);
        }
        return Collections.unmodifiableMap(out);
    }
}
