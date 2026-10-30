
package com.giftplatform.user.controller;

import com.giftplatform.user.service.DuplicateEmailException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(
        assignableTypes = RegistrationController.class
)
public class RegistrationExceptionHandler {

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Map<String, String>> duplicateEmail() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "error", "Email is already registered"
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> invalidInput() {
        return ResponseEntity.badRequest()
                .body(Map.of(
                        "error", "Invalid registration data"
                ));
    }
}
