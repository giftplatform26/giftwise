package com.giftplatform.hostpayment.service;
import com.giftplatform.hostpayment.entity.*;
import com.giftplatform.hostpayment.repository.*;
import com.giftplatform.hostpayment.security.PaymentDataProtector;
import com.giftplatform.hostpayment.qr.UpiQrImageStorage;
import com.giftplatform.security.ActorContext;
import com.giftplatform.user.repository.UserRepository;
import com.giftplatform.audit.service.AuditService;
import com.giftplatform.audit.entity.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.*;
@Service
@ConditionalOnProperty(name="gift.payment.profiles.enabled",havingValue="true")
public class HostPaymentProfileService {
 @org.springframework.beans.factory.annotation.Autowired private com.giftplatform.event.repository.EventRepository events;
 @org.springframework.beans.factory.annotation.Autowired private EventAccountLinkService eventLinks;
 private void validateEvent(UUID hostId,UUID eventId){if(eventId!=null && events.findById(eventId).filter(e->hostId.equals(e.getOwnerId())).isEmpty())throw new NoSuchElementException("Event not found");}
 private final HostUpiAccountRepository upis; private final HostBankAccountRepository banks;
 private final UpiQrImageStorage qrStorage; private final PaymentDataProtector protector; private final ActorContext actor; private final UserRepository users; private final AuditService audit;
 @org.springframework.beans.factory.annotation.Autowired public HostPaymentProfileService(HostUpiAccountRepository upis,HostBankAccountRepository banks,UpiQrImageStorage qrStorage,PaymentDataProtector protector,ActorContext actor,UserRepository users,AuditService audit){this.upis=upis;this.banks=banks;this.qrStorage=qrStorage;this.protector=protector;this.actor=actor;this.users=users;this.audit=audit;}
 public record UpiView(UUID id,String masked,VerificationStatus verificationStatus,boolean enabled,boolean preferred,boolean hostConfirmed,String accountLabel,boolean qrReady,UUID hostId,UUID eventId) {}
 public record BankView(UUID id,String accountMasked,String ifscMasked,VerificationStatus verificationStatus,boolean enabled,boolean preferred,boolean hostConfirmed,String accountLabel,UUID hostId,UUID eventId) {}
 private UUID host(){UUID id=actor.requireAuthenticatedUserId();if(!users.findById(id).filter(u -> u.isEnabled() && u.getRole() == com.giftplatform.user.entity.UserRole.CUSTOMER).isPresent())throw new IllegalStateException("Authenticated host is not eligible");return id;}
 private void audit(UUID hostId,String entity,UUID id,AuditAction action,String status){audit.recordChange(hostId,AuditActorType.valueOf("CUSTOMER"),entity,id,action,null,Map.of("status",status),null);}
 private UpiView view(HostUpiAccount a){return new UpiView(a.getId(),a.getMasked(),a.getVerificationStatus(),a.isEnabled(),a.isPreferred(),a.getHostConfirmedAt()!=null,a.getAccountLabel(),qrStorage.hasValidQr(a.getHostId(),a.getId()),a.getHostId(),a.getEventId());}
 private static BankView view(HostBankAccount a){return new BankView(a.getId(),a.getAccountMasked(),a.getIfscMasked(),a.getVerificationStatus(),a.isEnabled(),a.isPreferred(),a.getHostConfirmedAt()!=null,a.getAccountLabel(),a.getHostId(),a.getEventId());}
 @Transactional(readOnly=true) public List<UpiView> listUpis(){return listUpis(null);}
 @Transactional(readOnly=true) public List<BankView> listBanks(){return listBanks(null);}
 @Transactional(readOnly=true) public List<UpiView> listUpis(UUID eventId){UUID hostId=host();validateEvent(hostId,eventId);if(eventId!=null)return eventLinks.upis(eventId);return upis.findByHostIdOrderByCreatedAtAsc(hostId).stream().filter(a -> !a.isArchived() && Objects.equals(a.getEventId(),eventId)).map(this::view).toList();}
 @Transactional(readOnly=true) public List<BankView> listBanks(UUID eventId){UUID hostId=host();validateEvent(hostId,eventId);if(eventId!=null)return eventLinks.banks(eventId);return banks.findByHostIdOrderByCreatedAtAsc(hostId).stream().filter(a -> !a.isArchived() && Objects.equals(a.getEventId(),eventId)).map(HostPaymentProfileService::view).toList();}
 @Transactional public UpiView addUpi(String input,String label){return addUpi(input,label,null);}
 @Transactional public UpiView addUpi(String input,String label,UUID eventId){UUID hostId=host();validateEvent(hostId,eventId);String upi=PaymentDataProtector.normalizeUpi(input);String fingerprint=protector.fingerprint("UPI",upi);if(upis.existsByHostIdAndFingerprintAndEventIdAndArchivedAtIsNull(hostId,fingerprint,eventId))throw new IllegalArgumentException("Destination already registered");UUID id=UUID.randomUUID();var account=new HostUpiAccount(id,hostId,protector.encrypt(hostId,id,"upi_id",upi),fingerprint,PaymentDataProtector.maskUpi(upi));account.assignEvent(eventId);account.rename(normalizeLabel(label));upis.saveAndFlush(account);if(eventId!=null)eventLinks.link(eventId,"UPI",id);audit(hostId,"HOST_UPI_ACCOUNT",id,AuditAction.CREATE,"PENDING");return view(account);}
 @Transactional public BankView addBank(String number,String ifsc,String holder,String label){return addBank(number,ifsc,holder,label,null);}
 @Transactional public BankView addBank(String number,String ifsc,String holder,String label,UUID eventId){UUID hostId=host();validateEvent(hostId,eventId);String account=PaymentDataProtector.normalizeAccountNumber(number);String bankIfsc=PaymentDataProtector.normalizeIfsc(ifsc);if(holder==null||holder.isBlank()||holder.length()>120||holder.chars().anyMatch(Character::isISOControl))throw new IllegalArgumentException("Invalid account holder name");String name=holder.trim();String fingerprint=protector.fingerprint("BANK",bankIfsc+":"+account);if(banks.existsByHostIdAndFingerprintAndEventIdAndArchivedAtIsNull(hostId,fingerprint,eventId))throw new IllegalArgumentException("Destination already registered");UUID id=UUID.randomUUID();var record=new HostBankAccount(id,hostId,protector.encrypt(hostId,id,"account_number",account),fingerprint,PaymentDataProtector.maskAccount(account),protector.encrypt(hostId,id,"ifsc",bankIfsc),bankIfsc.substring(0,4)+"*******",protector.encrypt(hostId,id,"account_holder",name));record.assignEvent(eventId);record.rename(normalizeLabel(label));banks.saveAndFlush(record);if(eventId!=null)eventLinks.link(eventId,"BANK",id);audit(hostId,"HOST_BANK_ACCOUNT",id,AuditAction.CREATE,"PENDING");return view(record);}
 @Transactional public UpiView confirmUpi(UUID id){UUID hostId=host();var a=upis.findByIdAndHostId(id,hostId).orElseThrow(()->new NoSuchElementException("Destination not found"));requireQr(hostId,id);a.confirmByHost();audit(hostId,"HOST_UPI_ACCOUNT",id,AuditAction.UPDATE,"HOST_CONFIRMED_NOT_VERIFIED");return view(a);}
 @Transactional public BankView confirmBank(UUID id){UUID hostId=host();var a=banks.findByIdAndHostId(id,hostId).orElseThrow(()->new NoSuchElementException("Destination not found"));a.confirmByHost();audit(hostId,"HOST_BANK_ACCOUNT",id,AuditAction.UPDATE,"HOST_CONFIRMED_NOT_VERIFIED");return view(a);}
 @Transactional public UpiView enableUpi(UUID id){UUID hostId=host();var a=upis.findByIdAndHostId(id,hostId).orElseThrow(()->new NoSuchElementException("Destination not found"));if(!a.isEnabled()){a.enable();audit(hostId,"HOST_UPI_ACCOUNT",id,AuditAction.STATUS_CHANGE,"ENABLED_NOT_REVERIFIED");}return view(a);}
 @Transactional public UpiView disableUpi(UUID id){UUID hostId=host();var a=upis.findByIdAndHostId(id,hostId).orElseThrow(()->new NoSuchElementException("Destination not found"));a.disable();audit(hostId,"HOST_UPI_ACCOUNT",id,AuditAction.STATUS_CHANGE,"DISABLED");return view(a);}
 @Transactional public BankView enableBank(UUID id){UUID hostId=host();var a=banks.findByIdAndHostId(id,hostId).orElseThrow(()->new NoSuchElementException("Destination not found"));if(!a.isEnabled()){a.enable();audit(hostId,"HOST_BANK_ACCOUNT",id,AuditAction.STATUS_CHANGE,"ENABLED_NOT_REVERIFIED");}return view(a);}
 @Transactional public BankView disableBank(UUID id){UUID hostId=host();var a=banks.findByIdAndHostId(id,hostId).orElseThrow(()->new NoSuchElementException("Destination not found"));a.disable();audit(hostId,"HOST_BANK_ACCOUNT",id,AuditAction.STATUS_CHANGE,"DISABLED");return view(a);}
 // Serialize preference changes per host using the users row, then flush to enforce partial unique indexes.
 @Transactional public UpiView preferUpi(UUID id){UUID hostId=host();users.findForPhoneEnrollment(hostId).orElseThrow();var selected=upis.findByIdAndHostId(id,hostId).orElseThrow(()->new NoSuchElementException("Destination not found"));if(selected.getEventId()!=null){eventLinks.update(selected.getEventId(),"UPI",id,"preferred");return eventLinks.upis(selected.getEventId()).stream().filter(a->a.id().equals(id)).findFirst().orElseThrow();}requireQr(hostId,id);for(var a:upis.findByHostIdOrderByCreatedAtAsc(hostId))if(!a.getId().equals(id)&&Objects.equals(a.getEventId(),selected.getEventId()))a.clearPreferred();upis.flush();selected.prefer();upis.flush();audit(hostId,"HOST_UPI_ACCOUNT",id,AuditAction.UPDATE,"PREFERRED");return view(selected);}
 @Transactional public BankView preferBank(UUID id){UUID hostId=host();users.findForPhoneEnrollment(hostId).orElseThrow();var selected=banks.findByIdAndHostId(id,hostId).orElseThrow(()->new NoSuchElementException("Destination not found"));if(selected.getEventId()!=null){eventLinks.update(selected.getEventId(),"BANK",id,"preferred");return eventLinks.banks(selected.getEventId()).stream().filter(a->a.id().equals(id)).findFirst().orElseThrow();}for(var a:banks.findByHostIdOrderByCreatedAtAsc(hostId))if(!a.getId().equals(id)&&Objects.equals(a.getEventId(),selected.getEventId()))a.clearPreferred();banks.flush();selected.prefer();banks.flush();audit(hostId,"HOST_BANK_ACCOUNT",id,AuditAction.UPDATE,"PREFERRED");return view(selected);}
 // Compatibility for isolated legacy unit tests; Spring uses the annotated constructor.
 public HostPaymentProfileService(HostUpiAccountRepository u,HostBankAccountRepository b,PaymentDataProtector p,ActorContext a,UserRepository r,AuditService audit){this(u,b,new UpiQrImageStorage(){public void saveUploaded(UUID h,UUID i,byte[] png,String d){} public void saveGenerated(UUID h,UUID i,String d){} public byte[] getUploaded(UUID h,UUID i){return null;} public boolean hasUploaded(UUID h,UUID i){return false;} public boolean hasValidQr(UUID h,UUID i){return false;}},p,a,r,audit); }
 public UpiView addUpi(String input){return addUpi(input,null); }
 public BankView addBank(String n,String i,String h){return addBank(n,i,h,null); }

