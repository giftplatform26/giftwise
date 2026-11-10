'use client';
import dynamic from 'next/dynamic';
const Editor=dynamic(()=>import('../../components/InvitationEditor'),{loading:()=> <main role="status">Loading invitation designer…</main>});
export default function Studio(){return <Editor/>}
