package com.giftplatform.security.recovery;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth/signup-email")
public class SignupEmailVerificationController {
    private final SignupEmailVerificationService service;
    public SignupEmailVerificationController(SignupEmailVerificationService service){this.service=service;}
    public record Start(String accountKind,String email,UUID eventToken,UUID groupId){}
    public record Verify(UUID challengeId,String code){}
    @PostMapping("/start") public SignupEmailVerificationService.Started start(@RequestBody Start body,HttpServletRequest request){
        if(body==null)throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,"Email required");
        return service.start(body.accountKind(),body.email(),body.eventToken(),body.groupId(),request.getRemoteAddr());
    }
    @PostMapping("/verify") public SignupEmailVerificationService.Verified verify(@RequestBody Verify body){if(body==null)throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,"Code required");return service.verify(body.challengeId(),body.code());}
}
