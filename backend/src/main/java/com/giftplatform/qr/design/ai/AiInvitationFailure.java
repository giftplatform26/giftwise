package com.giftplatform.qr.design.ai;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Safe categories for the host UI; provider credentials and response bodies are never returned. */
public class AiInvitationFailure extends ResponseStatusException {
 private final String code;
 public AiInvitationFailure(HttpStatus status,String code,String reason,Throwable cause){super(status,reason,cause);this.code=code;}
 public String code(){return code;}
}
