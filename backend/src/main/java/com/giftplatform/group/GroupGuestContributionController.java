package com.giftplatform.group;

import com.giftplatform.hostpayment.security.PaymentDataProtector;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import java.util.regex.Pattern;

/** Guest-reported transfers. These records are never verified financial postings. */
@RestController
@RequestMapping("/api/public/groups/{token}/guest/{groupId}")
public class GroupGuestContributionController {
 private static final java.util.concurrent.ConcurrentHashMap<String,java.util.ArrayDeque<Long>> REPORT_ATTEMPTS=new java.util.concurrent.ConcurrentHashMap<>();
 private static void limit(String key){long now=System.currentTimeMillis();if(REPORT_ATTEMPTS.size()>10000)REPORT_ATTEMPTS.entrySet().removeIf(e->{synchronized(e.getValue()){return e.getValue().isEmpty()||now-e.getValue().peekLast()>600000;}});var q=REPORT_ATTEMPTS.computeIfAbsent(key,k->new java.util.ArrayDeque<>());synchronized(q){while(!q.isEmpty()&&now-q.peekFirst()>600000)q.removeFirst();if(q.size()>=10)throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,"Too many payment reports. Try again later");q.addLast(now);}}
 private final JdbcTemplate db;
 private final GroupContributionController groups;
 private final ObjectProvider<PaymentDataProtector> protectors;
 public GroupGuestContributionController(JdbcTemplate db,GroupContributionController groups,ObjectProvider<PaymentDataProtector> protectors){this.db=db;this.groups=groups;this.protectors=protectors;}
 private static final Pattern REFERENCE=Pattern.compile("[A-Za-z0-9][A-Za-z0-9 /_.-]{5,119}");
 private ResponseStatusException bad(String reason){return new ResponseStatusException(HttpStatus.BAD_REQUEST,reason);}
 private PaymentDataProtector protector(){PaymentDataProtector p=protectors.getIfAvailable();if(p==null)throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Secure payment configuration is unavailable");return p;}
 private Map<String,Object> group(UUID token,UUID id){groups.publicGroup(token,id);return db.queryForMap("SELECT g.id,g.event_id,g.name,g.creator_name,g.description,g.gift_name,g.suggested_contribution_paise,g.manual_contributions_enabled,g.creator_direct_payments_enabled,g.contribution_access_mode,e.title AS event_title,e.event_date FROM gift_groups g JOIN events e ON e.id=g.event_id WHERE g.id=?",id);}
 private String accessMode(Map<String,Object> g){return (String)g.get("contribution_access_mode");}
 private boolean guestsAllowed(Map<String,Object> g){return !"MEMBERS_ONLY".equals(accessMode(g));}
 private boolean membersAllowed(Map<String,Object> g){return !"GUESTS_ONLY".equals(accessMode(g));}
 private UUID approvedMember(UUID groupId,HttpServletRequest req){return groups.member(groupId,req);}
 private List<Map<String,Object>> choices(UUID groupId){
  return db.queryForList("SELECT id,kind,label,qr_image IS NOT NULL AS has_qr FROM gift_group_payment_destinations WHERE group_id=? AND enabled=TRUE AND status IN ('REGISTERED','VERIFIED') AND id IN (SELECT guest_upi_destination_id FROM gift_groups WHERE id=? UNION SELECT guest_bank_destination_id FROM gift_groups WHERE id=?) ORDER BY kind",groupId,groupId,groupId);
 }
 private Map<String,Object> chosen(UUID groupId,UUID destinationId){
  List<Map<String,Object>> rows=db.queryForList("SELECT * FROM gift_group_payment_destinations WHERE id=? AND group_id=? AND enabled=TRUE AND id IN (SELECT guest_upi_destination_id FROM gift_groups WHERE id=? UNION SELECT guest_bank_destination_id FROM gift_groups WHERE id=?) AND status IN ('REGISTERED','VERIFIED')",destinationId,groupId,groupId,groupId);
  if(rows.isEmpty())throw new ResponseStatusException(HttpStatus.CONFLICT,"This payment destination is no longer available");return rows.get(0);
 }
 @GetMapping("/invitation")
 public Map<String,Object> invitation(@PathVariable UUID token,@PathVariable UUID groupId){
  Map<String,Object> g=group(token,groupId);boolean enabled=Boolean.TRUE.equals(g.get("manual_contributions_enabled"));
  Map<String,Object> out=new LinkedHashMap<>();for(String k:List.of("name","creator_name","description","gift_name","event_title","event_date","suggested_contribution_paise"))out.put(k,g.get(k));
  out.put("contributionsEnabled",enabled&&!choices(groupId).isEmpty());out.put("accessMode",accessMode(g));
  out.put("methods",enabled?choices(groupId):List.of());return out;
 }
 @GetMapping("/destination/{id}")
 public Map<String,Object> destination(@PathVariable UUID token,@PathVariable UUID groupId,@PathVariable UUID id){
  Map<String,Object> g=group(token,groupId);if(!Boolean.TRUE.equals(g.get("manual_contributions_enabled")))throw new ResponseStatusException(HttpStatus.CONFLICT,"Contributions are not enabled");
  Map<String,Object> row=chosen(groupId,id);String kind=(String)row.get("kind");PaymentDataProtector p=protector();Map<String,Object> out=new LinkedHashMap<>();out.put("id",id);out.put("kind",kind);out.put("label",row.get("label"));
  if("UPI".equals(kind)){out.put("hasQr",row.get("qr_image")!=null);}else{
   out.put("accountNumber",p.decrypt(groupId,id,"account_number",(String)row.get("account_ciphertext")));
   out.put("ifsc",p.decrypt(groupId,id,"ifsc",(String)row.get("ifsc_ciphertext")));
   out.put("accountHolder",p.decrypt(groupId,id,"holder",(String)row.get("holder_ciphertext")));
  }
  out.put("verificationNotice","Manual transfer instructions; payment receipt is not automatically verified");return out;
 }
 @GetMapping(value="/destination/{id}/qr",produces=MediaType.IMAGE_PNG_VALUE)
 public ResponseEntity<byte[]> qr(@PathVariable UUID token,@PathVariable UUID groupId,@PathVariable UUID id){
  Map<String,Object> g=group(token,groupId);if(!Boolean.TRUE.equals(g.get("manual_contributions_enabled")))throw new ResponseStatusException(HttpStatus.CONFLICT);
  Map<String,Object> d=chosen(groupId,id);if(!"UPI".equals(d.get("kind"))||d.get("qr_image")==null)throw new ResponseStatusException(HttpStatus.NOT_FOUND);
  return ResponseEntity.ok().cacheControl(CacheControl.noStore()).contentType(MediaType.IMAGE_PNG).body((byte[])d.get("qr_image"));
 }
 public record Report(UUID destinationId,String guestName,String contact,Long amountPaise,String transactionReference,UUID submissionKey){}
 @GetMapping("/member-history")
 public List<Map<String,Object>> memberHistory(@PathVariable UUID token,@PathVariable UUID groupId,HttpServletRequest request){Map<String,Object> g=group(token,groupId);if(!membersAllowed(g))throw new ResponseStatusException(HttpStatus.FORBIDDEN);UUID id=approvedMember(groupId,request);return db.queryForList("SELECT id,amount_paise,method,transaction_reference,state,created_at FROM gift_group_guest_reports WHERE group_id=? AND member_id=? ORDER BY created_at DESC LIMIT 100",groupId,id);}
 @PostMapping("/member-report") @Transactional
 public Map<String,Object> memberReport(@PathVariable UUID token,@PathVariable UUID groupId,@RequestBody Report input,HttpServletRequest request){Map<String,Object> g=group(token,groupId);if(!membersAllowed(g))throw new ResponseStatusException(HttpStatus.FORBIDDEN);UUID id=approvedMember(groupId,request);String name=db.queryForObject("SELECT display_name FROM gift_group_members WHERE id=?",String.class,id);if(input==null)throw bad("Complete the contribution details");return recordReport(token,groupId,new Report(input.destinationId(),name,null,input.amountPaise(),input.transactionReference(),input.submissionKey()),request,id);}
 @PostMapping("/report") @Transactional
 public Map<String,Object> report(@PathVariable UUID token,@PathVariable UUID groupId,@RequestBody Report input,HttpServletRequest request){
  Map<String,Object> g=group(token,groupId);if(!guestsAllowed(g))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Guest contributions are disabled");return recordReport(token,groupId,input,request,null);
 }
 @GetMapping("/creator-history")
 public List<Map<String,Object>> creatorHistory(@PathVariable UUID token,@PathVariable UUID groupId,HttpServletRequest request){
  UUID creator=groups.requireGroupCreator(token,groupId,request);
  return db.queryForList("SELECT id,amount_paise,method,transaction_reference,state,created_at FROM gift_group_guest_reports WHERE group_id=? AND creator_admin_id=? ORDER BY created_at DESC LIMIT 100",groupId,creator);
 }
 @PostMapping("/creator-report") @Transactional
 public Map<String,Object> creatorReport(@PathVariable UUID token,@PathVariable UUID groupId,@RequestBody Report input,HttpServletRequest request){
  UUID creator=groups.requireGroupCreator(token,groupId,request);
  if(input==null)throw bad("Complete the contribution details");
  String name=db.queryForObject("SELECT creator_name FROM gift_groups WHERE id=? AND admin_id=?",String.class,groupId,creator);
  return recordReport(token,groupId,new Report(input.destinationId(),name,null,input.amountPaise(),input.transactionReference(),input.submissionKey()),request,null,creator);
 }
 private Map<String,Object> recordReport(UUID token,UUID groupId,Report input,HttpServletRequest request,UUID memberId){
  return recordReport(token,groupId,input,request,memberId,null);
 }
 private Map<String,Object> recordReport(UUID token,UUID groupId,Report input,HttpServletRequest request,UUID memberId,UUID creatorId){
  limit(groupId+"|"+request.getRemoteAddr());
  Map<String,Object> g=group(token,groupId);if(!Boolean.TRUE.equals(g.get("manual_contributions_enabled")))throw new ResponseStatusException(HttpStatus.CONFLICT,"Contributions are not enabled");
  if(input==null||input.destinationId()==null||input.submissionKey()==null||input.amountPaise()==null)throw bad("Complete the contribution details");
  String name=input.guestName()==null?"":input.guestName().strip();String contact=input.contact()==null?"":input.contact().strip();String reference=input.transactionReference()==null?"":input.transactionReference().strip();
  if(name.length()<2||name.length()>120||name.chars().anyMatch(Character::isISOControl))throw bad("Enter your name");
  if(contact.length()>120||contact.chars().anyMatch(Character::isISOControl))throw bad("Invalid contact details");
  if(memberId==null && creatorId==null && !(contact.matches("\\+?[0-9][0-9 -]{7,19}") || contact.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")))throw bad("Enter a valid mobile number or email address");
  if(input.amountPaise()<100||input.amountPaise()>1000000000L)throw bad("Enter a valid amount");
  if(!REFERENCE.matcher(reference).matches())throw bad("Enter a valid transaction reference (at least six characters)");
  Map<String,Object> d=chosen(groupId,input.destinationId());
  List<Map<String,Object>> existing=db.queryForList("SELECT id,destination_id,guest_name,guest_contact,amount_paise,transaction_reference,member_id,creator_admin_id FROM gift_group_guest_reports WHERE group_id=? AND submission_key=?",groupId,input.submissionKey());
  if(!existing.isEmpty()){
   Map<String,Object> old=existing.get(0);if(!Objects.equals(old.get("creator_admin_id"),creatorId)||!Objects.equals(old.get("member_id"),memberId)||!Objects.equals(old.get("destination_id"),input.destinationId())||!Objects.equals(old.get("guest_name"),name)||!Objects.equals(old.get("guest_contact"),contact.isEmpty()?null:contact)||!Objects.equals(((Number)old.get("amount_paise")).longValue(),input.amountPaise())||!Objects.equals(old.get("transaction_reference"),reference))throw new ResponseStatusException(HttpStatus.CONFLICT,"Submission key was already used for different details");
   return Map.of("reportId",old.get("id"),"state","REPORTED_UNVERIFIED","message","Thank you! Your contribution details have been received.");
  }
  UUID id=UUID.randomUUID();try{db.update("INSERT INTO gift_group_guest_reports(id,group_id,destination_id,guest_name,guest_contact,amount_paise,method,transaction_reference,submission_key,member_id,creator_admin_id) VALUES(?,?,?,?,?,?,?,?,?,?,?)",id,groupId,input.destinationId(),name,contact.isEmpty()?null:contact,input.amountPaise(),d.get("kind"),reference,input.submissionKey(),memberId,creatorId);}
  catch(org.springframework.dao.DuplicateKeyException ex){throw new ResponseStatusException(HttpStatus.CONFLICT,"This transaction reference has already been reported");}
  return Map.of("reportId",id,"state","REPORTED_UNVERIFIED","message","Thank you! Your contribution details have been received.");
 }
 public record Settings(Long suggestedAmountPaise,boolean enabled,UUID upiDestinationId,UUID bankDestinationId,String accessMode){}
 @PostMapping("/settings") @Transactional
 public Map<String,Object> settings(@PathVariable UUID token,@PathVariable UUID groupId,@RequestBody Settings input,HttpServletRequest req){
  groups.requireGroupCreator(token,groupId,req);if(input==null)throw bad("Settings required");
  if(input.suggestedAmountPaise()!=null&&(input.suggestedAmountPaise()<100||input.suggestedAmountPaise()>1000000000L))throw bad("Suggested amount must be between ₹1 and ₹1 crore");
  for(var choice:List.of(new AbstractMap.SimpleEntry<>("UPI",input.upiDestinationId()),new AbstractMap.SimpleEntry<>("BANK",input.bankDestinationId()))){
   if(choice.getValue()!=null){Integer n=db.queryForObject("SELECT count(*) FROM gift_group_payment_destinations WHERE id=? AND group_id=? AND kind=? AND enabled=TRUE AND status IN ('REGISTERED','VERIFIED')",Integer.class,choice.getValue(),groupId,choice.getKey());if(n==null||n!=1)throw bad("Choose an enabled "+choice.getKey()+" account");}
  }
  String mode=input.accessMode()==null?"BOTH":input.accessMode();if(!List.of("BOTH","GUESTS_ONLY","MEMBERS_ONLY").contains(mode))throw bad("Invalid invitation access mode");
  db.update("UPDATE gift_groups SET suggested_contribution_paise=?,manual_contributions_enabled=?,guest_upi_destination_id=?,guest_bank_destination_id=?,contribution_access_mode=? WHERE id=?",input.suggestedAmountPaise(),input.enabled(),input.upiDestinationId(),input.bankDestinationId(),mode,groupId);
  return Map.of("enabled",input.enabled(),"message","Contribution settings saved");
 }
 @GetMapping("/settings")
 public Map<String,Object> settingsView(@PathVariable UUID token,@PathVariable UUID groupId,HttpServletRequest req){groups.requireGroupCreator(token,groupId,req);return db.queryForMap("SELECT suggested_contribution_paise,manual_contributions_enabled,guest_upi_destination_id,guest_bank_destination_id,contribution_access_mode FROM gift_groups WHERE id=?",groupId);}
 @GetMapping("/history")
 public List<Map<String,Object>> history(@PathVariable UUID token,@PathVariable UUID groupId,HttpServletRequest req){groups.requireGroupCreator(token,groupId,req);return db.queryForList("SELECT r.id,r.guest_name,r.guest_contact,r.amount_paise,r.method,r.transaction_reference,r.state,r.created_at,r.member_id,r.creator_admin_id,m.display_name AS member_name FROM gift_group_guest_reports r LEFT JOIN gift_group_members m ON m.id=r.member_id AND m.group_id=r.group_id WHERE r.group_id=? ORDER BY r.created_at DESC LIMIT 500",groupId);}
}
