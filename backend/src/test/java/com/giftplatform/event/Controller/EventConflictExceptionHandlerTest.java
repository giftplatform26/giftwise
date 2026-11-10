package com.giftplatform.event.Controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class EventConflictExceptionHandlerTest {
    private final EventConflictExceptionHandler handler = new EventConflictExceptionHandler();

    @Test void hostFacingReasonsAreReturnedForValidationPlanAndConflictErrors() {
        for (HttpStatus status : new HttpStatus[]{HttpStatus.BAD_REQUEST, HttpStatus.PAYMENT_REQUIRED, HttpStatus.CONFLICT}) {
            var response = handler.status(new ResponseStatusException(status, "Add a preferred receiving account"));
            assertThat(response.getStatusCode()).isEqualTo(status);
            assertThat(response.getBody()).isEqualTo(Map.of("error", "Add a preferred receiving account"));
        }
    }

    @Test void otherStatusesKeepAnEmptyBodySoMessagesStayHidden() {
        var notFound = handler.status(new ResponseStatusException(HttpStatus.NOT_FOUND, "Event 123 belongs to another owner"));
        assertThat(notFound.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(notFound.getBody()).isNull();
        var noReason = handler.status(new ResponseStatusException(HttpStatus.CONFLICT));
        assertThat(noReason.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(noReason.getBody()).isNull();
    }
}
