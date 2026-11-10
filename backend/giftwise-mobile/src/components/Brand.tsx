import React from 'react';
import { View, Text, Image, StyleSheet } from 'react-native';
import { palette } from '../theme/tokens';
export function BrandMark({ size = 44, imageUri, name = "GiftWise" }: { size?: number; imageUri?: string; name?: string }) {
  const [failed,setFailed]=React.useState(false);React.useEffect(()=>setFailed(false),[imageUri]);
  if(imageUri&&!failed)return <Image source={{uri:imageUri}} accessibilityLabel={name+" logo"} style={{width:size,height:size,resizeMode:"contain"}} onError={()=>setFailed(true)}/>;
  return <View accessible accessibilityLabel="GiftWise ribbon mark" style={{ width: size, height: size, borderRadius: size * .3, backgroundColor: palette.purple, alignItems: 'center', justifyContent: 'center' }}>
    <View style={{ position: 'absolute', width: size * .62, height: size * .1, backgroundColor: palette.gold, transform: [{ rotate: '-34deg' }] }} />
    <View style={{ position: 'absolute', width: size * .62, height: size * .1, backgroundColor: '#FFF7E4', transform: [{ rotate: '34deg' }] }} />
    <View style={{ width: size * .18, height: size * .18, borderRadius: size, backgroundColor: palette.gold }} />
  </View>;
}
export function Miri({ size = 140 }: { size?: number }) {
  return <View accessible accessibilityLabel="Miri, GiftWise’s warm little ribbon companion" style={{ width: size, height: size * 1.1, alignItems: 'center', justifyContent: 'flex-end' }}>
    <View style={{ position: 'absolute', top: 0, left: size * .22, width: size * .3, height: size * .42, borderWidth: size * .09, borderColor: palette.gold, borderRadius: size, transform: [{ rotate: '-30deg' }] }} />
    <View style={{ position: 'absolute', top: 0, right: size * .16, width: size * .3, height: size * .42, borderWidth: size * .09, borderColor: palette.gold, borderRadius: size, transform: [{ rotate: '30deg' }] }} />
    <View style={{ width: size * .85, height: size * .78, borderRadius: size * .3, backgroundColor: palette.purple, alignItems: 'center', paddingTop: size * .23 }}>
      <View style={{ flexDirection: 'row', gap: size * .2 }}><View style={styles.eye}/><View style={styles.eye}/></View>
      <Text allowFontScaling={false} style={{ color: '#FFFFFF', fontSize: size * .25, lineHeight: size * .27 }}>⌣</Text>
      <View style={{ position: 'absolute', bottom: 0, backgroundColor: palette.gold, height: size * .12, width: size * .2, borderTopLeftRadius: 6, borderTopRightRadius: 6 }} />
    </View>
  </View>;
}
const styles = StyleSheet.create({ eye: { width: 6, height: 9, borderRadius: 4, backgroundColor: '#FFF7E4' } });
