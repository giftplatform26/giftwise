'use client';
import { createContext, useContext } from 'react';
import type { DemoGift } from '@/lib/demo-data';
import type { InfoKind } from './Dialogs';
/** Opens the home page's dialogs from client islands inside otherwise server-rendered sections. */
export type MarketingActions = { openGift: (gift: DemoGift) => void; openWalkthrough: () => void; openInfo: (kind: InfoKind) => void };
const noop = () => {};
export const MarketingContext = createContext<MarketingActions>({ openGift: noop, openWalkthrough: noop, openInfo: noop });
export const useMarketing = () => useContext(MarketingContext);
