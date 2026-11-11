import type {Metadata} from 'next';
import ReceiptClient from './ReceiptClient';
import './receipt.css';
export const metadata:Metadata={title:'Your gift receipt · GiftWise',robots:{index:false,follow:false,noarchive:true},referrer:'no-referrer'};
export default function Page(){return <ReceiptClient/>}
