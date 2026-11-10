import type {MetadataRoute} from 'next';
export default function robots():MetadataRoute.Robots{return {rules:{userAgent:'*',disallow:['/admin','/host','/events','/dashboard','/studio','/demo','/memories/','/gifts','/e/','/groups/','/login','/register','/forgot-password','/forgot-username','/verify-email','/backend/']}}}
