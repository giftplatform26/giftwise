import Login from '../../login/page';
export default function PlatformAdminLogin() {
  return <div className="gwAdminLogin"><div className="gwAdminLoginNotice"><strong>GiftWise · Platform administration</strong><span>Use your existing platform administrator account. Permissions are verified by the server.</span><a href="/login">Host sign in →</a></div><Login/></div>;
}
