package com.giftplatform.event.entity;

import org.junit.jupiter.api.Test;
import java.util.LinkedHashMap;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;

class OccasionDetailsTest {
    @Test void keepsOnlyThisOccasionsNonBlankTrimmedValues() {
        var input = new LinkedHashMap<String,String>();
        input.put("groomName", " Rahul "); input.put("brideName", "Anu"); input.put("celebrantName", "Maya"); input.put("muhurtham", "  ");
        assertThat(OccasionDetails.normalize(OccasionType.WEDDING, input)).containsExactly(Map.entry("brideName", "Anu"), Map.entry("groomName", "Rahul"));
    }
    @Test void rejectsUnknownKeysAndUnsafeValues() {
        assertThatThrownBy(() -> OccasionDetails.normalize(OccasionType.WEDDING, Map.of("ssn", "1"))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OccasionDetails.normalize(OccasionType.WEDDING, Map.of("brideName", "<script>"))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OccasionDetails.normalize(OccasionType.WEDDING, Map.of("brideName", "x".repeat(201)))).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void emptyOrMissingDetailsAreAllowed() {
        assertThat(OccasionDetails.normalize(OccasionType.BIRTHDAY, null)).isEmpty();
        assertThat(OccasionDetails.allowedKeys(OccasionType.OTHER)).contains("venueName", "startTime");
    }
    @Test void eventDefaultsKeepTheOriginalGiftingExperience() {
        Event event = new Event("Party", OccasionType.BIRTHDAY);
        assertThat(event.getGiftMode()).isEqualTo(GiftMode.CONTRIBUTION_AND_GIFTS);
        assertThat(event.acceptsGiftItems()).isTrue();
        assertThat(event.getOccasionDetails()).isEmpty();
        event.setGiftMode(GiftMode.CONTRIBUTION_ONLY);
        assertThat(event.acceptsGiftItems()).isFalse();
        event.setOccasionDetails(Map.of("celebrantName", "Maya", "brideName", "Anu"));
        assertThat(event.getOccasionDetails()).containsOnlyKeys("celebrantName");
    }
}
