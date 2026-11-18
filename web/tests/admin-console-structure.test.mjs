// Admin console information architecture: every control is reachable from a grouped section,
// global switches ask for confirmation, and long switch lists can be searched and filtered.
import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
import {ADMIN_SECTIONS,ADMIN_GROUPS,MODULE_CATEGORIES,moduleCategory,groupModules,parseAdminHash,sectionHref,findSettings,SETTING_INDEX} from '../lib/admin-sections.mjs';

const read=p=>readFileSync(new URL(`../${p}`,import.meta.url),'utf8');
const page=read('app/admin/page.tsx'),shell=read('components/admin/AdminShell.tsx'),ui=read('components/admin/AdminUi.tsx'),gallery=read('app/admin/invitation-assets/page.tsx'),verification=read('components/PlatformVerificationControls.tsx');
const moduleCodes=[...page.match(/const modules:Record<string,string>=\{([^}]*)\}/)[1].matchAll(/([A-Z_]+):'/g)].map(m=>m[1]);

test('every section belongs to a known group and has a description',()=>{
  const groups=new Set(ADMIN_GROUPS.map(g=>g.id));
  for(const s of ADMIN_SECTIONS){assert.ok(groups.has(s.group),s.id);assert.ok(s.description.length>20,s.id);}
  assert.equal(new Set(ADMIN_SECTIONS.map(s=>s.id)).size,ADMIN_SECTIONS.length);
});

test('every in-page section is rendered by the console',()=>{
  for(const s of ADMIN_SECTIONS.filter(s=>!s.href))assert.ok(page.includes(`tab==='${s.id}'`),`render branch for ${s.id}`);
});

test('every existing admin control is still mounted',()=>{
  for(const c of ['<GrowthControls/>','<GrowthFunnel/>','<ProfilePreferences/>','<BusinessAudit/>','<EmailDeliveryStatus/>','<BrandingAdmin/>','<LocalizationAdmin/>','<LanguageScopeControl scope="HOST"','<LanguageScopeControl scope="EVENT"','<PreferredLanguage/>','<ProductionReadiness/>','<AdminEventModules ','<AdminVendors/>','<AdminReferrals/>','<AdminShowcase/>','<AdminPresets/>','<AdminGrowthResults/>','<AdminAiDesignDetails key=','<AdminAiDesignDetails eventId=','<PlatformVerificationControls/>','<MobileVerification/>','<AccountEmailVerification/>','<GroupMemberEmailPolicy ','<AccountPolicies ','<AdminAudit ','<AdminInvitationUploadAccess ','<AdminInteractions/>','<MonetizationControls/>','<CommercialAdminControls/>','<PaymentControls ','<RazorpaySwitch '])
    assert.ok(page.includes(c),c);
  for(const path of ["'/modules/'+code","'/hosts/'+selected+'/modules/'+m.module_code","'/events/'+e.id+'/album-settings'","'/events/'+e.id+'/ai-design-settings'","'/reports/group-transfers'","'/reports/guestbook'","'/reports/notifications'","'/summary'","'/audit'"])
    assert.ok(page.includes(path),path);
});

test('every platform module has a category',()=>{
  assert.ok(moduleCodes.length>=46);
  for(const code of moduleCodes)assert.notEqual(moduleCategory(code).id,'other',`category for ${code}`);
  const all=MODULE_CATEGORIES.flatMap(c=>c.codes);assert.equal(new Set(all).size,all.length,'no module in two categories');
});

test('module lists filter by text and state and group by category',()=>{
  const rows=[{module_code:'RSVP',enabled:true},{module_code:'SEATING_PLAN',enabled:false},{module_code:'GROUP_GIFTING',enabled:true}];
  const labels={RSVP:'RSVP',SEATING_PLAN:'Seating plan',GROUP_GIFTING:'Group gifting'};
  assert.deepEqual(groupModules(rows,labels).map(g=>g.id),['guests','gifts']);
  assert.deepEqual(groupModules(rows,labels,{query:'seat'})[0].rows.map(r=>r.module_code),['SEATING_PLAN']);
  assert.deepEqual(groupModules(rows,labels,{state:'OFF'}).flatMap(g=>g.rows).map(r=>r.module_code),['SEATING_PLAN']);
  assert.deepEqual(groupModules(rows,labels,{state:'ON',isOn:r=>r.module_code==='RSVP'}).flatMap(g=>g.rows).map(r=>r.module_code),['RSVP']);
  assert.deepEqual(groupModules(rows,labels,{query:'nothing'}),[]);
});

test('sections are addressable by hash and unknown hashes fall back to the overview',()=>{
  assert.deepEqual(parseAdminHash('#payments'),{section:'payments',query:''});
  assert.deepEqual(parseAdminHash('#features/Seating%20plan'),{section:'features',query:'Seating plan'});
  assert.deepEqual(parseAdminHash('#nope'),{section:'overview',query:''});
  assert.deepEqual(parseAdminHash('#gallery'),{section:'overview',query:''});
  assert.deepEqual(parseAdminHash('#features/%E0%A4'),{section:'features',query:''});
  assert.equal(sectionHref('gallery'),'/admin/invitation-assets');
  assert.equal(sectionHref('hosts'),'/admin#hosts');
});

test('settings search finds controls by plain-language words',()=>{
  assert.equal(findSettings('razorpay')[0].section,'payments');
  assert.equal(findSettings('otp')[0].section,'policies');
  assert.equal(findSettings('logo')[0].section,'appearance');
  assert.equal(findSettings('x').length,0);
  assert.equal(findSettings('seating',[{label:'Seating plan',section:'features',query:'Seating plan'}])[0].query,'Seating plan');
  for(const e of SETTING_INDEX)assert.ok(ADMIN_SECTIONS.some(s=>s.id===e.section),e.label);
});

test('platform-wide and verification switches ask for confirmation before saving',()=>{
  assert.match(page,/onChange=\{e=>setPending\(\{code:m\.module_code,enabled:e\.target\.checked\}\)\}/);
  assert.match(page,/<ConfirmDialog open=\{!!pending\}/);
  assert.match(verification,/setPending\(\{\.\.\.value\.settings,mobileOtpEnabled:e\.target\.checked\}\)/);
  assert.match(verification,/<ConfirmDialog /);
  assert.match(ui,/showModal\(\)/);assert.match(ui,/autoFocus/);
});

test('shell is navigable and accessible',()=>{
  assert.match(shell,/aria-current=\{item\.id===current\?'page':undefined\}/);
  assert.match(shell,/aria-expanded=\{menuOpen\}/);
  assert.match(shell,/className="admSkip" href="#admin-content"/);
  assert.match(shell,/role="combobox"/);
  assert.match(page,/role="switch"/);
  assert.match(gallery,/<AdminShell current="gallery"/);
});

test('users, verification inbox and group gift payments are reachable and wired to admin APIs',()=>{
  for(const id of ['users','inbox','groups'])assert.ok(ADMIN_SECTIONS.some(s=>s.id===id),id);
  for(const c of ['<AdminUsers/>','<AdminVerificationInbox/>','<AdminGroupPayments/>'])assert.ok(page.includes(c),c);
  const users=read('components/admin/AdminUsers.tsx'),inbox=read('components/admin/AdminVerificationInbox.tsx'),groups=read('components/admin/AdminGroupPayments.tsx');
  assert.match(users,/\/api\/admin\/console\/users\?/);
  assert.match(users,/sectionHref\('hosts',u\.id\)/);
  assert.match(inbox,/\/api\/admin\/console\/verification-inbox/);
  for(const key of ['GUEST_TRANSFERS','GROUP_REPORTS','HOST_ACCOUNTS','GROUP_ACCOUNTS','SHOWCASE'])assert.ok(inbox.includes(`${key}:{`),key);
  assert.doesNotMatch(inbox,/method:'POST'|,'POST'\)|,'PUT'\)/,'inbox is read-only');
  assert.match(groups,/\/api\/admin\/group-payments\/\$\{pending\.group\.id\}\/switching`,\{enabled:pending\.enabled\},'POST'/);
  assert.match(groups,/onChange=\{e=>setPending\(\{group:g,enabled:e\.target\.checked\}\)\}/);
  assert.match(groups,/<ConfirmDialog /);
  assert.equal(findSettings('switching')[0].section,'groups');
  assert.equal(findSettings('pending verification')[0].section,'inbox');
  assert.equal(parseAdminHash('#hosts/2b1c9a0e-1111-4222-8333-444455556666').query,'2b1c9a0e-1111-4222-8333-444455556666');
});
