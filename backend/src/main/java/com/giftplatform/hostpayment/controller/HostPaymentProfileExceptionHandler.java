package com.giftplatform.hostpayment.controller;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.*;
@RestControllerAdvice(assignableTypes=HostPaymentProfileController.class)
@ConditionalOnProperty(name="gift.payment.profiles.enabled",havingValue="true")
public class HostPaymentProfileExceptionHandler {
 @ExceptionHandler(IllegalArgumentException.class) ResponseEntity<Map<String,String>> invalid(){return ResponseEntity.badRequest().body(Map.of("error","Invalid or duplicate payment destination"));}
 @ExceptionHandler(DataIntegrityViolationException.class) ResponseEntity<Map<String,String>> conflict(){return ResponseEntity.status(409).body(Map.of("error","Payment destination conflict"));}
 @ExceptionHandler(NoSuchElementException.class) ResponseEntity<Map<String,String>> missing(){return ResponseEntity.status(404).body(Map.of("error","Destination not found"));}
 @ExceptionHandler(IllegalStateException.class) ResponseEntity<Map<String,String>> unavailable(){return ResponseEntity.status(409).body(Map.of("error","Destination is not eligible for this operation"));}
}
