// Vendor directory helpers.
/** @type {[string, string][]} */
export const VENDOR_CATEGORIES = [['DECOR', 'Decor'], ['CATERING', 'Catering'], ['PHOTOGRAPHY', 'Photography'], ['MAKEUP', 'Makeup'], ['MEHENDI', 'Mehendi'],
  ['MUSIC', 'Music & DJ'], ['VENUE', 'Venues'], ['PLANNER', 'Planners'], ['INVITATIONS', 'Invitations'], ['GIFTS', 'Gifts'], ['OTHER', 'Other']];

/** Contact links built only from validated values. */
export function vendorLinks(v) {
  const phone = typeof v?.phone === 'string' && /^\+[1-9][0-9]{7,14}$/.test(v.phone) ? v.phone : null;
  const website = typeof v?.website === 'string' && /^https:\/\/[^\s<>"]+$/.test(v.website) ? v.website : null;
  const insta = typeof v?.instagram === 'string' && /^[A-Za-z0-9._]{1,30}$/.test(v.instagram) ? v.instagram : null;
  return {
    call: phone ? `tel:${phone}` : null,
    whatsapp: phone ? `https://wa.me/${phone.slice(1)}` : null,
    website,
    instagram: insta ? `https://instagram.com/${insta}` : null,
  };
}
