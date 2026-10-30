package com.giftplatform.hostpayment.entity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="host_bank_accounts")
public class HostBankAccount {
 @Id private UUID id;
 @Column(name="event_id",updatable=false) private UUID eventId;
 public UUID getEventId(){return eventId;}
 public void assignEvent(UUID eventId){this.eventId=eventId;}
 @Column(name="host_id",nullable=false,updatable=false) private UUID hostId;
 @Column(name="account_number_encrypted",nullable=false) private String accountEncrypted;
 @Column(name="account_fingerprint",nullable=false,length=64) private String fingerprint;
 @Column(name="account_number_masked",nullable=false,length=32) private String accountMasked;
 @Column(name="ifsc_encrypted",nullable=false) private String ifscEncrypted;
 @Column(name="ifsc_masked",nullable=false,length=20) private String ifscMasked;
 @Column(name="account_holder_encrypted",nullable=false) private String holderEncrypted;
 @Enumerated(EnumType.STRING) @Column(name="verification_status",nullable=false) private VerificationStatus verificationStatus;
 @Column(nullable=false) private boolean enabled;
 @Column(nullable=false) private boolean preferred;
 @Column(name="host_confirmed_at") private Instant hostConfirmedAt;
 @Column(name="account_label",length=50) private String accountLabel;
 @Column(name="archived_at") private Instant archivedAt;
 @Version private long version;
 @Column(name="created_at",insertable=false,updatable=false) private Instant createdAt;
 @Column(name="updated_at",insertable=false,updatable=false) private Instant updatedAt;
 protected HostBankAccount() {}
 public HostBankAccount(UUID id,UUID hostId,String accountEncrypted,String fingerprint,String accountMasked,String ifscEncrypted,String ifscMasked,String holderEncrypted){this.id=id;this.hostId=hostId;this.accountEncrypted=accountEncrypted;this.fingerprint=fingerprint;this.accountMasked=accountMasked;this.ifscEncrypted=ifscEncrypted;this.ifscMasked=ifscMasked;this.holderEncrypted=holderEncrypted;this.verificationStatus=VerificationStatus.PENDING;this.enabled=true;}
 public UUID getId(){return id;} public UUID getHostId(){return hostId;} public String getAccountMasked(){return accountMasked;} public String getIfscMasked(){return ifscMasked;}
 public VerificationStatus getVerificationStatus(){return verificationStatus;} public boolean isEnabled(){return enabled;} public boolean isPreferred(){return preferred;}
 public Instant getHostConfirmedAt(){return hostConfirmedAt;}
 public String getAccountLabel(){return accountLabel;}
 public Instant getArchivedAt(){return archivedAt;}
 public boolean isArchived(){return archivedAt!=null;}
 public void rename(String label){if(isArchived())throw new IllegalStateException("Archived account cannot be renamed");accountLabel=label;}
 public void archive(){if(isArchived())throw new IllegalStateException("Account already archived");enabled=false;preferred=false;archivedAt=Instant.now();}

 public void confirmByHost(){if(!enabled||isArchived())throw new IllegalStateException("Disabled or archived destination cannot be confirmed");hostConfirmedAt=Instant.now();}
 public void enable(){if(isArchived())throw new IllegalStateException("Archived account cannot be enabled");if(verificationStatus==VerificationStatus.REVOKED)throw new IllegalStateException("Revoked destination cannot be enabled");enabled=true;preferred=false;} public void disable(){enabled=false;preferred=false;} public void clearPreferred(){preferred=false;}
 public void prefer(){if(!enabled||isArchived()||(verificationStatus!=VerificationStatus.VERIFIED&&hostConfirmedAt==null))throw new IllegalStateException("Destination must be active and host-confirmed or provider-verified");preferred=true;}
 public void applyTrustedVerification(VerificationStatus result){if(result==null||result==VerificationStatus.PENDING)throw new IllegalArgumentException("Invalid verification result");if(!enabled)throw new IllegalStateException("Disabled destination cannot be verified");if(verificationStatus==VerificationStatus.REVOKED)throw new IllegalStateException("Revoked destination cannot be reverified");verificationStatus=result;if(result!=VerificationStatus.VERIFIED)preferred=false;}
}
