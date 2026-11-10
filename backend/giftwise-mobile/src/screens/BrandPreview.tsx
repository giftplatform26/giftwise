import React, { useEffect, useState } from 'react';
import { BackHandler, Modal, Pressable, ScrollView, Share, StyleSheet, Switch, Text, TextInput, useColorScheme, View } from 'react-native';
import { StatusBar } from 'expo-status-bar';
import { SafeAreaProvider, SafeAreaView } from 'react-native-safe-area-context';
import { BrandMark, Miri } from '../components/Brand';
import { palette, Theme, themes } from '../theme/tokens';
import { Celebration, giftIdeas, initialCelebrations, onboarding } from '../data/demo';

type Tab = 'Today' | 'Wishes' | 'Memories' | 'You';
type Sheet = 'create' | 'detail' | 'contribute' | 'premium' | 'referral' | null;
export default function App() { return <SafeAreaProvider><GiftWise /></SafeAreaProvider>; }
function GiftWise() {
  const systemDark = useColorScheme() === 'dark';
  const [dark, setDark] = useState(systemDark);
  const theme: Theme = dark ? themes.dark : themes.light;
  const [intro, setIntro] = useState(0);
  const [welcomed, setWelcomed] = useState(false);
  const [tab, setTab] = useState<Tab>('Today');
  const [sheet, setSheet] = useState<Sheet>(null);
  const [events, setEvents] = useState(initialCelebrations);
  const [selected, setSelected] = useState(initialCelebrations[0]);
  const [title, setTitle] = useState('');
  const [occasion, setOccasion] = useState('Birthday');
  const [amount, setAmount] = useState('500');
  const [anonymous, setAnonymous] = useState(false);
  const [feedback, setFeedback] = useState('');
  const [contributions, setContributions] = useState(0);
  const [giftId, setGiftId] = useState('camera');
  const gift = giftIdeas.find(item => item.id === giftId)!;
  const [error, setError] = useState('');
  const [wishText, setWishText] = useState('');
  const [wishes, setWishes] = useState<string[]>([]);
  useEffect(() => { if (!feedback) return; const timer = setTimeout(() => setFeedback(''), 4500); return () => clearTimeout(timer); }, [feedback]);
  useEffect(() => { const sub = BackHandler.addEventListener('hardwareBackPress', () => { if (sheet) { setSheet(null); return true; } if (tab !== 'Today') { setTab('Today'); return true; } return false; }); return () => sub.remove(); }, [sheet, tab]);
  function open(next: Sheet) { setError(''); setSheet(next); }
  const body = { color: theme.text };
  const muted = { color: theme.muted };
  const surface = { backgroundColor: theme.surface, borderColor: theme.border };
  function Button({ label, onPress, secondary = false }: { label: string; onPress: () => void; secondary?: boolean }) {
    return <Pressable accessibilityRole="button" onPress={onPress} style={({ pressed }) => [s.button, { backgroundColor: secondary ? theme.tint : theme.primary, opacity: pressed ? .75 : 1 }]}><Text style={[s.buttonText, { color: secondary ? theme.text : theme.onPrimary }]}>{label}</Text></Pressable>;
  }
  function Heading({ eyebrow, children }: { eyebrow?: string; children: React.ReactNode }) { return <View style={s.heading}>{eyebrow && <Text style={[s.eyebrow, muted]}>{eyebrow}</Text>}<Text accessibilityRole="header" style={[s.h2, body]}>{children}</Text></View>; }
  const inputStyle = [s.input, surface, body, { borderColor: theme.muted }];
  function shareReferral() {
    Share.share({ message: 'Every celebration, thoughtfully together. Explore GiftWise. My preview invite code: JOY-MAYA. (Demo invitation; no live referral reward.)' }).catch(() => setFeedback('Sharing is unavailable. Your demo code is JOY-MAYA.'));
  }
  if (!welcomed) {
    const slide = onboarding[intro];
    return <SafeAreaView style={[s.root, { backgroundColor: theme.background }]}><StatusBar style={dark ? 'light' : 'dark'} /><ScrollView contentContainerStyle={s.intro}>
      <View style={s.brand}><BrandMark size={38}/><Text style={[s.wordmark, body]}>GiftWise</Text></View>
      <View style={s.introArt}><View style={s.orbit}/><Text style={s.sparkOne}>✦</Text><Miri size={180}/><Text style={s.sparkTwo}>✧</Text></View>
      <Text style={[s.eyebrow, muted]}>{slide.eyebrow}</Text><Text accessibilityRole="header" style={[s.display, body]}>{slide.title}</Text><Text style={[s.introBody, muted]}>{slide.body}</Text>
      <View accessibilityLabel={`Page ${intro + 1} of 3`} style={s.dots}>{onboarding.map((_, index) => <View key={index} style={[s.dot, { width: index === intro ? 28 : 8, backgroundColor: index === intro ? theme.primary : theme.border }]}/>)}</View>
      <Button label={intro === 2 ? 'Start celebrating' : 'Continue →'} onPress={() => intro === 2 ? setWelcomed(true) : setIntro(intro + 1)}/>
      <Pressable accessibilityRole="button" style={s.textButton} onPress={() => setWelcomed(true)}><Text style={muted}>Explore GiftWise</Text></Pressable>
      <Text style={[s.caption, muted]}>Interactive brand preview · Sample data only</Text>
    </ScrollView></SafeAreaView>;
  }
  return <SafeAreaView style={[s.root, { backgroundColor: theme.background }]}><StatusBar style={dark ? 'light' : 'dark'}/>
    <View style={s.topbar}><View style={s.brand}><BrandMark size={34}/><Text style={[s.wordmark, body]}>GiftWise</Text></View><Pressable accessibilityRole="button" accessibilityLabel="Open your profile" onPress={() => setTab('You')} style={[s.avatar, { backgroundColor: theme.tint }]}><Text style={[s.bold, body]}>M</Text></Pressable></View>
    <ScrollView contentContainerStyle={s.content} keyboardShouldPersistTaps="handled">
      <View style={[s.preview, { backgroundColor: theme.tint }]}><Text style={[s.caption, body]}>BRAND PREVIEW · Changes stay in this session</Text></View>
      {tab === 'Today' && <>
        <Text style={[s.eyebrow, muted]}>A LITTLE THOUGHT. A LOT OF JOY.</Text><Text accessibilityRole="header" style={[s.display, body]}>Good things{ '\n' }bring us together.</Text>
        <View style={s.hero}><View style={{ flex: 1 }}><Text style={s.heroEyebrow}>YOUR NEXT BEAUTIFUL MOMENT</Text><Text style={s.heroTitle}>Make room{ '\n' }for celebration.</Text><Text style={s.heroBody}>The wishes, the people, the little details. All together.</Text></View><Miri size={100}/></View>
        <Button label="＋  Create a celebration" onPress={() => open('create')}/>
        <View style={s.sectionRow}><Text accessibilityRole="header" style={[s.h2, body]}>Coming together</Text><Text style={[s.caption, muted]}>{events.length} celebrations</Text></View>
        {events.map(event => <Pressable accessibilityRole="button" accessibilityLabel={`Open ${event.occasion}`} key={event.id} onPress={() => { setSelected(event); open('detail'); }} style={[s.eventCard, surface]}>
          <View style={[s.eventArt, { backgroundColor: event.tint }]}><Text style={s.eventMotif}>{event.motif}</Text><View style={s.datePill}><Text style={s.dateText}>{event.date}</Text></View></View>
          <View style={s.cardInner}><Text style={[s.eyebrow, muted]}>{event.occasion}</Text><Text style={[s.h3, body]}>{event.title}</Text><View style={s.sectionRow}><Text style={[s.small, muted]}>{event.guests} people, one lovely moment</Text><Text style={{ color: theme.primary }}>↗</Text></View></View>
        </Pressable>)}
        <View style={[s.note, { backgroundColor: theme.tint }]}><Text style={[s.h3, body]}>A gift is more than a thing.</Text><Text style={[s.paragraph, muted]}>It’s “I know you.” “I’m here.” “Let’s celebrate.”</Text></View>
      </>}
      {tab === 'Wishes' && <>
        <Heading eyebrow="WISHES & TOGETHER GIFTS">A little inspiration.{ '\n' }A lot of possibility.</Heading><Text style={[s.paragraph, muted]}>Choose something meaningful, or add a wish of your own.</Text>
        {giftIdeas.map(item => <View key={item.id} style={[s.card, surface]}><View style={[s.giftArt, { backgroundColor: item.tint }]}><Text style={s.giftMotif}>{item.motif}</Text></View><Text style={[s.caption, muted]}>{item.detail}</Text><Text style={[s.h3, body]}>{item.name}</Text><View accessibilityRole="progressbar" accessibilityValue={{ min: 0, max: item.goal, now: item.raised }} style={[s.track, { backgroundColor: theme.tint }]}><View style={[s.fill, { width: `${item.raised / item.goal * 100}%`, backgroundColor: theme.primary }]}/></View><Text style={[s.small, muted]}>₹{item.raised.toLocaleString('en-IN')} of ₹{item.goal.toLocaleString('en-IN')} · Sample progress</Text><Button label="Give together" onPress={() => { setGiftId(item.id); open('contribute'); }}/></View>)}
        <Heading>Your Wishbook</Heading><TextInput accessibilityLabel="New wish" placeholder="Something you’d love…" placeholderTextColor={theme.muted} value={wishText} onChangeText={setWishText} style={inputStyle}/><Button secondary label="Add a wish" onPress={() => { if (!wishText.trim()) { setFeedback('Add a name for your wish first.'); return; } setWishes([...wishes, wishText.trim()]); setWishText(''); setFeedback('A lovely wish, saved to your preview.'); }}/>
        {!wishes.length ? <Text style={[s.paragraph, muted]}>Your next lovely thing starts here. Add your first wish.</Text> : wishes.map((wish, i) => <View key={i} style={[s.note, surface]}><Text style={body}>✦  {wish}</Text></View>)}
      </>}
      {tab === 'Memories' && <>
        <Heading eyebrow="KEEPSAKES">Some moments{ '\n' }deserve to stay.</Heading><View style={[s.empty, surface]}><Miri size={130}/><Text style={[s.h2, body]}>A home for your happy days.</Text><Text style={[s.paragraph, muted]}>Your celebration photos and notes will belong here. Memory uploads are planned for a future release.</Text><Button label="Plan a new memory" onPress={() => open('create')}/></View><Text style={[s.paragraph, muted]}>Designed for private sharing: hosts choose who can view and contribute. No public photo feed by default.</Text>
      </>}
      {tab === 'You' && <>
        <Heading eyebrow="YOUR LITTLE CORNER">Hello, Maya.</Heading><Text style={[s.paragraph, muted]}>Here’s to the people and moments that matter.</Text><View style={[s.card, surface]}><Text style={[s.h3, body]}>Your celebration style</Text><View style={s.sectionRow}><Text style={body}>Dark appearance</Text><Switch accessibilityLabel="Dark appearance" value={dark} onValueChange={setDark} trackColor={{ true: palette.purple }}/></View><Text style={[s.caption, muted]}>Appearance and profile are preview settings.</Text></View>
        <View style={s.premiumCard}><Text style={s.heroEyebrow}>GIFTWISE PLUS</Text><Text style={s.heroTitle}>A little extra magic.</Text><Text style={s.heroBody}>Thoughtful details for your biggest days.</Text><Button label="Explore Plus" secondary onPress={() => open('premium')}/></View>
        <Button label="Invite your circle" secondary onPress={() => open('referral')}/><Button label="Revisit the welcome experience" secondary onPress={() => { setIntro(0); setWelcomed(false); }}/>
        <Text style={[s.caption, muted]}>GiftWise mobile · Design prototype 0.1{ '\n' }No account, payment, or backend connection.</Text>
      </>}
    </ScrollView>
    {!!feedback && <View accessibilityLiveRegion="polite" style={[s.toast, { backgroundColor: theme.text }]}><Text style={{ color: theme.background }}>{feedback}</Text></View>}
    <View style={[s.nav, surface]}>{(['Today', 'Wishes', 'Memories', 'You'] as Tab[]).map((name, i) => <Pressable accessibilityRole="tab" accessibilityState={{ selected: tab === name }} key={name} onPress={() => setTab(name)} style={[s.navItem, tab === name && { backgroundColor: theme.tint }]}><Text style={[s.navIcon, { color: tab === name ? theme.primary : theme.muted }]}>{['✦', '♡', '▧', '○'][i]}</Text><Text style={[s.caption, { color: tab === name ? theme.primary : theme.muted, fontWeight: tab === name ? '700' : '400' }]}>{name}</Text></Pressable>)}</View>
    <Modal animationType="slide" visible={sheet !== null} onRequestClose={() => setSheet(null)} presentationStyle="pageSheet"><SafeAreaView style={[s.root, { backgroundColor: theme.background }]}><View style={s.sheetBar}><Text style={[s.eyebrow, muted]}>GIFTWISE / {sheet?.toUpperCase()}</Text><Pressable accessibilityRole="button" accessibilityLabel="Close screen" onPress={() => setSheet(null)} style={s.close}><Text style={[s.h2, body]}>×</Text></Pressable></View><ScrollView contentContainerStyle={s.content} keyboardShouldPersistTaps="handled">
      {sheet === 'create' && <><Heading eyebrow="CELEBRATION SPACE">What are we celebrating?</Heading><Text style={[s.paragraph, muted]}>Give your moment a name. You can make it your own as you go.</Text><Text style={[s.label, body]}>Celebration name</Text><TextInput autoFocus maxLength={80} accessibilityLabel="Celebration name" value={title} onChangeText={setTitle} placeholder="e.g. Maya’s birthday" placeholderTextColor={theme.muted} style={inputStyle}/><Text style={[s.label, body]}>Occasion</Text><View style={s.chips}>{['Birthday','Wedding','Baby shower','Housewarming','Anniversary','Festival','Corporate','Other'].map(item => <Pressable accessibilityRole="radio" accessibilityState={{ checked: occasion === item }} key={item} onPress={() => setOccasion(item)} style={[s.chip, { backgroundColor: occasion === item ? theme.primary : theme.tint }]}><Text style={{ color: occasion === item ? theme.onPrimary : theme.text }}>{item}</Text></Pressable>)}</View>{!!error && <Text accessibilityLiveRegion="polite" style={{ color: theme.error }}>{error}</Text>}<Button label="Create my celebration" onPress={() => { if (!title.trim()) { setError('Give your celebration a name to get started.'); return; } const event: Celebration = { id: String(Date.now()), title: 'A beautiful moment, in the making.', occasion: title.trim(), date: 'NEW', guests: 0, motif: '✦', tint: palette.lavender }; setEvents([event, ...events]); setTitle(''); setTab('Today'); setSheet(null); setFeedback(`${occasion} celebration created in your preview.`); }}/><Text style={[s.caption, muted]}>Preview only. Dates, invitations, and publishing require backend integration.</Text></>}
      {sheet === 'detail' && <><View style={[s.detailArt, { backgroundColor: selected.tint }]}><Miri size={130}/></View><Heading eyebrow={selected.occasion}>{selected.title}</Heading><Text style={[s.paragraph, muted]}>{selected.guests} people · {selected.date === 'NEW' ? 'Date to be decided' : selected.date}{ '\n' }An invitation to give, gather, and make memories.</Text><Button label="Explore the wishes" onPress={() => { setSheet(null); setTab('Wishes'); }}/><Button secondary label="Share a celebration note" onPress={() => { Share.share({ message: `You’re invited to ${selected.occasion}! Created with GiftWise. (Preview note — no live event link.)` }).catch(() => setError('Sharing is unavailable on this device.')); }}/>{!!error && <Text style={{ color: theme.error }}>{error}</Text>}<View style={[s.note, { backgroundColor: theme.tint }]}><Text style={[s.h3, body]}>Everyone belongs here.</Text><Text style={[s.paragraph, muted]}>A kind note, a shared gift, or simply showing up. There’s no right amount of generosity.</Text></View></>}
      {sheet === 'contribute' && <><Heading eyebrow="TOGETHER GIFTS">A part of something lovely.</Heading><Text style={[s.h3, body]}>{gift.name}</Text><Text style={[s.paragraph, muted]}>Choose an amount that feels right for you. This preview never collects or transfers money.</Text><Text style={[s.label, body]}>Contribution amount (INR)</Text><TextInput accessibilityLabel="Contribution amount in rupees" value={amount} onChangeText={setAmount} keyboardType="decimal-pad" style={inputStyle}/><View style={s.sectionRow}><Text style={[s.small, body]}>Hide my name from other guests</Text><Switch accessibilityLabel="Hide my name from other guests" value={anonymous} onValueChange={setAnonymous}/></View><Text style={[s.caption, muted]}>In a live service, the host and payment provider may still see your identity. Audience visibility must be shown before payment.</Text><View style={[s.note, surface]}><Text style={[s.label, body]}>Preview summary</Text><Text style={muted}>No charge · No fees · No payment details requested</Text></View>{!!error && <Text accessibilityLiveRegion="polite" style={{ color: theme.error }}>{error}</Text>}<Button label="Preview contribution confirmation" onPress={() => { const parsed = Number(amount); if (!/^\d+(\.\d{1,2})?$/.test(amount) || !Number.isFinite(parsed) || parsed <= 0 || parsed > 100000) { setError('Enter an amount from ₹0.01 to ₹1,00,000, with up to 2 decimal places.'); return; } setContributions(contributions + 1); setSheet(null); setFeedback(`Preview saved: ₹${parsed.toLocaleString('en-IN')} ${anonymous ? 'with your name hidden from guests' : 'from you'}. No money moved.`); }}/><Text style={[s.caption, muted]}>{contributions} confirmations explored in this session. Gift totals above remain sample data.</Text></>}
      {sheet === 'premium' && <><View style={s.center}><Miri size={120}/></View><Heading eyebrow="GIFTWISE PLUS / CONCEPT">For the moments you make your own.</Heading><Text style={[s.paragraph, muted]}>A proposed membership for thoughtful hosts.</Text>{['Beautiful celebration themes','Custom celebration covers','Longer memory storage','Expanded planning tools'].map(item => <View key={item} style={[s.note, surface]}><Text style={body}>✦  {item}</Text></View>)}<Text style={[s.h3, body]}>Pricing coming after validation</Text><Text style={[s.paragraph, muted]}>These benefits are concepts. No subscription is sold in this preview. Clear records, contribution visibility, and essential account controls belong to everyone.</Text><Button label="Preview interest confirmation" onPress={() => { setSheet(null); setFeedback('Thanks for exploring Plus. No subscription or waitlist entry was created.'); }}/></>}
      {sheet === 'referral' && <><View style={s.center}><Miri size={120}/></View><Heading eyebrow="GROW THE JOY">Good things deserve good company.</Heading><Text style={[s.paragraph, muted]}>Invite your people to imagine more meaningful celebrations.</Text><View style={[s.code, { backgroundColor: theme.tint }]}><Text selectable style={[s.h2, body]}>JOY-MAYA</Text><Text style={[s.caption, muted]}>DEMO INVITE CODE</Text></View><Button label="Share an invitation" onPress={shareReferral}/><Text style={[s.paragraph, muted]}>Referral concept: an eligible new host publishes their first celebration, then both receive a theme credit. Limits and eligibility would be shown before enrollment. No rewards are active in this preview.</Text></>}
    </ScrollView></SafeAreaView></Modal>
  </SafeAreaView>;
}
const s = StyleSheet.create({
  root: { flex: 1 }, content: { padding: 24, gap: 16, paddingBottom: 40, width: '100%', maxWidth: 680, alignSelf: 'center' },
  intro: { flexGrow: 1, padding: 28, gap: 20, maxWidth: 600, width: '100%', alignSelf: 'center', justifyContent: 'center' },
  brand: { flexDirection: 'row', alignItems: 'center', gap: 10 }, wordmark: { fontSize: 24, fontWeight: '700', letterSpacing: -.8 },
  topbar: { paddingHorizontal: 24, paddingVertical: 12, flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  avatar: { height: 44, width: 44, borderRadius: 22, alignItems: 'center', justifyContent: 'center' }, bold: { fontWeight: '700' },
  preview: { paddingHorizontal: 12, paddingVertical: 8, borderRadius: 10 }, eyebrow: { fontSize: 11, letterSpacing: 1.7, fontWeight: '700', lineHeight: 18 },
  display: { fontSize: 37, lineHeight: 44, fontWeight: '700', letterSpacing: -1.5 }, h2: { fontSize: 25, lineHeight: 32, fontWeight: '700', letterSpacing: -.6 },
  h3: { fontSize: 20, lineHeight: 27, fontWeight: '600', letterSpacing: -.4 }, paragraph: { fontSize: 16, lineHeight: 25 }, small: { fontSize: 14, lineHeight: 21 }, caption: { fontSize: 12, lineHeight: 18 },
  heading: { gap: 10, marginTop: 8 }, label: { fontSize: 15, fontWeight: '600', lineHeight: 22 },
  hero: { backgroundColor: palette.purpleDeep, borderRadius: 28, padding: 24, flexDirection: 'row', alignItems: 'center', gap: 12, overflow: 'hidden' }, heroEyebrow: { color: palette.gold, fontSize: 10, lineHeight: 17, letterSpacing: 1.4, fontWeight: '700' }, heroTitle: { color: '#FFFFFF', fontSize: 27, lineHeight: 33, fontWeight: '600', letterSpacing: -.6, marginVertical: 12 }, heroBody: { color: '#E8DBF7', fontSize: 14, lineHeight: 22 },
  button: { minHeight: 52, borderRadius: 16, padding: 16, justifyContent: 'center', alignItems: 'center' }, buttonText: { fontSize: 16, fontWeight: '600', textAlign: 'center' }, textButton: { minHeight: 48, alignItems: 'center', justifyContent: 'center' },
  sectionRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', gap: 12, flexWrap: 'wrap' },
  eventCard: { borderRadius: 24, borderWidth: 1, overflow: 'hidden' }, eventArt: { height: 130, alignItems: 'center', justifyContent: 'center' }, eventMotif: { fontSize: 82, color: palette.purpleDeep }, datePill: { position: 'absolute', top: 16, left: 16, padding: 8, borderRadius: 10, backgroundColor: '#FFFFFF' }, dateText: { color: palette.purpleDeep, fontWeight: '700', fontSize: 11, letterSpacing: 1 }, cardInner: { padding: 20, gap: 10 },
  card: { borderRadius: 24, borderWidth: 1, padding: 20, gap: 16 }, note: { padding: 20, borderRadius: 20, gap: 8 }, empty: { borderWidth: 1, padding: 28, borderRadius: 28, gap: 24, alignItems: 'center' },
  giftArt: { height: 135, borderRadius: 16, alignItems: 'center', justifyContent: 'center' }, giftMotif: { fontSize: 74, color: palette.purpleDeep }, track: { height: 8, borderRadius: 4, overflow: 'hidden' }, fill: { height: 8, borderRadius: 4 },
  nav: { flexDirection: 'row', padding: 8, gap: 4, borderTopWidth: 1 }, navItem: { flex: 1, paddingVertical: 9, borderRadius: 16, alignItems: 'center', gap: 3, minHeight: 54 }, navIcon: { fontSize: 23, lineHeight: 26 },
  input: { borderRadius: 16, borderWidth: 1, padding: 16, minHeight: 54, fontSize: 16 }, chips: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 }, chip: { paddingHorizontal: 16, paddingVertical: 14, borderRadius: 30, minHeight: 48 },
  toast: { marginHorizontal: 16, padding: 16, borderRadius: 16, marginBottom: 8 }, sheetBar: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', paddingLeft: 24, paddingRight: 16 }, close: { minWidth: 48, minHeight: 48, alignItems: 'center', justifyContent: 'center' }, detailArt: { height: 210, borderRadius: 28, justifyContent: 'center', alignItems: 'center' }, center: { alignItems: 'center', padding: 16 }, premiumCard: { backgroundColor: palette.purpleDeep, borderRadius: 28, padding: 24, gap: 12 }, code: { padding: 32, borderRadius: 24, gap: 8, alignItems: 'center' },
  introArt: { height: 260, justifyContent: 'center', alignItems: 'center' }, orbit: { position: 'absolute', width: 240, height: 240, borderRadius: 120, backgroundColor: palette.lavender }, sparkOne: { position: 'absolute', top: 24, right: 30, fontSize: 40, color: palette.goldInk }, sparkTwo: { position: 'absolute', bottom: 30, left: 20, fontSize: 44, color: palette.purple }, introBody: { fontSize: 17, lineHeight: 27 }, dots: { flexDirection: 'row', gap: 8, marginVertical: 8 }, dot: { height: 8, borderRadius: 4 },
});
