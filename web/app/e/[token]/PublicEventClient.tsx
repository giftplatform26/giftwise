'use client';
import PublicInvitation,{type InvitationEvent} from '../../../components/focused/Invitation';
import './public-event.css';
export default function PublicEventClient({initialEvent=null}:{initialEvent?:InvitationEvent|null}){return <PublicInvitation initialEvent={initialEvent}/>}
