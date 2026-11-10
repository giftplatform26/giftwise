'use client';
import {createContext,useContext,type ReactNode} from 'react';

// GiftWise uses a single experience: the guided "Focused" design. The former Classic
// screens and the admin toggle were retired so features no longer have to be built twice.
// `useExperience` is kept so shared components can keep their (now constant) branches.
export type ViewMode='FOCUSED';
const value={mode:'FOCUSED' as ViewMode,apply:(_mode:ViewMode)=>{}};
const ExperienceContext=createContext(value);
export const useExperience=()=>useContext(ExperienceContext);
export default function ExperienceProvider({children}:{children:ReactNode}){
 return <ExperienceContext.Provider value={value}><div className="ux-focused" data-experience="FOCUSED">{children}</div></ExperienceContext.Provider>;
}
