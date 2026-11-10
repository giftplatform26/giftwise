package com.giftplatform.asset;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import static org.junit.jupiter.api.Assertions.*;

class GalleryPreviewRendererTest {
 @Test void previewWatermarkChangesPixelsAcrossEntireArtworkWithoutChangingOriginal() throws Exception {
  byte[] clean=db.migration.V98__premium_invitation_collection.artwork(0);byte[] original=clean.clone();
  var source=ImageIO.read(new ByteArrayInputStream(clean));
  var preview=ImageIO.read(new ByteArrayInputStream(GalleryPreviewRenderer.watermarked(clean)));
  assertEquals(600,preview.getWidth());assertEquals(800,preview.getHeight());assertArrayEquals(original,clean);
  for(int band=0;band<8;band++){
   int changed=0;
   for(int y=band*100;y<(band+1)*100;y++)for(int x=0;x<600;x++)if(source.getRGB(x,y)!=preview.getRGB(x,y))changed++;
   assertTrue(changed>100,"Watermark must cover image band "+band);
  }
 }
 @Test void allPremiumArtworkIsValidPng() throws Exception {
  for(int i=0;i<6;i++){var image=ImageIO.read(new ByteArrayInputStream(db.migration.V98__premium_invitation_collection.artwork(i)));assertEquals(600,image.getWidth());assertEquals(800,image.getHeight());}
 }
}
