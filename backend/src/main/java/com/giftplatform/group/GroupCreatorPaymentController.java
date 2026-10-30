package com.giftplatform.group;

import com.giftplatform.hostpayment.security.PaymentDataProtector;
import com.giftplatform.security.ActorContext;
import com.giftplatform.hostpayment.qr.UpiQrImageCodec;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

/** Creator-owned destinations. Registered is NOT payment-provider verified. */
@RestController
public class GroupCreatorPaymentController {
 private final JdbcTemplate db;
 private final GroupContributionController groups;
 private final ObjectProvider<PaymentDataProtector> protectors;
 private final ActorContext actor;
 public GroupCreatorPaymentController(JdbcTemplate db,GroupContributionController groups,ObjectProvider<PaymentDataProtector> protectors,ActorContext actor){this.db=db;this.groups=groups;this.protectors=protectors;this.actor=actor;}
 private PaymentDataProtector protector(){PaymentDataProtector p=protectors.getIfAvailable();if(p==null)throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Secure payment profiles are not configured");return p;}
 private UUID creator(UUID token,UUID group,HttpServletRequest request){return groups.requireGroupCreator(token,group,request);}
 private ResponseStatusException bad(String message){return new ResponseStatusException(HttpStatus.BAD_REQUEST,message);}
 public record DestinationInput(String kind,String label,String upiId,String accountNumber,String ifsc,String accountHolder){}
 public record SelectInput(UUID destinationId){}
 public record SwitchInput(boolean enabled){}
 public record EnabledInput(boolean enabled){}
 private Map<String,Object> destination(UUID token,UUID groupId,UUID id,HttpServletRequest req){
  creator(token,groupId,req);
  List<Map<String,Object>> rows=db.queryForList("SELECT * FROM gift_group_payment_destinations WHERE id=? AND group_id=? AND status<>'REVOKED'",id,groupId);
  if(rows.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Payment account not found");
  return rows.get(0);
 }
 private void audit(UUID group,UUID actorId,String action,UUID destinationId){
  db.update("INSERT INTO gift_group_payment_audit(id,group_id,actor_type,actor_id,action,destination_id) VALUES(?,?,'CREATOR',?,?,?)",UUID.randomUUID(),group,actorId,action,destinationId);
 }
 @PostMapping("/api/public/groups/{token}/payments/{groupId}/destinations/{id}/enabled") @Transactional
 public Map<String,String> setEnabled(@PathVariable UUID token,@PathVariable UUID groupId,@PathVariable UUID id,@RequestBody EnabledInput input,HttpServletRequest req){
  UUID admin=creator(token,groupId,req);destination(token,groupId,id,req);
  if(input==null)throw bad("Choose whether to enable the account");
  db.update("UPDATE gift_group_payment_destinations SET enabled=?,preferred=CASE WHEN ? THEN preferred ELSE FALSE END WHERE id=? AND group_id=?",input.enabled(),input.enabled(),id,groupId);
  audit(groupId,admin,input.enabled()?"ACCOUNT_ENABLED":"ACCOUNT_DISABLED",id);
  return Map.of("message",input.enabled()?"Account enabled":"Account disabled");
 }
 @PostMapping(value="/api/public/groups/{token}/payments/{groupId}/destinations/{id}/qr/upload",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) @Transactional
 public Map<String,String> uploadQr(@PathVariable UUID token,@PathVariable UUID groupId,@PathVariable UUID id,@RequestParam("file") MultipartFile file,HttpServletRequest req){
  UUID admin=creator(token,groupId,req);Map<String,Object> row=destination(token,groupId,id,req);
  if(!"UPI".equals(row.get("kind")))throw bad("QR images are only available for UPI accounts");
  if(file==null||file.isEmpty()||file.getSize()>UpiQrImageCodec.MAX_UPLOAD_BYTES)throw bad("Please upload a PNG or JPG QR smaller than 2 MB");
  if(!Set.of("image/png","image/jpeg").contains(file.getContentType()))throw bad("Please upload a PNG or JPG QR image");
  try{
   byte[] bytes=file.getBytes();
   String upi=protector().decrypt(groupId,id,"upi_id",(String)row.get("upi_ciphertext"));
   UpiQrImageCodec.validateUpload(bytes,upi);
   // Store a normalized PNG, never an untrusted original upload.
   byte[] png=javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(bytes))==null?null:normalizePng(bytes);
   if(png==null)throw bad("We couldn't read this QR image");
   db.update("UPDATE gift_group_payment_destinations SET qr_image=?,qr_source='UPLOADED' WHERE id=? AND group_id=?",png,id,groupId);
  }catch(java.io.IOException ex){throw bad("We couldn't read this QR image");}
  catch(IllegalArgumentException ex){String msg=ex.getMessage()==null?"":ex.getMessage().toLowerCase(Locale.ROOT);throw bad(msg.contains("match")||msg.contains("payee")?"This QR belongs to a different UPI ID. Please upload the correct QR.":"We couldn't read a matching UPI QR. Try a clearer PNG or JPG image.");}
  audit(groupId,admin,"QR_UPLOADED",id);
  return Map.of("message","UPI QR saved");
 }
 private byte[] normalizePng(byte[] bytes)throws java.io.IOException{
  java.awt.image.BufferedImage image=javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(bytes));
  java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();
  if(image==null||!javax.imageio.ImageIO.write(image,"png",out))throw new java.io.IOException("Cannot normalize QR");
  return out.toByteArray();
 }
 @PostMapping("/api/public/groups/{token}/payments/{groupId}/destinations/{id}/qr/generate") @Transactional
 public Map<String,String> generateQr(@PathVariable UUID token,@PathVariable UUID groupId,@PathVariable UUID id,HttpServletRequest req){
  UUID admin=creator(token,groupId,req);Map<String,Object> row=destination(token,groupId,id,req);
  if(!"UPI".equals(row.get("kind")))throw bad("QR images are only available for UPI accounts");
  String upi=protector().decrypt(groupId,id,"upi_id",(String)row.get("upi_ciphertext"));
  byte[] image=UpiQrImageCodec.generatePng(upi,(String)row.get("label"));
  db.update("UPDATE gift_group_payment_destinations SET qr_image=?,qr_source='GENERATED' WHERE id=? AND group_id=?",image,id,groupId);
  audit(groupId,admin,"QR_GENERATED",id);
  return Map.of("message","UPI QR generated for private preview only");
 }
 @GetMapping(value="/api/public/groups/{token}/payments/{groupId}/destinations/{id}/qr",produces=MediaType.IMAGE_PNG_VALUE)
 public ResponseEntity<byte[]> qr(@PathVariable UUID token,@PathVariable UUID groupId,@PathVariable UUID id,HttpServletRequest req){
  Map<String,Object> row=destination(token,groupId,id,req);
  if(!"UPI".equals(row.get("kind"))||row.get("qr_image")==null)throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No QR saved");
  return ResponseEntity.ok().header("Cache-Control","private, no-store").contentType(MediaType.IMAGE_PNG).body((byte[])row.get("qr_image"));
 }

