
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

    @ExceptionHandler(com.giftplatform.user.service.DuplicateUsernameException.class)
    public ResponseEntity<Map<String, String>> duplicateUsername() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Username is already registered. Please choose another username."));
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> invalidFields(org.springframework.web.bind.MethodArgumentNotValidException ex) {
        var fields = new java.util.TreeMap<String, String>();
        for (var error : ex.getBindingResult().getFieldErrors())
            fields.putIfAbsent(error.getField(), error.getField() + ": " + error.getDefaultMessage());
        return ResponseEntity.badRequest().body(Map.of("error", String.join(" ", fields.values())));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> invalidInput(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(Map.of(
                        "error", ex.getMessage()
                ));
    }
}
