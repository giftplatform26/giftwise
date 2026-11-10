export const palette = {
  purple: '#6336B5', purpleDeep: '#35205B', gold: '#E8BC62', goldInk: '#76520E',
  lavender: '#EEE7FA', peach: '#F9DED0', mint: '#DDEFE5', cream: '#FCFAF6',
  success: '#216344', warning: '#825509', error: '#AC324A',
};
export const themes = {
  light: { background: palette.cream, surface: '#FFFFFF', text: '#292137', muted: '#6F657B', border: '#DED7E8', tint: palette.lavender, primary: palette.purple, onPrimary: '#FFFFFF', success: palette.success, error: palette.error },
  dark: { background: '#171220', surface: '#241C30', text: '#FAF6FF', muted: '#C3B7D1', border: '#51435F', tint: '#362747', primary: '#CFB4FF', onPrimary: '#281441', success: '#8ED5AC', error: '#FFA8B8' },
};
export type Theme = typeof themes.light;
export const spacing = { xs: 4, sm: 8, md: 16, lg: 24, xl: 32, xxl: 48 };
export const radius = { control: 16, card: 24, hero: 32, pill: 100 };
export const typography = { display: 38, h1: 30, h2: 24, h3: 20, body: 16, small: 14, caption: 12 };