 private String label(String s){if(s==null||s.isBlank()||s.strip().length()>80)throw bad("Enter an account label (up to 80 characters)");return s.strip();}
 private Map<String,Object> view(Map<String,Object> r){UUID id=(UUID)r.get("id"),group=(UUID)r.get("group_id");String kind=(String)r.get("kind");String masked;
  PaymentDataProtector p=protector();
  if("UPI".equals(kind))masked=PaymentDataProtector.maskUpi(p.decrypt(group,id,"upi_id",(String)r.get("upi_ciphertext")));
  else masked=PaymentDataProtector.maskAccount(p.decrypt(group,id,"account_number",(String)r.get("account_ciphertext")));
  return Map.of("id",id,"kind",kind,"label",r.get("label"),"masked",masked,"status",r.get("status"),"preferred",r.get("preferred"),"enabled",r.get("enabled"),"hasQr",r.get("qr_image")!=null,"qrSource",r.get("qr_source")==null?"NONE":r.get("qr_source"));
 }
 @GetMapping("/api/public/groups/{token}/payments/{groupId}/destinations")
 public Map<String,Object> list(@PathVariable UUID token,@PathVariable UUID groupId,HttpServletRequest request){creator(token,groupId,request);
  List<Map<String,Object>> rows=db.queryForList("SELECT * FROM gift_group_payment_destinations WHERE group_id=? AND status<>'REVOKED' ORDER BY created_at",groupId);
  boolean switchEnabled=Boolean.TRUE.equals(db.query("SELECT switching_enabled FROM gift_group_payment_permissions WHERE group_id=?",rs->rs.next()?rs.getBoolean(1):false,groupId));
  return Map.of("destinations",rows.stream().map(this::view).toList(),"switchingEnabled",switchEnabled,"verificationAvailable",false);
 }
 @PostMapping("/api/public/groups/{token}/payments/{groupId}/destinations") @Transactional
 public Map<String,Object> add(@PathVariable UUID token,@PathVariable UUID groupId,@RequestBody DestinationInput input,HttpServletRequest request){UUID admin=creator(token,groupId,request);PaymentDataProtector p=protector();if(input==null)throw bad("Account details required");UUID id=UUID.randomUUID();String kind=input.kind(),accountLabel=label(input.label());
  try{
   if("UPI".equals(kind)){
    String upi;try{upi=PaymentDataProtector.normalizeUpi(input.upiId());}catch(IllegalArgumentException ex){throw bad("Please enter a valid UPI ID, such as name@bank");}
    db.update("INSERT INTO gift_group_payment_destinations(id,group_id,kind,label,upi_ciphertext,upi_fingerprint) VALUES(?,?,'UPI',?,?,?)",id,groupId,accountLabel,p.encrypt(groupId,id,"upi_id",upi),p.fingerprint("UPI",upi));
   }else if("BANK".equals(kind)){
    String account,ifsc;try{account=PaymentDataProtector.normalizeAccountNumber(input.accountNumber());}catch(IllegalArgumentException ex){throw bad("Please enter a valid bank account number");}try{ifsc=PaymentDataProtector.normalizeIfsc(input.ifsc());}catch(IllegalArgumentException ex){throw bad("Please check the 11-character IFSC code");}String holder=input.accountHolder()==null?"":input.accountHolder().strip();
    if(holder.length()<2||holder.length()>120||holder.chars().anyMatch(Character::isISOControl))throw bad("Enter the account holder name");
    db.update("INSERT INTO gift_group_payment_destinations(id,group_id,kind,label,account_ciphertext,account_fingerprint,ifsc_ciphertext,holder_ciphertext) VALUES(?,?,'BANK',?,?,?,?,?)",id,groupId,accountLabel,p.encrypt(groupId,id,"account_number",account),p.fingerprint("BANK",account+":"+ifsc),p.encrypt(groupId,id,"ifsc",ifsc),p.encrypt(groupId,id,"holder",holder));
   }else throw bad("Choose UPI or bank transfer");
  }catch(org.springframework.dao.DuplicateKeyException ex){throw new ResponseStatusException(HttpStatus.CONFLICT,"This account is already registered for this group");}
  db.update("INSERT INTO gift_group_payment_audit(id,group_id,actor_type,actor_id,action,destination_id) VALUES(?,?,'CREATOR',?,'DESTINATION_REGISTERED',?)",UUID.randomUUID(),groupId,admin,id);
  return view(db.queryForMap("SELECT * FROM gift_group_payment_destinations WHERE id=?",id));
 }
 @PostMapping("/api/public/groups/{token}/payments/{groupId}/preferred") @Transactional
 public Map<String,String> select(@PathVariable UUID token,@PathVariable UUID groupId,@RequestBody SelectInput input,HttpServletRequest request){UUID admin=creator(token,groupId,request);
  if(input==null||input.destinationId()==null)throw bad("Select an account");
  db.queryForObject("SELECT id FROM gift_groups WHERE id=? FOR UPDATE",UUID.class,groupId);
  Boolean allowed=db.query("SELECT switching_enabled FROM gift_group_payment_permissions WHERE group_id=?",rs->rs.next()?rs.getBoolean(1):false,groupId);
  if(!Boolean.TRUE.equals(allowed))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Account switching requires platform administrator permission");
  Integer n=db.queryForObject("SELECT count(*) FROM gift_group_payment_destinations WHERE id=? AND group_id=? AND status='VERIFIED' AND enabled=TRUE",Integer.class,input.destinationId(),groupId);
  if(n==null||n!=1)throw new ResponseStatusException(HttpStatus.CONFLICT,"Only independently verified accounts may be selected for payment");
  db.update("UPDATE gift_group_payment_destinations SET preferred=FALSE WHERE group_id=? AND kind=(SELECT kind FROM gift_group_payment_destinations WHERE id=?)",groupId,input.destinationId());
  db.update("UPDATE gift_group_payment_destinations SET preferred=TRUE WHERE id=? AND group_id=?",input.destinationId(),groupId);
  db.update("INSERT INTO gift_group_payment_audit(id,group_id,actor_type,actor_id,action,destination_id) VALUES(?,?,'CREATOR',?,'PREFERRED_CHANGED',?)",UUID.randomUUID(),groupId,admin,input.destinationId());
  return Map.of("message","Preferred destination updated");
 }
 @PostMapping("/api/admin/group-payments/{groupId}/switching") @Transactional
 public Map<String,Object> adminSwitch(@PathVariable UUID groupId,@RequestBody SwitchInput input){if(!actor.isPlatformAdmin())throw new ResponseStatusException(HttpStatus.FORBIDDEN);UUID id=actor.requireAuthenticatedUserId();
  Integer n=db.queryForObject("SELECT count(*) FROM gift_groups WHERE id=?",Integer.class,groupId);if(n==null||n!=1)throw new ResponseStatusException(HttpStatus.NOT_FOUND);
  db.update("INSERT INTO gift_group_payment_permissions(group_id,switching_enabled,changed_by) VALUES(?,?,?) ON CONFLICT(group_id) DO UPDATE SET switching_enabled=EXCLUDED.switching_enabled,changed_by=EXCLUDED.changed_by,changed_at=now()",groupId,input.enabled(),id);
  db.update("INSERT INTO gift_group_payment_audit(id,group_id,actor_type,actor_id,action) VALUES(?,?,'PLATFORM_ADMIN',?,?)",UUID.randomUUID(),groupId,id,input.enabled()?"SWITCHING_ENABLED":"SWITCHING_DISABLED");
  return Map.of("groupId",groupId,"switchingEnabled",input.enabled());
 }
}
