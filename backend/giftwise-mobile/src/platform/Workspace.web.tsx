import React, { useEffect } from 'react';
import { Linking, Pressable, Text, View } from 'react-native';
import type { WorkspaceProps } from './Workspace';
export default function Workspace({ origin, path }: WorkspaceProps) {
  const url = origin + path;
  useEffect(() => { window.location.assign(url); }, [url]);
  return <View style={{ padding: 32, gap: 20 }}><Text>Opening your full GiftWise web workspace…</Text><Pressable accessibilityRole="link" onPress={() => { void Linking.openURL(url); }}><Text>Continue to GiftWise →</Text></Pressable></View>;
}
