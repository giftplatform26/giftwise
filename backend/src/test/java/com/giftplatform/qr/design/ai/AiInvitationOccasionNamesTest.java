package com.giftplatform.qr.design.ai;

import com.giftplatform.event.entity.OccasionType;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AiInvitationOccasionNamesTest {
    @Test void featuredNamesComeFromOccasionDetails() {
        assertEquals("Anu & Rahul", AiInvitationService.featuredNames(OccasionType.WEDDING, Map.of("brideName", "Anu", "groomName", "Rahul")));
        assertEquals("Anu", AiInvitationService.featuredNames(OccasionType.WEDDING, Map.of("brideName", "Anu")));
        assertEquals("Maya", AiInvitationService.featuredNames(OccasionType.BIRTHDAY, Map.of("celebrantName", " Maya ")));
        assertEquals("", AiInvitationService.featuredNames(OccasionType.ANNIVERSARY, Map.of()));
    }
}
