export type Celebration = { id: string; title: string; occasion: string; date: string; guests: number; motif: string; tint: string };
export const initialCelebrations: Celebration[] = [
  { id: 'birthday', title: 'A little older. A lot more loved.', occasion: 'Maya’s birthday', date: '24 NOV', guests: 12, motif: '✦', tint: '#EEE7FA' },
  { id: 'home', title: 'A new place for new memories.', occasion: 'Aarav & Anika’s housewarming', date: '30 NOV', guests: 8, motif: '⌂', tint: '#F9DED0' },
];
export const giftIdeas = [
  { id: 'camera', name: 'A camera for the little moments', detail: 'Instant camera · Group gift', goal: 12000, raised: 8400, motif: '◉', tint: '#F9DED0' },
  { id: 'weekend', name: 'A weekend to remember', detail: 'Experience fund · Group gift', goal: 20000, raised: 6000, motif: '✧', tint: '#EEE7FA' },
];
export const onboarding = [
  { eyebrow: '01 / MAKE IT PERSONAL', title: 'Every celebration starts with you.', body: 'A birthday, a new home, a beautiful beginning. Give your moment a place to come together.' },
  { eyebrow: '02 / GIVE TOGETHER', title: 'Many thoughtful people. One lovely gift.', body: 'Share wishes, plan a group gift, and let everyone choose how they take part.' },
  { eyebrow: '03 / KEEP THE FEELING', title: 'The day passes. The joy stays.', body: 'Bring your wishes, contributions, thank-yous, and memories into one celebration.' },
];
