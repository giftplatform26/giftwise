// Search-friendly landing pages: one per occasion or popular need, written for Indian celebrations.
export type OccasionPage = {
  slug: string; label: string; title: string; description: string; kicker: string; heading: string; intro: string;
  features: { title: string; copy: string }[]; steps: string[]; faqs: [string, string][];
};

export const occasionPages: OccasionPage[] = [
  {
    slug: 'wedding-invitation-and-gift-registry', label: 'Weddings', kicker: 'WEDDINGS',
    title: 'Online wedding invitation, RSVP & gift registry | GiftWise',
    description: 'Create a beautiful online wedding invitation with function-wise RSVP, WhatsApp invites, a gift wishlist, direct UPI gifts with receipts, a live wall and a digital shagun register.',
    heading: 'Your wedding, every function, one beautiful invitation.',
    intro: 'From haldi to reception, give every function its own time, venue and RSVP. Guests answer once, choose their meal, and send gifts straight to your account.',
    features: [
      { title: 'Function-wise schedule & RSVP', copy: 'Haldi, mehendi, sangeet, wedding and reception — each with its venue, dress code and headcount.' },
      { title: 'WhatsApp invitations & replies', copy: 'Send the invitation on WhatsApp; guests reply YES 3 and you see the count instantly.' },
      { title: 'Gift registry & group gifts', copy: 'A wishlist from any store, group gifting for big wishes, and honeymoon or charity wishes.' },
      { title: 'Shagun register & live wall', copy: 'Record envelopes at the gift desk and put guests’ wishes on the big screen.' },
      { title: 'Seating, passes & check-in', copy: 'Seat every family, send QR passes, and check guests in at the door in seconds.' },
      { title: 'Co-hosts for the family', copy: 'Let siblings and parents help — with only the tools you allow.' },
    ],
    steps: ['Pick a wedding design and add your functions', 'Add your wishlist and receiving account', 'Send the invitation on WhatsApp', 'Run the day with check-in, the gift desk and the live wall'],
    faqs: [
      ['Can guests RSVP separately for each function?', 'Yes. Guests see your schedule and answer for each function, with the number of people and their meal choice.'],
      ['Do gifts go to my account?', 'Yes. UPI and bank gifts go directly to your account with a payment code and a receipt for the guest. GiftWise never holds the money.'],
      ['Can my wedding planner set it up for us?', 'Yes. Planners can create the celebration and hand it over to you with one link, then stay on to help.'],
    ],
  },
  {
    slug: 'birthday-invitation-and-wishlist', label: 'Birthdays', kicker: 'BIRTHDAYS',
    title: 'Online birthday invitation with RSVP & wishlist | GiftWise',
    description: 'Make a birthday invitation in a minute: RSVP, a wishlist with group gifting, Status cards for WhatsApp, a live photo wall and return-gift tracking.',
    heading: 'Birthday invitations kids and grandparents both love.',
    intro: 'A cheerful invitation, a wishlist so nobody brings the fifth teddy bear, and a reminder to celebrate again next year.',
    features: [
      { title: 'Ready in a minute', copy: 'Try it free without signing up — see the invitation come alive as you type.' },
      { title: 'A wishlist that prevents duplicates', copy: 'Guests see what’s already gifted, or chip in together for the big one.' },
      { title: 'Countdown cards for Status', copy: 'Share “5 days to go” and thank-you cards on WhatsApp Status and Instagram.' },
      { title: 'Photos and wishes on the screen', copy: 'Guests scan, post a photo and a wish, and watch it appear live.' },
      { title: 'Return gifts, sorted', copy: 'Tick off return gifts as families leave.' },
      { title: 'Next year, one tap', copy: 'We’ll remind you before the next birthday and copy last year’s details and guests.' },
    ],
    steps: ['Choose a birthday design', 'Add a few wishes', 'Share the link or QR', 'Enjoy the party — we’ll handle the replies'],
    faqs: [
      ['Is it free?', 'You can start for free, and guests never pay to open your invitation, RSVP or send a wish.'],
      ['Do guests need an app?', 'No. The invitation opens in any phone browser.'],
    ],
  },
  {
    slug: 'housewarming-invitation', label: 'Housewarmings', kicker: 'NEW HOMES',
    title: 'Housewarming (griha pravesh) invitation & gift wishlist | GiftWise',
    description: 'Invite family to your griha pravesh with an online invitation, a wishlist for your new home, group gifting for furniture and appliances, and directions for guests.',
    heading: 'A new home deserves gifts you’ll actually use.',
    intro: 'Skip the showpieces. Share a wishlist for the dining table, the mixer or the sofa, and let friends make it happen together.',
    features: [
      { title: 'Wishlist from any store', copy: 'Add items from Amazon, Flipkart, IKEA or any link, with photos and prices.' },
      { title: 'Group gifting for big items', copy: 'Friends pool together for the sofa or the dining table you really need.' },
      { title: 'Directions and timings', copy: 'Share the puja time, address and a map link in one place.' },
      { title: 'Thank-you cards', copy: 'Thank everyone personally once you’ve settled in.' },
    ],
    steps: ['Create your housewarming', 'Add the things your home needs', 'Share the invitation', 'Thank everyone afterwards'],
    faqs: [['Can guests contribute part of a gift?', 'Yes — guests can buy a whole gift, contribute part of it, or join a group gift.']],
  },
  {
    slug: 'baby-shower-invitation', label: 'Baby showers', kicker: 'LITTLE BEGINNINGS',
    title: 'Baby shower (godh bharai) invitation & baby registry | GiftWise',
    description: 'A sweet online invitation for your baby shower or godh bharai, with a baby registry, group gifts, RSVP and a memory album.',
    heading: 'Less stuff. More of what the little one needs.',
    intro: 'A baby registry with the crib, the pram and the essentials — and a private album of every blessing.',
    features: [
      { title: 'A baby registry', copy: 'Add the essentials from any store and let family choose or chip in.' },
      { title: 'Blessings and photos', copy: 'Guests leave wishes and photos in a private memory album.' },
      { title: 'Simple RSVP', copy: 'Know who’s coming without a single phone call.' },
    ],
    steps: ['Create the shower', 'Add your registry', 'Share with family', 'Keep every blessing'],
    faqs: [['Is the album private?', 'Yes. Your memory album is visible only to you and the people you choose.']],
  },
  {
    slug: 'anniversary-celebration', label: 'Anniversaries', kicker: 'ANNIVERSARIES',
    title: 'Anniversary party invitation & wishes | GiftWise',
    description: 'Celebrate an anniversary with an elegant online invitation, RSVPs, a wall of wishes, a printable memory book and yearly reminders.',
    heading: 'Years of love, celebrated beautifully.',
    intro: 'An elegant invitation, wishes from everyone who matters, and a printable memory book to keep.',
    features: [
      { title: 'Elegant designs', copy: 'Invitations that suit a silver jubilee as much as a first anniversary.' },
      { title: 'A wall of wishes', copy: 'Family near and far send wishes and photos.' },
      { title: 'A printable memory book', copy: 'Every wish and photo in a keepsake you can print.' },
      { title: 'Livestream for family abroad', copy: 'Add a Watch live button to the invitation.' },
    ],
    steps: ['Create the celebration', 'Invite family', 'Collect wishes', 'Print the memory book'],
    faqs: [['Can relatives abroad join?', 'Yes — add a livestream link and they’ll see a Watch live button on the invitation.']],
  },
  {
    slug: 'office-farewell-group-gift', label: 'Office farewells', kicker: 'OFFICES & TEAMS',
    title: 'Collect money for an office farewell or group gift | GiftWise',
    description: 'Start a group gift for a colleague’s farewell, birthday or new baby in under a minute. Everyone pays you by UPI with a receipt, and leaves a message for the card.',
    heading: 'The easiest way to collect for a colleague’s gift.',
    intro: 'No spreadsheets, no chasing. Share one link on the team chat; everyone pays by UPI, gets a receipt and adds a message to the card.',
    features: [
      { title: 'Ready in under a minute', copy: 'Who it’s for, the target and a closing date — that’s it.' },
      { title: 'UPI straight to the organiser', copy: 'Contributions go directly to your account with a payment code and receipt.' },
      { title: 'Progress without awkwardness', copy: 'Show the total collected without anyone’s individual amount.' },
      { title: 'A card from everyone', copy: 'Each person adds a message for the farewell card.' },
    ],
    steps: ['Start a group gift', 'Share the link on the team chat', 'Confirm payments as they arrive', 'Present the gift and the card'],
    faqs: [
      ['Do colleagues see how much others paid?', 'No. You can show overall progress, but individual amounts stay private to the organiser.'],
      ['Do I need to create an event?', 'No. Group gifts don’t need an event or an invitation.'],
    ],
  },
  {
    slug: 'digital-shagun-register', label: 'Shagun register', kicker: 'AT THE GIFT DESK',
    title: 'Digital shagun / moi register for weddings and functions | GiftWise',
    description: 'Replace the paper notebook at the gift desk: record envelopes, cash and gifts in seconds, even on weak Wi-Fi, see totals by family and export to Excel.',
    heading: 'The gift-desk notebook, finally digital.',
    intro: 'Record every envelope and gift in seconds — with one-tap amounts, family groups and an Excel export. It even works when the venue Wi-Fi doesn’t.',
    features: [
      { title: 'One-tap amounts', copy: '₹501, ₹1,100, ₹2,100… tap and move to the next family.' },
      { title: 'Works on weak Wi-Fi', copy: 'Entries save on the phone and sync when the connection returns — never twice.' },
      { title: 'Totals by family', copy: 'See bride side, groom side or any group you choose.' },
      { title: 'Your lifelong gift ledger', copy: 'Remember what each family gave — and what you gave them — for the next wedding.' },
    ],
    steps: ['Switch on the gift register', 'Hand the phone to your gift-desk helper', 'Record envelopes as they arrive', 'Download the list at the end of the night'],
    faqs: [['Can a relative manage the gift desk?', 'Yes. Invite them as a gift-desk helper: they can add entries but can’t edit, delete or export.']],
  },
  {
    slug: 'whatsapp-invitation-rsvp', label: 'WhatsApp invitations', kicker: 'WHATSAPP',
    title: 'Send invitations and collect RSVPs on WhatsApp | GiftWise',
    description: 'Send your invitation, reminders and thank-you messages on WhatsApp. Guests reply YES, NO or MAYBE and you see every answer in one place.',
    heading: 'Invitations and RSVPs, right inside WhatsApp.',
    intro: 'Paste your guest list, send the invitation, and let guests reply in the chat they already use. Reminders and thank-yous follow with one tap.',
    features: [
      { title: 'Paste your guest list', copy: 'Names and numbers from a spreadsheet — Indian numbers without +91 are fine.' },
      { title: 'Replies that count themselves', copy: '“YES 3”, “haan”, “maybe” — every reply becomes an RSVP.' },
      { title: 'Reminders and thank-yous', copy: 'Remind only the guests who haven’t replied, then thank everyone who came.' },
      { title: 'Respectful by design', copy: 'Guests can reply STOP, and we never message them again.' },
    ],
    steps: ['Paste names and numbers', 'Send the invitation', 'Watch replies arrive', 'Send a reminder to the rest'],
    faqs: [['Is it the official WhatsApp Business service?', 'Yes. Messages use approved WhatsApp Business templates.']],
  },
];

export function occasionBySlug(slug: string) { return occasionPages.find(o => o.slug === slug); }
