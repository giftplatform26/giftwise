import { validateOrigin } from '../src/platform/policy.mjs';
const missing=[];
for(const key of ['EXPO_PUBLIC_GIFTWISE_WEB_URL','GIFTWISE_IOS_BUNDLE_ID','GIFTWISE_ANDROID_PACKAGE','GIFTWISE_EAS_PROJECT_ID'])if(!process.env[key])missing.push(key);
if(missing.length){console.error('Build configuration missing:\n'+missing.map(key=>'  '+key).join('\n'));process.exitCode=1;}
else {
 try {
  const origin=validateOrigin(process.env.EXPO_PUBLIC_GIFTWISE_WEB_URL);
  const host=new URL(origin).hostname;
  if(host==='localhost'||host.endsWith('.example')||host.endsWith('.test')||host.endsWith('.invalid'))throw Error('Use your deployed HTTPS origin, not a placeholder.');
  for(const key of ['GIFTWISE_IOS_BUNDLE_ID','GIFTWISE_ANDROID_PACKAGE'])if(!/^[A-Za-z][\w]*(\.[A-Za-z][\w]*){2,}$/.test(process.env[key]))throw Error(key+' must be a valid reverse-domain application identifier.');
  if(!/^[\da-f]{8}(-[\da-f]{4}){3}-[\da-f]{12}$/i.test(process.env.GIFTWISE_EAS_PROJECT_ID))throw Error('EAS project ID must be a project UUID.');
  console.log('Public build configuration passes local validation. EAS account, signing credentials, deployment, and device QA must still be checked.');
 }catch(err){console.error(err.message);process.exitCode=1;}
}
