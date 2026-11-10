import type {Metadata} from 'next';
import PublicLanding from '../components/PublicLanding';
export const metadata:Metadata={title:'GiftWise | Invitations and celebrations',description:'Create personal invitations and bring your people together for weddings, birthdays and special moments.',robots:{index:true,follow:true}};
export default function Home(){return <PublicLanding/>}
