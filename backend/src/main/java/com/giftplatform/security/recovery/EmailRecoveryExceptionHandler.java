package com.giftplatform.security.recovery;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

/** These endpoints use explicit, safe messages; never returns provider response bodies or stack traces. */
@RestControllerAdvice(assignableTypes={EmailRecoveryController.class,SignupEmailVerificationController.class,com.giftplatform.user.controller.RegistrationController.class})
public class EmailRecoveryExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String,String>> failure(ResponseStatusException ex){
        String message=ex.getReason()==null?"The request could not be completed. Please try again.":ex.getReason();
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("message",message));
    }
}
