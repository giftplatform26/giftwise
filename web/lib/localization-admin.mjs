import {bootstrapCsrf} from './host-api.mjs';
import {csrfToken} from './gift-api.mjs';
export async function languageAdmin(path='',method='GET',payload){
 const headers={'Accept':'application/json'};
 if(method!=='GET'){await bootstrapCsrf(fetch);const token=csrfToken(document.cookie);if(!token)throw Error('Secure session unavailable. Reload and try again.');headers['X-XSRF-TOKEN']=token;headers['Content-Type']='application/json';}
 const response=await fetch('/backend/api/admin/localization'+path,{method,credentials:'include',cache:'no-store',headers,...(payload===undefined?{}:{body:JSON.stringify(payload)})});
 if(!response.ok)throw Error(response.status===409?'Language settings changed or this feature is disabled. Reload before saving.':response.status===403?'Platform administrator access required.':response.status===400?'Check language codes, required fields and {{placeholders}}.':'Could not update language settings.');
 return response.json();
}
