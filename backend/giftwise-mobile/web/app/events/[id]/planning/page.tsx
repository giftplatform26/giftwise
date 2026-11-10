'use client';
import {useParams} from 'next/navigation';
import CelebrationSettings from '../../../../components/CelebrationSettings';
export default function Page(){const {id}=useParams();return <CelebrationSettings key={String(id)} eventId={String(id)}/>}
