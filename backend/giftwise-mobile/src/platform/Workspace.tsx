// Native implementation; Metro selects Workspace.web.tsx for browser previews.
import React, { useEffect, useRef, useState } from 'react';
import { ActivityIndicator, Alert, BackHandler, Linking, Pressable, Share, StyleSheet, Text, View } from 'react-native';
import { WebView } from 'react-native-webview';
import type { WebViewMessageEvent } from 'react-native-webview';
import * as Browser from 'expo-web-browser';
import * as Clipboard from 'expo-clipboard';
import { File, Paths } from 'expo-file-system';
import * as Sharing from 'expo-sharing';
import { nativeBridge } from './bridge';
import { navigationTarget, parseNativeMessage, isAdminPath } from './policy.mjs';
import { palette } from '../theme/tokens';
// Lets the shared web app recognise the app (gift-platform/web/lib/native-app.mjs) and keep administration web-only.
const NATIVE_USER_AGENT = 'GiftWiseApp/1';
export type WorkspaceProps = { origin: string; path: string; onClose: () => void; checkoutOrigins: string[] };
export default function Workspace({ origin, path, onClose, checkoutOrigins }: WorkspaceProps) {
  const ref = useRef<WebView<{}>>(null);
  const [back, setBack] = useState(false);
  const [current, setCurrent] = useState(origin + path);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  useEffect(() => { const subscription = BackHandler.addEventListener('hardwareBackPress', () => { if (back) ref.current?.goBack(); else onClose(); return true; }); return () => subscription.remove(); }, [back, onClose]);
  async function external(url: string) {
    try { if (/^https?:/.test(url)) await Browser.openBrowserAsync(url); else await Linking.openURL(url); }
    catch { Alert.alert('Unable to open link', 'The required app may not be installed. You can continue in GiftWise.'); }
  }
  function navigate(url: string, topFrame = true) {
    // Embedded provider frames stay in their own WebView context, not the native bridge.
    if (isAdminPath(new URL(url, origin).pathname)) return false;
    if (!topFrame) return true;
    const target = navigationTarget(url, origin, checkoutOrigins);
    if (target === 'internal' || target === 'checkout' || url === 'about:blank') return true;
    if (target === 'external') void external(url);
    else Alert.alert('Link unavailable', 'This link type is not supported inside GiftWise.');
    return false;
  }
  async function message(event: WebViewMessageEvent) {
    try {
      const data = parseNativeMessage(event.nativeEvent.data, event.nativeEvent.url, origin);
      if (data.type === 'clipboard') { await Clipboard.setStringAsync(data.text); return; }
      if (data.type === 'share') { await Share.share({ message: [data.text, data.url].filter(Boolean).join('\n') }); return; }
      if (!await Sharing.isAvailableAsync()) throw Error('File sharing is not available on this device.');
      const file = new File(Paths.cache, `${Date.now()}-${data.name}`);
      try {
        await file.write(data.base64, { encoding: 'base64' });
        await Sharing.shareAsync(file.uri, { mimeType: data.mime, dialogTitle: 'Save or share your GiftWise file' });
      } finally { if (file.exists) file.delete(); }
    } catch (err) { Alert.alert('Action unavailable', err instanceof Error ? err.message : 'Please try again or use the browser version.'); }
  }
  return <View style={s.root}><View style={s.toolbar}>
    <Pressable style={s.tool} accessibilityRole="button" accessibilityLabel={back ? 'Go back' : 'Return to GiftWise home'} onPress={() => back ? ref.current?.goBack() : onClose()}><Text style={s.toolText}>←</Text></Pressable>
    <View style={s.title}><Text style={s.brand}>GiftWise</Text><Text numberOfLines={1} style={s.caption}>Your celebration workspace</Text></View>
    <Pressable style={s.tool} accessibilityRole="button" accessibilityLabel="Reload page" onPress={() => { setError(''); ref.current?.reload(); }}><Text style={s.toolText}>↻</Text></Pressable>
    <Pressable style={s.tool} accessibilityRole="button" accessibilityLabel="Return to app home" onPress={onClose}><Text style={s.toolText}>⌂</Text></Pressable>
  </View>{loading && <View accessibilityRole="progressbar" accessibilityLabel="Loading workspace" style={s.loading}><ActivityIndicator color={palette.purple}/></View>}
  <WebView<{}> ref={ref} source={{ uri: origin + path }} style={s.web} applicationNameForUserAgent={NATIVE_USER_AGENT} originWhitelist={['*']} javaScriptEnabled webviewDebuggingEnabled={process.env.EXPO_PUBLIC_GIFTWISE_LOCAL_PREVIEW === '1'} domStorageEnabled sharedCookiesEnabled thirdPartyCookiesEnabled
    injectedJavaScript={nativeBridge(origin)} onMessage={event => { void message(event); }}
    onShouldStartLoadWithRequest={request => navigate(request.url, request.isTopFrame !== false)}
    onOpenWindow={({ nativeEvent }) => { if (navigationTarget(nativeEvent.targetUrl, origin, checkoutOrigins) === 'internal') { ref.current?.injectJavaScript(`window.location.assign(${JSON.stringify(nativeEvent.targetUrl)});true;`); } else if (navigationTarget(nativeEvent.targetUrl, origin, checkoutOrigins) === 'checkout') { ref.current?.injectJavaScript(`window.location.assign(${JSON.stringify(nativeEvent.targetUrl)});true;`); } else if (navigationTarget(nativeEvent.targetUrl, origin, checkoutOrigins) === 'external') void external(nativeEvent.targetUrl); }}
    onNavigationStateChange={state => { setBack(state.canGoBack); setCurrent(state.url); }}
    onLoadStart={() => setLoading(true)} onLoadEnd={() => setLoading(false)}
    onError={() => { setLoading(false); setError('We couldn’t open your workspace. Check your connection and the web server, then retry.'); }}
    onHttpError={({ nativeEvent }) => { if (nativeEvent.url === current && nativeEvent.statusCode >= 500) setError('The GiftWise server is temporarily unavailable. Please retry.'); }}
    onContentProcessDidTerminate={() => ref.current?.reload()} allowsBackForwardNavigationGestures allowsInlineMediaPlayback mixedContentMode="never" allowFileAccess={false}
  />
  {!!error && <View accessibilityLiveRegion="polite" style={s.error}><Text style={s.errorText}>{error}</Text><Pressable accessibilityRole="button" onPress={() => { setError(''); ref.current?.reload(); }} style={s.retry}><Text style={s.retryText}>Try again</Text></Pressable></View>}</View>;
}
const s = StyleSheet.create({ root: { flex: 1, backgroundColor: '#FCFAF6' }, web: { flex: 1, backgroundColor: '#FCFAF6' }, toolbar: { flexDirection: 'row', alignItems: 'center', padding: 8, gap: 4, borderBottomWidth: 1, borderColor: '#DED7E8', backgroundColor: '#FFFFFF' }, tool: { minWidth: 44, minHeight: 48, justifyContent: 'center', alignItems: 'center', borderRadius: 14, backgroundColor: '#F6F0FD' }, toolText: { fontSize: 23, color: '#6336B5' }, title: { flex: 1, paddingHorizontal: 8 }, brand: { fontSize: 18, fontWeight: '700', color: '#35205B' }, caption: { fontSize: 11, color: '#6F657B' }, loading: { height: 28, justifyContent: 'center' }, error: { position: 'absolute', bottom: 24, left: 16, right: 16, padding: 20, borderRadius: 20, backgroundColor: '#FFF0F2', borderWidth: 1, borderColor: '#AC324A', gap: 12 }, errorText: { color: '#84273B', lineHeight: 22 }, retry: { backgroundColor: '#6336B5', padding: 14, borderRadius: 12, alignItems: 'center' }, retryText: { color: '#FFFFFF', fontWeight: '600' } });
