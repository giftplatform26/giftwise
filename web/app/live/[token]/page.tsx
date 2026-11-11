import type {Metadata} from 'next';
import LiveWall from './LiveWall';
import './live-wall.css';
export const metadata:Metadata={title:'Live wall · GiftWise',robots:{index:false,follow:false,noarchive:true},referrer:'no-referrer'};
export default function Page(){return <LiveWall/>}
