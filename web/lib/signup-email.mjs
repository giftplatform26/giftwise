export function signupEmailState(email,verified=false){
 const value=String(email||'').trim();
 const validFormat=value.length>0&&value.length<=254&&/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
 const confirmed=validFormat&&verified===true;
 return {validFormat,verified:confirmed,message:!value?'Enter your email address.':!validFormat?'Enter a valid email address.':confirmed?'Email verified.':'Email format is valid. Please verify your email in Account after signup.'};
}
