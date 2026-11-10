export function usernameRules(value='') { return [
 ['Be 4–30 characters', value.length>=4&&value.length<=30],
 ['Contain only letters, numbers and underscore (_)', /^[A-Za-z0-9_]+$/.test(value)],
 ['Start with a letter or number', /^[A-Za-z0-9]/.test(value)],
]; }
export function passwordRules(value='') { return [
 ['Minimum 8 characters', Array.from(value).length>=8],
 ['One uppercase letter', /[A-Z]/.test(value)],
 ['One lowercase letter', /[a-z]/.test(value)],
 ['One number', /[0-9]/.test(value)],
 ['One special character', Array.from(value).some(c=>{const n=c.charCodeAt(0);return n>=33&&n<=47||n>=58&&n<=64||n>=91&&n<=96||n>=123&&n<=126})],
 ['At most 72 UTF-8 bytes', new TextEncoder().encode(value).length<=72],
]; }
