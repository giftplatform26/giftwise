package com.giftplatform.hostpayment.verification;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity
@Table(name="host_destination_verification_attempts")
public class VerificationAttempt {
 @Id private UUID id;
 @Column(name="host_id",nullable=false,updatable=false) private UUID hostId;
 @Column(name="destination_id",nullable=false,updatable=false) private UUID destinationId;
 @Enumerated(EnumType.STRING) @Column(name="destination_kind",nullable=false,updatable=false) private HostDestinationVerifier.Kind destinationKind;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private VerificationAttemptState state;
 @Column(length=40) private String provider;
 @Column(name="provider_reference",length=120) private String providerReference;
 @Column(name="request_provider",length=40) private String requestProvider;
 @Column(name="request_reference",length=120) private String requestReference;
 @Column(name="request_fund_account_id",length=120) private String requestFundAccountId;
 @Column(name="created_at",insertable=false,updatable=false) private Instant createdAt;
 @Column(name="updated_at",insertable=false,updatable=false) private Instant updatedAt;
 protected VerificationAttempt() {}
 public VerificationAttempt(UUID id,UUID hostId,UUID destinationId,HostDestinationVerifier.Kind kind) {
  if(id==null||hostId==null||destinationId==null||kind==null)throw new IllegalArgumentException("Missing attempt identity");
  this.id=id;this.hostId=hostId;this.destinationId=destinationId;this.destinationKind=kind;this.state=VerificationAttemptState.PENDING;
 }
 public VerificationRequestBinding getRequestBinding(){
  if(requestProvider==null)return null;
  return new VerificationRequestBinding(requestProvider,requestReference,requestFundAccountId);
 }
 public void bindRequest(VerificationRequestBinding binding){
  if(state!=VerificationAttemptState.PENDING)throw new IllegalStateException("Attempt already finalized");
  if(binding==null)throw new IllegalArgumentException("Request binding required");
  if(requestProvider!=null)throw new IllegalStateException("Request already bound");
  requestProvider=binding.provider();
  requestReference=binding.requestReference();
  requestFundAccountId=binding.fundAccountId();
 }
 public UUID getId(){return id;}
 public UUID getHostId(){return hostId;}
 public UUID getDestinationId(){return destinationId;}
 public HostDestinationVerifier.Kind getDestinationKind(){return destinationKind;}
 public VerificationAttemptState getState(){return state;}
 public String getProviderReference(){return providerReference;}
 public void complete(VerificationEvidence evidence){
  if(state!=VerificationAttemptState.PENDING)throw new IllegalStateException("Attempt already finalized");
  if(evidence==null)throw new IllegalArgumentException("Evidence required");
  switch(evidence.outcome()) {
   case VERIFIED -> { if(!evidence.verified()||blank(evidence.provider())||blank(evidence.providerReference()))throw new IllegalArgumentException("Missing verified evidence");state=VerificationAttemptState.VERIFIED;provider=evidence.provider();providerReference=evidence.providerReference(); }
   case REJECTED -> {state=VerificationAttemptState.REJECTED;provider=evidence.provider();providerReference=evidence.providerReference();}
   case UNAVAILABLE -> state=VerificationAttemptState.UNAVAILABLE;
   case PENDING -> throw new IllegalArgumentException("Pending is not a final result");
  }
 }
 private static boolean blank(String s){return s==null||s.isBlank();}
}