 private static String normalizeLabel(String label){
   if(label==null||label.isEmpty())return null;
   String cleaned=label.strip();
   if(cleaned.isEmpty()||cleaned.length()>50||cleaned.chars().anyMatch(Character::isISOControl))throw new IllegalArgumentException("Account name must be 1-50 visible characters");
   return cleaned;
 }
 private void requireQr(UUID hostId,UUID id){
   if(!qrStorage.hasValidQr(hostId,id))throw new IllegalStateException("Upload or generate a valid UPI QR before confirming or selecting Preferred");
 }
 @Transactional public UpiView renameUpi(UUID id,String label){UUID h=host();var a=upis.findByIdAndHostId(id,h).orElseThrow();a.rename(normalizeLabel(label));audit(h,"HOST_UPI_ACCOUNT",id,AuditAction.UPDATE,"ACCOUNT_RENAMED");return view(a);}
 @Transactional public BankView renameBank(UUID id,String label){UUID h=host();var a=banks.findByIdAndHostId(id,h).orElseThrow();a.rename(normalizeLabel(label));audit(h,"HOST_BANK_ACCOUNT",id,AuditAction.UPDATE,"ACCOUNT_RENAMED");return view(a);}
 @Transactional public void archiveUpi(UUID id){UUID h=host();users.findForPhoneEnrollment(h).orElseThrow();var a=upis.findByIdAndHostId(id,h).orElseThrow();a.archive();upis.flush();audit(h,"HOST_UPI_ACCOUNT",id,AuditAction.STATUS_CHANGE,"ACCOUNT_ARCHIVED");}
 @Transactional public void archiveBank(UUID id){UUID h=host();users.findForPhoneEnrollment(h).orElseThrow();var a=banks.findByIdAndHostId(id,h).orElseThrow();a.archive();banks.flush();audit(h,"HOST_BANK_ACCOUNT",id,AuditAction.STATUS_CHANGE,"ACCOUNT_ARCHIVED");}

}
