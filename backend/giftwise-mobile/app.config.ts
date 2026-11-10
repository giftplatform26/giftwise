import type { ConfigContext, ExpoConfig } from 'expo/config';
export default ({ config }: ConfigContext): ExpoConfig => {
  const release = process.env.GIFTWISE_RELEASE === '1';
  const webURL = process.env.EXPO_PUBLIC_GIFTWISE_WEB_URL;
  const iosID = process.env.GIFTWISE_IOS_BUNDLE_ID;
  const androidID = process.env.GIFTWISE_ANDROID_PACKAGE;
  const projectID = process.env.GIFTWISE_EAS_PROJECT_ID;
  if (release && process.env.EXPO_PUBLIC_GIFTWISE_LOCAL_PREVIEW === '1') throw Error('Local preview configuration cannot be used in a production release.');
  if (release) {
    if (!webURL) throw Error('Set EXPO_PUBLIC_GIFTWISE_WEB_URL before building an installable app.');
    const url = new URL(webURL);
    if (url.protocol !== 'https:' || url.username || url.password || url.pathname !== '/' || url.search || url.hash) throw Error('Release web URL must be an HTTPS origin without credentials or path.');
    if (!iosID || !androidID || !projectID) throw Error('Set owned iOS/Android identifiers and the EAS project ID before a release build.');
  }
  return {
    ...config,
    plugins: [...(config.plugins || []), ['expo-build-properties', { android: { usesCleartextTraffic: !release } }]],
    name: 'GiftWise', slug: 'giftwise-mobile', version: '0.3.0', scheme: 'giftwise',
    ios: { ...config.ios, bundleIdentifier: iosID || 'com.giftwise.preview', infoPlist: { ...config.ios?.infoPlist, NSPhotoLibraryUsageDescription: 'Choose photos and invitation images for your GiftWise celebrations.', NSCameraUsageDescription: 'Take a photo to add to your GiftWise celebration.', ...(release ? {} : { NSAppTransportSecurity: { NSAllowsLocalNetworking: true, NSAllowsArbitraryLoadsInWebContent: true } }) } },
    android: { ...config.android, versionCode: 2, package: androidID || 'com.giftwise.preview' },
    extra: { ...config.extra, ...(projectID ? { eas: { projectId: projectID } } : {}) },
  };
};
