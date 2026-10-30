package com.giftplatform.hostpayment.controller;
import com.giftplatform.hostpayment.service.HostPaymentProfileService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/v1/host/payment-profiles")
@ConditionalOnProperty(name="gift.payment.profiles.enabled",havingValue="true")
public class HostPaymentProfileController {
 private final HostPaymentProfileService service;
 public HostPaymentProfileController(HostPaymentProfileService service){this.service=service;}
 public record AddUpi(String upiId,String accountLabel,UUID eventId) {}
 public record AddBank(String accountNumber,String ifsc,String accountHolderName,String accountLabel,UUID eventId) {}
 @GetMapping("/upi") public List<HostPaymentProfileService.UpiView> upis(@RequestParam(required=false) UUID eventId){return service.listUpis(eventId);}
 @GetMapping("/banks") public List<HostPaymentProfileService.BankView> banks(@RequestParam(required=false) UUID eventId){return service.listBanks(eventId);}
 @PostMapping("/upi") @ResponseStatus(HttpStatus.CREATED) public HostPaymentProfileService.UpiView addUpi(@RequestBody AddUpi body){return service.addUpi(body.upiId(),body.accountLabel(),body.eventId());}
 @PostMapping("/banks") @ResponseStatus(HttpStatus.CREATED) public HostPaymentProfileService.BankView addBank(@RequestBody AddBank body){return service.addBank(body.accountNumber(),body.ifsc(),body.accountHolderName(),body.accountLabel(),body.eventId());}
 @PostMapping("/upi/{id}/host-confirm") public HostPaymentProfileService.UpiView confirmUpi(@PathVariable UUID id){return service.confirmUpi(id);}
 @PostMapping("/banks/{id}/host-confirm") public HostPaymentProfileService.BankView confirmBank(@PathVariable UUID id){return service.confirmBank(id);}
 @PostMapping("/upi/{id}/enable") public HostPaymentProfileService.UpiView enableUpi(@PathVariable UUID id){return service.enableUpi(id);}
 @PostMapping("/upi/{id}/disable") public HostPaymentProfileService.UpiView disableUpi(@PathVariable UUID id){return service.disableUpi(id);}
 @PostMapping("/banks/{id}/enable") public HostPaymentProfileService.BankView enableBank(@PathVariable UUID id){return service.enableBank(id);}
 @PostMapping("/banks/{id}/disable") public HostPaymentProfileService.BankView disableBank(@PathVariable UUID id){return service.disableBank(id);}
 @PostMapping("/upi/{id}/preferred") public HostPaymentProfileService.UpiView preferUpi(@PathVariable UUID id){return service.preferUpi(id);}
 @PostMapping("/banks/{id}/preferred") public HostPaymentProfileService.BankView preferBank(@PathVariable UUID id){return service.preferBank(id);}
 public record RenameAccount(String accountLabel) {}
 @PatchMapping("/upi/{id}/label") public HostPaymentProfileService.UpiView renameUpi(@PathVariable UUID id,@RequestBody RenameAccount body){return service.renameUpi(id,body.accountLabel());}
 @PatchMapping("/banks/{id}/label") public HostPaymentProfileService.BankView renameBank(@PathVariable UUID id,@RequestBody RenameAccount body){return service.renameBank(id,body.accountLabel());}
 @DeleteMapping("/upi/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void archiveUpi(@PathVariable UUID id){service.archiveUpi(id);}
 @DeleteMapping("/banks/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void archiveBank(@PathVariable UUID id){service.archiveBank(id);}
}
