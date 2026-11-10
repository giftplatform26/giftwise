package com.giftplatform.group;

import com.giftplatform.event.entity.EventStatus;
import com.giftplatform.event.entity.Event;
import com.giftplatform.event.repository.EventRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/public/groups/{token}")
public class GroupContributionController {
 @org.springframework.beans.factory.annotation.Autowired(required=false) private GroupGiftLifecycleService giftLifecycle;
 private final JdbcTemplate db;
 private final GroupReadinessService readiness;
 @org.springframework.beans.factory.annotation.Autowired private com.giftplatform.security.recovery.SignupEmailVerificationService signupEmails;
 private final EventRepository events;
 private final PasswordEncoder passwords;
 private final com.giftplatform.security.recovery.CredentialSessionVersionStore versions;
 private final com.giftplatform.security.recovery.EffectiveRecoveryPolicyService policies;
 private static final Pattern MOBILE=Pattern.compile("\\+?[1-9][0-9]{7,14}");
 private static final Pattern USER=Pattern.compile("[a-z0-9][a-z0-9_]{2,39}");
 private static final String MEMBER_SESSION="gift.group.member.";
 private static final String SESSION_KEY="gift.group.admin.";
 private static final String ACTIVITY_KEY="gift.group.activity.";
 private static final String PAGE_KEY="gift.group.page.";
 private static final String PAGE_HEADER="X-Group-Page-Id";
 private static final long GROUP_IDLE_TIMEOUT_MS=10L*60L*1000L;
 @ExceptionHandler(ResponseStatusException.class)
 public org.springframework.http.ResponseEntity<org.springframework.http.ProblemDetail> failure(ResponseStatusException ex){return org.springframework.http.ResponseEntity.status(ex.getStatusCode()).body(org.springframework.http.ProblemDetail.forStatusAndDetail(ex.getStatusCode(),ex.getReason()==null?"Unable to complete this group request":ex.getReason()));}
 private static final GroupLoginRateLimiter LOGIN_LIMITER = new GroupLoginRateLimiter();
 @org.springframework.beans.factory.annotation.Autowired
 public GroupContributionController(JdbcTemplate db,EventRepository events,PasswordEncoder passwords,com.giftplatform.security.recovery.CredentialSessionVersionStore versions,com.giftplatform.security.recovery.EffectiveRecoveryPolicyService policies,GroupReadinessService readiness){this.db=db;this.readiness=readiness;this.events=events;this.passwords=passwords;this.versions=versions;this.policies=policies;}
 public GroupContributionController(JdbcTemplate db,EventRepository events,PasswordEncoder passwords,com.giftplatform.security.recovery.CredentialSessionVersionStore versions,com.giftplatform.security.recovery.EffectiveRecoveryPolicyService policies){this(db,events,passwords,versions,policies,new GroupReadinessService(db));}
 public GroupContributionController(JdbcTemplate db,EventRepository events,PasswordEncoder passwords,com.giftplatform.security.recovery.CredentialSessionVersionStore versions){this(db,events,passwords,versions,null);}
 @org.springframework.beans.factory.annotation.Autowired private com.giftplatform.monetization.CommercialAccessService commerce;
 public record Credentials(String username,String password,String pin,String confirmPin,String fullName,String email,String mobile,String emailVerificationToken,boolean termsAccepted){
 public Credentials(String username,String password,String pin,String confirmPin,String fullName,String email,String mobile,String token){this(username,password,pin,confirmPin,fullName,email,mobile,token,false);}
 public Credentials(String username,String password,String pin,String confirmPin,String fullName,String email,String mobile){this(username,password,pin,confirmPin,fullName,email,mobile,null);}
 public Credentials(String username,String password){this(username,password,null,null,null,null,null);}
 public Credentials(String username,String password,String pin,String confirmPin){this(username,password,pin,confirmPin,null,null,null);}
 public Credentials(String username,String password,String pin,String confirmPin,String fullName){this(username,password,pin,confirmPin,fullName,null,null);}
}
 public record Setup(String name,String creatorName,String creatorMobile,String description,String giftName,String giftDescription,String giftLink,Long targetPaise,UUID giftId,Boolean independentGift){
  public Setup {independentGift=Boolean.TRUE.equals(independentGift);}
  public Setup(String name,String creatorName,String creatorMobile,String description,String giftName,String giftDescription,String giftLink,Long targetPaise,UUID giftId){this(name,creatorName,creatorMobile,description,giftName,giftDescription,giftLink,targetPaise,giftId,false);}
  public Setup(String name,String creatorName,String creatorMobile,String description,String giftName,String giftDescription,String giftLink,Long targetPaise){this(name,creatorName,creatorMobile,description,giftName,giftDescription,giftLink,targetPaise,null);}
 }
 public record GroupView(UUID id,String name,String creatorName,String description,String giftName,String giftDescription,String giftLink,Long targetPaise,String status,boolean independentGift){}
 UUID event(UUID token){return events.findByPublicToken(token).filter(e->e.getStatus()==EventStatus.PUBLISHED).map(e->e.getId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));}
 private String username(String v){if(v==null||!USER.matcher(v.toLowerCase(Locale.ROOT)).matches())throw bad("Username must have 3–40 letters, digits or underscores");return v.toLowerCase(Locale.ROOT);}
 private String text(String v,int max,boolean required){if(v==null||v.isBlank()){if(required)throw bad("Required field missing");return null;}v=v.strip();if(v.length()>max||v.chars().anyMatch(Character::isISOControl))throw bad("Invalid field");return v;}
 private ResponseStatusException bad(String msg){return new ResponseStatusException(HttpStatus.BAD_REQUEST,msg);}
 private boolean pinAllowed(UUID eventId){return policies==null||policies.effective(null,eventId).allowGroupPin();}
 private void requirePinAllowed(UUID eventId){if(!pinAllowed(eventId))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"PIN sign in is disabled. Use your username and password.");}
 private String registrationPin(UUID eventId,String pin,String confirm){
  if(!pinAllowed(eventId))return null; // Password registration remains available when PINs are disabled.
  return validatedPin(pin,confirm);
 }
 @GetMapping("/account-policy")
 public Map<String,Boolean> accountPolicy(@PathVariable UUID token){UUID e=event(token);return Map.of("allowGroupPin",pinAllowed(e),"memberEmailVerificationRequired",readiness.memberEmailRequired(e));}
 private String validatedPin(String pin,String confirm){
  if(pin==null||!pin.matches("[0-9]{6}"))throw bad("PIN must be exactly six digits");
  if(!pin.equals(confirm))throw bad("PIN confirmation does not match");
  return pin;
 }
 private boolean validLogin(String password,String pin,String pwHash,String pinHash){
  if(pin!=null&&!pin.isBlank())return pin.matches("[0-9]{6}")&&pinHash!=null&&passwords.matches(pin,pinHash);
  return password!=null&&passwords.matches(password,pwHash);
 }
 private void ensureUniqueMemberPin(UUID groupId,String username,String pin,UUID exclude){
  db.queryForObject("SELECT id FROM gift_groups WHERE id=? FOR UPDATE",UUID.class,groupId);
  for(Map<String,Object> row:db.queryForList("SELECT id,pin_hash FROM gift_group_members WHERE group_id=? AND username=? AND pin_hash IS NOT NULL",groupId,username)){
   if(!row.get("id").equals(exclude)&&passwords.matches(pin,(String)row.get("pin_hash")))
    throw new ResponseStatusException(HttpStatus.CONFLICT,"PIN already taken for this group. Choose a different PIN");
  }
 }
 private void ensureUniqueAdminPin(UUID eventId,String username,String pin,UUID exclude){
  db.queryForObject("SELECT id FROM events WHERE id=? FOR UPDATE",UUID.class,eventId);
  for(Map<String,Object> row:db.queryForList("SELECT id,pin_hash FROM gift_group_accounts WHERE event_id=? AND username=? AND pin_hash IS NOT NULL",eventId,username)){
   if(!row.get("id").equals(exclude)&&passwords.matches(pin,(String)row.get("pin_hash")))
    throw new ResponseStatusException(HttpStatus.CONFLICT,"PIN already taken for this group. Choose a different PIN");
  }
 }
 UUID sessionAdmin(UUID eventId,HttpServletRequest req){HttpSession s=req.getSession(false);Object o=s==null?null:s.getAttribute(SESSION_KEY+eventId);if(!(o instanceof UUID id))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
  Object bound=s.getAttribute(PAGE_KEY+eventId);
  String presented=req.getHeader(PAGE_HEADER);
  if(!(bound instanceof String pageId)||presented==null||!pageId.equals(presented))
   throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Group login required for this page");
  Object last=s.getAttribute(ACTIVITY_KEY+eventId);
  long now=System.currentTimeMillis();
  if(!(last instanceof Long time)||now-time<0||now-time>=GROUP_IDLE_TIMEOUT_MS){
   s.removeAttribute(SESSION_KEY+eventId);s.removeAttribute(ACTIVITY_KEY+eventId);s.removeAttribute(PAGE_KEY+eventId);
   throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Group session expired");
  }
  Object epoch=s.getAttribute("gift.group.admin.epoch."+eventId);
  if(!(epoch instanceof Long v)||v.longValue()!=versions.current("GROUP_ADMIN",id)){
   s.removeAttribute(SESSION_KEY+eventId);throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Group session expired");
  }
  if("1".equals(req.getHeader("X-Gift-User-Activity")))s.setAttribute(ACTIVITY_KEY+eventId,now);Integer n=db.queryForObject("SELECT count(*) FROM gift_group_accounts WHERE id=? AND event_id=?",Integer.class,id,eventId);if(n==null||n!=1)throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);return id;}
 @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED) @Transactional
 public Map<String,String> register(@PathVariable UUID token,@RequestBody Credentials c){UUID e=event(token);if(c==null)throw bad("Credentials required");String u=c.username();String pw=c.password();if(u==null||!u.matches(com.giftplatform.security.CredentialPolicy.USERNAME_PATTERN))throw bad(com.giftplatform.security.CredentialPolicy.USERNAME_MESSAGE);u=u.toLowerCase(Locale.ROOT);validateNewPassword(pw);requireConsent(c.termsAccepted());String pin=registrationPin(e,c.pin(),c.confirmPin());String fullName=text(c.fullName(),120,true);if(fullName.length()<2)throw bad("Full name must be at least two characters");
 String email=text(c.email(),254,true).toLowerCase(Locale.ROOT);
 if(!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))throw bad("Valid email required");
 String phone=mobile(c.mobile());
 // Mandatory contact collection does not imply verified ownership.
 // Contact-verification timestamps remain NULL until real independent verification.
 UUID accountId=UUID.randomUUID();if(pin!=null)ensureUniqueAdminPin(e,u,pin,null);try{db.update("INSERT INTO gift_group_accounts(id,event_id,username,password_hash,pin_hash,full_name,email,mobile,email_verified_at,mobile_verified_at) VALUES(?,?,?,?,?,?,?,?,NULL,NULL)",accountId,e,u,passwords.encode(pw),pin==null?null:passwords.encode(pin),fullName,email,phone);}catch(org.springframework.dao.DuplicateKeyException ex){throw new ResponseStatusException(HttpStatus.CONFLICT,"Username already exists");}if(readiness.emailEnabled()&&c.emailVerificationToken()!=null&&!c.emailVerificationToken().isBlank()&&signupEmails.consume(c.emailVerificationToken(),"GROUP_ADMIN",e,email))db.update("UPDATE gift_group_accounts SET email_verified_at=now() WHERE id=?",accountId);consent("GROUP_ADMIN",accountId);return Map.of("username",u);}
 @PostMapping("/login")
 public Map<String,String> login(@PathVariable UUID token,@RequestBody Credentials c,HttpServletRequest req){UUID e=event(token);if(c==null)throw bad("Credentials required");if(c.pin()!=null&&!c.pin().isBlank())requirePinAllowed(e);boolean pinLogin=c.pin()!=null&&!c.pin().isBlank();
  if(pinLogin&&!c.pin().matches("[0-9]{6}"))throw bad("PIN must be six digits");
  String u=username(c.username()==null?null:c.username().strip());String loginKey=req.getRemoteAddr()+"|"+e+"|"+u;String ipKey=req.getRemoteAddr();if(!LOGIN_LIMITER.allowed(loginKey,ipKey))throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,"Try again later");
  List<Map<String,Object>> rows=db.queryForList("SELECT id,password_hash,pin_hash,locked_until FROM gift_group_accounts WHERE event_id=? AND username=?",e,u);
  if(rows.isEmpty()){LOGIN_LIMITER.failed(loginKey,ipKey);passwords.matches(c.password()==null?"":c.password(),"$2a$12$0VdzzV7Z6d1XIf5PjCPZkuoFw73qhOUmIZ.3mxY0C7hzXEKpBQeNa");throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid credentials");}Map<String,Object> row=rows.get(0);UUID id=(UUID)row.get("id");java.sql.Timestamp locked=(java.sql.Timestamp)row.get("locked_until");if(locked!=null&&locked.toInstant().isAfter(java.time.Instant.now()))throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,"Try again later");if(!validLogin(c.password(),c.pin(),(String)row.get("password_hash"),(String)row.get("pin_hash"))){LOGIN_LIMITER.failed(loginKey,ipKey);db.update("UPDATE gift_group_accounts SET failed_attempts=CASE WHEN locked_until IS NOT NULL AND locked_until<=now() THEN 1 ELSE failed_attempts+1 END,locked_until=CASE WHEN (CASE WHEN locked_until IS NOT NULL AND locked_until<=now() THEN 1 ELSE failed_attempts+1 END)>=5 THEN now()+interval '15 minutes' ELSE NULL END WHERE id=? AND (locked_until IS NULL OR locked_until<=now())",id);throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid credentials");}db.update("UPDATE gift_group_accounts SET failed_attempts=0,locked_until=NULL WHERE id=?",id);LOGIN_LIMITER.succeeded(loginKey);String pageId=req.getHeader(PAGE_HEADER);
  if(pageId==null||!pageId.matches("[0-9a-fA-F-]{36}"))throw bad("Missing page identity");
  try{UUID.fromString(pageId);}catch(IllegalArgumentException ex){throw bad("Invalid page identity");}
  HttpSession session=req.getSession(true);req.changeSessionId();session.setAttribute(SESSION_KEY+e,id);session.setAttribute(ACTIVITY_KEY+e,System.currentTimeMillis());session.setAttribute(PAGE_KEY+e,pageId);session.setAttribute("gift.group.admin.epoch."+e,versions.current("GROUP_ADMIN",id));return Map.of("message","Signed in","pinSetupRequired",row.get("pin_hash")==null?"true":"false");}
 public record PinSetup(String password,String pin,String confirmPin){}
 @PostMapping("/pin/setup") @Transactional
 public Map<String,String> adminPinSetup(@PathVariable UUID token,@RequestBody PinSetup input,HttpServletRequest req){
  UUID e=event(token),id=sessionAdmin(e,req);requirePinAllowed(e);
  if(input==null)throw bad("PIN setup required");
  String pin=validatedPin(input.pin(),input.confirmPin());
  String hash=db.queryForObject("SELECT password_hash FROM gift_group_accounts WHERE id=? AND event_id=?",String.class,id,e);
  if(!passwords.matches(input.password()==null?"":input.password(),hash))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Current password is incorrect");
  ensureUniqueAdminPin(e,db.queryForObject("SELECT username FROM gift_group_accounts WHERE id=? AND event_id=?",String.class,id,e),pin,id);db.update("UPDATE gift_group_accounts SET pin_hash=? WHERE id=?",passwords.encode(pin),id);
  return Map.of("message","PIN saved");
 }
 @PostMapping("/member-pin/setup/{groupId}") @Transactional
 public Map<String,String> memberPinSetup(@PathVariable UUID token,@PathVariable UUID groupId,@RequestBody PinSetup input,HttpServletRequest req){
  publicGroup(token,groupId);requirePinAllowed(event(token));UUID id=member(groupId,req);
  if(input==null)throw bad("PIN setup required");
  String pin=validatedPin(input.pin(),input.confirmPin());
  String hash=db.queryForObject("SELECT password_hash FROM gift_group_members WHERE id=? AND group_id=?",String.class,id,groupId);
  if(!passwords.matches(input.password()==null?"":input.password(),hash))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Current password is incorrect");
  ensureUniqueMemberPin(groupId,db.queryForObject("SELECT username FROM gift_group_members WHERE id=? AND group_id=?",String.class,id,groupId),pin,id);db.update("UPDATE gift_group_members SET pin_hash=? WHERE id=?",passwords.encode(pin),id);
  return Map.of("message","PIN saved");
 }
 @PostMapping("/logout") public void logout(@PathVariable UUID token,HttpServletRequest req){UUID e=event(token);HttpSession s=req.getSession(false);if(s!=null){Object bound=s.getAttribute(PAGE_KEY+e);
  if(bound!=null&&!bound.equals(req.getHeader(PAGE_HEADER)))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
  s.removeAttribute(SESSION_KEY+e);s.removeAttribute(ACTIVITY_KEY+e);s.removeAttribute(PAGE_KEY+e);}}
 public record FullNameInput(String fullName){}
 @PostMapping("/profile-name") public Map<String,String> setAdminName(@PathVariable UUID token,@RequestBody FullNameInput input,HttpServletRequest req){
  UUID eventId=event(token),adminId=sessionAdmin(eventId,req);
  String name=text(input==null?null:input.fullName(),120,true);
  if(name.length()<2)throw bad("Full name must be at least two characters");
  db.update("UPDATE gift_group_accounts SET full_name=? WHERE id=? AND event_id=?",name,adminId,eventId);
  return Map.of("fullName",name);
 }
 @GetMapping("/dashboard") public Map<String,Object> dashboard(@PathVariable UUID token,HttpServletRequest req){UUID e=event(token),id=sessionAdmin(e,req);List<GroupView> g=groups(id);Map<String,Object> out=new LinkedHashMap<>();out.put("fullName",db.queryForObject("SELECT full_name FROM gift_group_accounts WHERE id=?",String.class,id));out.put("setupRequired",g.isEmpty());out.put("group",g.isEmpty()?null:g.get(0));
  if(!g.isEmpty()){
   UUID groupId=g.get(0).id();
   out.put("creatorEmailVerified",Boolean.TRUE.equals(db.queryForObject("SELECT email_verified_at IS NOT NULL FROM gift_group_accounts WHERE id=?",Boolean.class,id)));
   out.put("selectedGiftId",db.queryForObject("SELECT selected_gift_id FROM gift_groups WHERE id=?",UUID.class,groupId));
   out.put("reservedGiftId",db.queryForObject("SELECT reserved_gift_id FROM gift_groups WHERE id=?",UUID.class,groupId));
   out.put("invitationReadiness",readiness.invitation(groupId));
   out.put("publishReadiness",readiness.invitation(groupId));
   out.put("giftProgress",db.queryForMap("SELECT p.collected_paise,p.reported_paise,p.completion_percentage,g.gift_published_at,g.fulfilment_status,g.tracking_details FROM group_gift_progress p JOIN gift_groups g ON g.id=p.id WHERE g.id=?",groupId));
   out.put("collectionPublished",Boolean.TRUE.equals(db.queryForObject("SELECT collection_published_at IS NOT NULL FROM gift_groups WHERE id=?",Boolean.class,groupId)));
   out.put("summary",db.queryForMap("SELECT (SELECT count(*) FROM gift_group_members WHERE group_id=? AND status='APPROVED') AS member_count, (SELECT count(*) FROM gift_group_members WHERE group_id=? AND status='PENDING') AS pending_member_count, COALESCE(SUM(amount_paise) FILTER (WHERE state='REPORTED_UNVERIFIED'),0) AS reported_paise, count(*) FILTER (WHERE state='REPORTED_UNVERIFIED') AS report_count FROM group_collection_transactions WHERE group_id=?",groupId,groupId,groupId));
   out.put("privacy",db.queryForMap("SELECT join_policy,member_visibility,amount_visibility,creator_direct_payments_enabled FROM gift_groups WHERE id=?",groupId));
  }
  return out;}
 public record PrivacySettings(String joinPolicy,String memberVisibility,String amountVisibility,Boolean creatorDirectPaymentsEnabled){}
 private static String choice(String value,String allowed,String field){if(value==null||!Arrays.asList(allowed.split(",")).contains(value))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid "+field);return value;}
 @GetMapping("/privacy") public Map<String,Object> privacy(@PathVariable UUID token,HttpServletRequest req){UUID e=event(token),id=sessionAdmin(e,req);List<Map<String,Object>> rows=db.queryForList("SELECT join_policy,member_visibility,amount_visibility,creator_direct_payments_enabled FROM gift_groups WHERE admin_id=?",id);if(rows.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Create group first");return rows.get(0);}
 @PostMapping("/privacy") @Transactional public Map<String,Object> privacyUpdate(@PathVariable UUID token,@RequestBody PrivacySettings p,HttpServletRequest req){UUID e=event(token),id=sessionAdmin(e,req);if(p==null)throw bad("Privacy settings required");String join=choice(p.joinPolicy(),"APPROVAL,AUTO_JOIN,INVITE_ONLY","join policy"),members=choice(p.memberVisibility(),"ADMIN_ONLY,MEMBERS","member visibility"),amounts=choice(p.amountVisibility(),"ADMIN_ONLY,OWN_ONLY","amount visibility");// Direct payments cannot be enabled until encrypted creator destinations are implemented and validated.
  if(Boolean.TRUE.equals(p.creatorDirectPaymentsEnabled()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Creator payment destinations are not configured; direct payments remain disabled");
  int n=db.update("UPDATE gift_groups SET join_policy=?,member_visibility=?,amount_visibility=?,creator_direct_payments_enabled=FALSE WHERE admin_id=? AND event_id=? AND status IN ('DRAFT','PUBLISHED')",join,members,amounts,id,e);if(n!=1)throw new ResponseStatusException(HttpStatus.CONFLICT,"Group not found or not editable");return privacy(token,req);}
 private List<GroupView> groups(UUID id){return db.query("SELECT id,name,creator_name,creator_mobile,description,gift_name,gift_description,gift_link,target_paise,status,independent_gift FROM gift_groups WHERE admin_id=?",(rs,i)->new GroupView((UUID)rs.getObject("id"),rs.getString("name"),rs.getString("creator_name"),rs.getString("description"),rs.getString("gift_name"),rs.getString("gift_description"),rs.getString("gift_link"),rs.getObject("target_paise")==null?null:rs.getLong("target_paise"),rs.getString("status"),rs.getBoolean("independent_gift")),id);}
 @PostMapping("/setup") @ResponseStatus(HttpStatus.CREATED) @Transactional
 public GroupView setup(@PathVariable UUID token,@RequestBody Setup s,HttpServletRequest req){UUID e=event(token),id=sessionAdmin(e,req);if(s==null)throw bad("Setup required");String name=text(s.name(),120,true),creator=text(s.creatorName(),120,true),mobile=text(s.creatorMobile(),20,true);if(!mobile.matches("\\+?[0-9 ]{8,20}"))throw bad("Invalid mobile number");if(s.independentGift()&&s.giftId()!=null)throw bad("Independent groups choose their own gift, outside the host wishlist");boolean fromWishlist=s.giftId()!=null;String gift=fromWishlist?null:text(s.giftName(),160,false),desc=fromWishlist?null:text(s.giftDescription(),500,false),link=fromWishlist?null:text(s.giftLink(),1000,false);if(link!=null&&!link.matches("https://[^\\s]+"))throw bad("Gift link must use HTTPS");if(!fromWishlist&&s.targetPaise()!=null&&(s.targetPaise()<=0||s.targetPaise()>100000000000L))throw bad("Invalid target amount");if(!fromWishlist&&(desc!=null||link!=null)&&gift==null)throw bad("Gift name required for gift details");if(!groups(id).isEmpty())throw new ResponseStatusException(HttpStatus.CONFLICT,"Group already created");UUID createdGroup=UUID.randomUUID();try{db.update("INSERT INTO gift_groups(id,event_id,admin_id,name,creator_name,creator_mobile,description,gift_name,gift_description,gift_link,target_paise,independent_gift) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)",createdGroup,e,id,name,creator,mobile,text(s.description(),500,false),gift,desc,link,fromWishlist?null:s.targetPaise(),s.independentGift());}catch(org.springframework.dao.DuplicateKeyException ex){throw new ResponseStatusException(HttpStatus.CONFLICT,"Group already created");}if(s.giftId()!=null){if(giftLifecycle==null)throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Gift selection is unavailable");giftLifecycle.select(createdGroup,id,s.giftId());}return groups(id).get(0);}

 // Batch 7C.3: Membership is isolated from the creator/admin login.
 public record JoinRequest(String displayName,String mobile,String password,String confirmPassword,String pin,String confirmPin,String email,String emailVerificationToken,boolean termsAccepted){
 public JoinRequest(String displayName,String mobile,String password,String confirmPassword,String pin,String confirmPin,String email,String token){this(displayName,mobile,password,confirmPassword,pin,confirmPin,email,token,false);}
 public JoinRequest(String displayName,String mobile,String password,String confirmPassword,String pin,String confirmPin){this(displayName,mobile,password,confirmPassword,pin,confirmPin,null,null);}
 }
 public record MemberLogin(String displayName,String password,String pin,String username){
  // Preserve existing clients that submit their group username as displayName.
  public MemberLogin(String displayName,String password,String pin){this(displayName,password,pin,null);}
 }
 public record Review(UUID memberId,String action){}
 private UUID groupId(UUID eventId,UUID groupId){
  Integer n=db.queryForObject("SELECT count(*) FROM gift_groups WHERE id=? AND event_id=?",Integer.class,groupId,eventId);
  if(n==null||n!=1)throw new ResponseStatusException(HttpStatus.NOT_FOUND);
  return groupId;
 }
 UUID requireGroupCreator(UUID token,UUID groupId,HttpServletRequest req){
  UUID e=event(token),admin=sessionAdmin(e,req);
  Integer n=db.queryForObject("SELECT count(*) FROM gift_groups WHERE id=? AND event_id=? AND admin_id=?",Integer.class,groupId,e,admin);
  if(n==null||n!=1)throw new ResponseStatusException(HttpStatus.FORBIDDEN);
  return admin;
 }
 private UUID adminGroup(UUID token,UUID groupId,HttpServletRequest req){
  UUID e=event(token),admin=sessionAdmin(e,req);
  Integer n=db.queryForObject("SELECT count(*) FROM gift_groups WHERE id=? AND event_id=? AND admin_id=?",Integer.class,groupId,e,admin);
  if(n==null||n!=1)throw new ResponseStatusException(HttpStatus.FORBIDDEN);
  return groupId;
 }
 private String memberName(String value){
  String name=text(value,120,true);
  if(name.length()<2)throw bad("Display name must have at least two characters");
  return name;
 }
 private String mobile(String value){
  String m=value==null?"":value.replace(" ","").replace("-","");
  if(!MOBILE.matcher(m).matches())throw bad("Enter a valid international mobile number");
  return m;
 }
 private void validateNewPassword(String value){try{com.giftplatform.security.CredentialPolicy.validatePassword(value);}catch(IllegalArgumentException ex){throw bad(ex.getMessage());}}
 private void requireConsent(boolean accepted){if(!accepted)throw bad("Please agree to the Terms & Conditions and Privacy Policy.");}
 private void consent(String kind,UUID id){db.update("INSERT INTO account_legal_consents(id,account_kind,account_id,terms_version,privacy_version) VALUES(?,?,?,?,?)",UUID.randomUUID(),kind,id,"2026-11-05","2026-11-05");}
 private String memberPassword(String value){
  validateNewPassword(value);
  return value;
 }
 @GetMapping("/join/{groupId}")
 public Map<String,Object> groupInvitation(@PathVariable UUID token,@PathVariable UUID groupId){
  Map<String,Object> view=new LinkedHashMap<>(publicGroup(token,groupId));
  view.put("memberEmailVerificationRequired",readiness.memberEmailRequired(event(token)));
  view.put("allowGroupPin",pinAllowed(event(token)));return view;
 }
 public Map<String,Object> publicGroup(@PathVariable UUID token,@PathVariable UUID groupId){
  UUID e=event(token);groupId(e,groupId);
  List<Map<String,Object>> rows=db.queryForList("SELECT name,description,    CASE WHEN gift_published_at IS NOT NULL THEN gift_name END AS gift_name,    CASE WHEN gift_published_at IS NOT NULL THEN gift_description END AS gift_description,    CASE WHEN gift_published_at IS NOT NULL THEN gift_link END AS gift_link,    CASE WHEN gift_published_at IS NULL THEN 'Something is Cooking 🍳' ELSE '100% Contributed ✅' END AS display_status,    join_policy,status FROM gift_groups WHERE id=? AND event_id=?",groupId,e);
  if(rows.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND);
  Map<String,Object> r=new LinkedHashMap<>(rows.get(0));
  if(commerce!=null)commerce.requireGroupFee(groupId);
  if(!readiness.invitation(groupId).enabled())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Group invitation is not ready");
  return r;
 }
 private void requireMemberAccess(UUID groupId){String mode=db.queryForObject("SELECT contribution_access_mode FROM gift_groups WHERE id=?",String.class,groupId);if("GUESTS_ONLY".equals(mode))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Member access disabled for this invitation");}
 @PostMapping("/join/{groupId}") @Transactional
 public Map<String,String> join(@PathVariable UUID token,@PathVariable UUID groupId,@RequestBody JoinRequest request){
  Map<String,Object> group=publicGroup(token,groupId);requireMemberAccess(groupId);
  if(request==null)throw bad("Member details required");
  requireConsent(request.termsAccepted());
  if(request.displayName()==null||!request.displayName().matches(com.giftplatform.security.CredentialPolicy.USERNAME_PATTERN))throw bad(com.giftplatform.security.CredentialPolicy.USERNAME_MESSAGE);
  String name=memberName(request.displayName()),phone=mobile(request.mobile()),password=memberPassword(request.password());
  if(!password.equals(request.confirmPassword()))throw bad("Password confirmation does not match");
  String pin=registrationPin(event(token),request.pin(),request.confirmPin());if(pin!=null)ensureUniqueMemberPin(groupId,name.toLowerCase(Locale.ROOT),pin,null);
  if("INVITE_ONLY".equals(group.get("join_policy")))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Invitation-only membership requires an individual invitation credential");
  UUID memberId=UUID.randomUUID();
  String signupEmail=request.email()==null||request.email().isBlank()?null:request.email().strip().toLowerCase(Locale.ROOT);
  if(signupEmail!=null&&(signupEmail.length()>254||!signupEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")))throw bad("Enter a valid email address");
  boolean emailRequired=readiness.memberEmailRequired(event(token));
  if(emailRequired&&signupEmail==null)throw bad("Verify your email before joining this group");
  boolean emailVerified=false;
  if(readiness.emailEnabled()&&request.emailVerificationToken()!=null&&!request.emailVerificationToken().isBlank()){
   if(signupEmail==null)throw bad("Email is required to use a verification");
   emailVerified=signupEmails.consume(request.emailVerificationToken(),"GROUP_MEMBER",groupId,signupEmail);
  }
  if(emailRequired&&!emailVerified)throw bad("Verify your email before joining this group");
  try{
   db.update("INSERT INTO gift_group_members(id,group_id,display_name,username,password_hash,mobile,status,pin_hash) VALUES(?,?,?,?,?,?,?,?)",memberId,groupId,name,name.toLowerCase(Locale.ROOT),passwords.encode(password),phone,"AUTO_JOIN".equals(group.get("join_policy"))?"APPROVED":"PENDING",pin==null?null:passwords.encode(pin));
  }catch(org.springframework.dao.DuplicateKeyException ex){throw new ResponseStatusException(HttpStatus.CONFLICT,"Display name already in use");}
  if(signupEmail!=null)db.update("UPDATE gift_group_members SET email=? WHERE id=?",signupEmail,memberId);
  if(emailVerified)db.update("UPDATE gift_group_members SET email_verified_at=now() WHERE id=?",memberId);
  consent("GROUP_MEMBER",memberId);
  boolean auto="AUTO_JOIN".equals(group.get("join_policy"));return Map.of("status",auto?"APPROVED":"PENDING","message",auto?"You have joined the group. Sign in to continue.":"Your request has been sent to the group admin");
 }
 @PostMapping("/member-login/{groupId}")
 public Map<String,String> memberLogin(@PathVariable UUID token,@PathVariable UUID groupId,@RequestBody MemberLogin request,HttpServletRequest req){
  publicGroup(token,groupId);requireMemberAccess(groupId);
  if(request==null)throw bad("Credentials required");
  if(request.pin()!=null&&!request.pin().isBlank())requirePinAllowed(event(token));
  String loginKey="member|"+groupId+"|"+req.getRemoteAddr();
  if(!LOGIN_LIMITER.allowed(loginKey,req.getRemoteAddr()))throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS);
  boolean pinLogin=request.pin()!=null&&!request.pin().isBlank();
  if(pinLogin&&!request.pin().matches("[0-9]{6}"))throw bad("PIN must be six digits");
  String name=memberName(request.username()!=null?request.username():request.displayName());
  List<Map<String,Object>> rows=db.queryForList("SELECT id,password_hash,pin_hash,status FROM gift_group_members WHERE group_id=? AND username=?",groupId,name.toLowerCase(Locale.ROOT));
  if(rows.isEmpty()||!validLogin(request.password(),request.pin(),(String)rows.get(0).get("password_hash"),(String)rows.get(0).get("pin_hash"))){LOGIN_LIMITER.failed(loginKey,req.getRemoteAddr());throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid member credentials");}
  Map<String,Object> m=rows.get(0);
  if(!"APPROVED".equals(m.get("status")))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Membership not approved");
  LOGIN_LIMITER.succeeded(loginKey);HttpSession session=req.getSession(true);req.changeSessionId();session.setAttribute(MEMBER_SESSION+groupId,m.get("id"));session.setAttribute("gift.group.member.activity."+groupId,System.currentTimeMillis());session.setAttribute("gift.group.member.epoch."+groupId,versions.current("GROUP_MEMBER",(UUID)m.get("id")));
  return Map.of("status","APPROVED","pinSetupRequired",m.get("pin_hash")==null?"true":"false");
 }
 UUID member(UUID groupId,HttpServletRequest req){
  HttpSession session=req.getSession(false);Object id=session==null?null:session.getAttribute(MEMBER_SESSION+groupId);
  if(!(id instanceof UUID memberId))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
  long now=System.currentTimeMillis();Object last=session.getAttribute("gift.group.member.activity."+groupId);
  if(last instanceof Long time&&(now-time<0||now-time>=GROUP_IDLE_TIMEOUT_MS)){session.removeAttribute(MEMBER_SESSION+groupId);session.removeAttribute("gift.group.member.activity."+groupId);throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Member session expired after inactivity.");}
  if(!(last instanceof Long)||"1".equals(req.getHeader("X-Gift-User-Activity")))session.setAttribute("gift.group.member.activity."+groupId,now);
  Object epoch=session.getAttribute("gift.group.member.epoch."+groupId);
  if(!(epoch instanceof Long v)||v.longValue()!=versions.current("GROUP_MEMBER",memberId)){
   session.removeAttribute(MEMBER_SESSION+groupId);throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Member session expired");
  }
  Integer n=db.queryForObject("SELECT count(*) FROM gift_group_members WHERE id=? AND group_id=? AND status='APPROVED'",Integer.class,memberId,groupId);
  if(n==null||n!=1)throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
  return memberId;
 }
 void requireMemberContact(UUID groupId,UUID memberId){
  UUID eventId=db.queryForObject("SELECT event_id FROM gift_groups WHERE id=?",UUID.class,groupId);
  if(readiness.mobileEnabled()&&!Boolean.TRUE.equals(db.queryForObject("SELECT mobile_verified_at IS NOT NULL FROM gift_group_members WHERE id=? AND group_id=?",Boolean.class,memberId,groupId)))throw new ResponseStatusException(HttpStatus.CONFLICT,"Verify your mobile in Account before contributing");
  if(readiness.memberEmailRequired(eventId)&&!Boolean.TRUE.equals(db.queryForObject("SELECT email_verified_at IS NOT NULL FROM gift_group_members WHERE id=? AND group_id=?",Boolean.class,memberId,groupId)))throw new ResponseStatusException(HttpStatus.CONFLICT,"Verify your email in Account before contributing");
 }
 @PostMapping("/member-logout/{groupId}")
 public Map<String,String> memberLogout(@PathVariable UUID token,@PathVariable UUID groupId,HttpServletRequest req){
  UUID eventId=events.findByPublicToken(token).map(Event::getId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
  groupId(eventId,groupId);HttpSession session=req.getSession(false);
  if(session!=null){session.removeAttribute(MEMBER_SESSION+groupId);session.removeAttribute("gift.group.member.epoch."+groupId);}
  return Map.of("message","Signed out");
 }
 @GetMapping("/member-view/{groupId}")
 public Map<String,Object> memberView(@PathVariable UUID token,@PathVariable UUID groupId,HttpServletRequest req){
  Map<String,Object> g=publicGroup(token,groupId);requireMemberAccess(groupId);UUID id=member(groupId,req);
  Map<String,Object> out=new LinkedHashMap<>();out.put("group",g);
  out.put("me",db.queryForMap("SELECT id,display_name FROM gift_group_members WHERE id=?",id));
  var progress=db.queryForMap("SELECT collected_paise,completion_percentage FROM group_gift_progress WHERE id=?",groupId);
  out.put("confirmedPaise",progress.get("collected_paise"));
  out.put("progressPercent",progress.get("completion_percentage"));
  if("MEMBERS".equals(db.queryForObject("SELECT member_visibility FROM gift_groups WHERE id=?",String.class,groupId)))
   out.put("members",db.queryForList("SELECT display_name FROM gift_group_members WHERE group_id=? AND status='APPROVED' ORDER BY created_at",groupId));
  return out;
 }
 @GetMapping("/members/{groupId}")
 public List<Map<String,Object>> members(@PathVariable UUID token,@PathVariable UUID groupId,HttpServletRequest req){
  adminGroup(token,groupId,req);
  return db.queryForList("SELECT id,display_name,mobile,status,created_at,reviewed_at FROM gift_group_members WHERE group_id=? ORDER BY created_at",groupId);
 }
 @PostMapping("/members/{groupId}/review") @Transactional
 public Map<String,String> review(@PathVariable UUID token,@PathVariable UUID groupId,@RequestBody Review review,HttpServletRequest req){
  adminGroup(token,groupId,req);
  if(review==null||review.memberId()==null||!("APPROVE".equals(review.action())||"REJECT".equals(review.action())))throw bad("Invalid review");
  String status="APPROVE".equals(review.action())?"APPROVED":"REJECTED";
  int n=db.update("UPDATE gift_group_members SET status=?,reviewed_at=now() WHERE id=? AND group_id=? AND status='PENDING'",status,review.memberId(),groupId);
  if(n!=1)throw new ResponseStatusException(HttpStatus.CONFLICT,"Request already reviewed or missing");
  return Map.of("status",status);
 }
 @PostMapping("/publish/{groupId}") @Transactional
 public Map<String,String> publish(@PathVariable UUID token,@PathVariable UUID groupId,HttpServletRequest req){
  adminGroup(token,groupId,req);
  if(commerce!=null)commerce.requireGroupFee(groupId);
  var ready=readiness.invitation(groupId);
  if(!ready.enabled())throw new ResponseStatusException(HttpStatus.CONFLICT,String.join("; ",ready.reasons()));
  int n=db.update("UPDATE gift_groups SET status='PUBLISHED',collection_published_at=now() WHERE id=? AND collection_published_at IS NULL",groupId);
  if(n!=1)throw new ResponseStatusException(HttpStatus.CONFLICT,"Group already published");
  db.update("INSERT INTO group_collection_notifications(id,group_id,event_id) SELECT ?,id,event_id FROM gift_groups WHERE id=? ON CONFLICT(group_id) DO NOTHING",UUID.randomUUID(),groupId);
  return Map.of("status","PUBLISHED");
 }

}
