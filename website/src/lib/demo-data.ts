export type DemoGift = {
  id: string; name: string; category: 'home' | 'dreams' | 'baby';
  price: number; funded: number; contributors: number; store: string; image: number; note: string;
};
export const gifts: DemoGift[] = [
  { id: 'dining', name: 'A seat for everyone', category: 'home', price: 48000, funded: 32000, contributors: 8, store: 'IKEA', image: 0, note: 'Oak dining table · For slow Sunday lunches' },
  { id: 'mixer', name: 'A little kitchen magic', category: 'home', price: 8500, funded: 5500, contributors: 5, store: 'Amazon', image: 1, note: 'Mixer grinder · A fresh start every morning' },
  { id: 'laptop', name: 'Room for bigger ideas', category: 'dreams', price: 75000, funded: 52500, contributors: 12, store: 'Flipkart', image: 2, note: 'Laptop · The next big chapter' },
  { id: 'sofa', name: 'Our new favourite spot', category: 'home', price: 65000, funded: 47000, contributors: 11, store: 'Any store', image: 3, note: 'Sage sofa · Made for movie nights' },
  { id: 'crib', name: 'Sweet little beginnings', category: 'baby', price: 18000, funded: 11500, contributors: 6, store: 'Amazon', image: 4, note: 'Baby crib · A cosy place to dream' },
  { id: 'vacation', name: 'Memories, not things', category: 'dreams', price: 90000, funded: 36000, contributors: 14, store: 'Wishlist fund', image: 5, note: 'Vacation fund · Our first adventure together' },
];
export const steps = [
  { title: 'Create your event', text: 'A birthday, a new home, a new beginning. Start with your moment.', short: 'Your moment' },
  { title: 'Make the invitation yours', text: 'A little personality. A personal note. An invitation that feels like you.', short: 'Your invitation' },
  { title: 'Build your wishlist', text: 'Add the things you love from Amazon, Flipkart, Myntra, IKEA or any product URL.', short: 'Your wishlist' },
  { title: 'Share your QR invite', text: 'One beautiful invitation. One simple scan. Your whole celebration.', short: 'Your QR invite' },
  { title: 'Let guests explore', text: 'Friends discover your wishlist and choose a gift that feels right.', short: 'Their choice' },
  { title: 'Make it happen together', text: 'An entire gift, a little contribution, or something extraordinary as a group.', short: 'A meaningful gift' },
];
export function inr(amount: number) { return `₹${new Intl.NumberFormat('en-IN', { maximumFractionDigits: 0 }).format(amount)}`; }
export function percent(funded: number, total: number) { return Math.min(100, Math.max(0, Math.round(funded / total * 100))); }
