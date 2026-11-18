import './admin-console.css';

// Platform administration is web-only; the native shell refuses /admin (see lib/native-app.mjs).
export default function AdminLayout({children}:{children:React.ReactNode}){return children;}
