package com.giftplatform.qr.design;

import com.giftplatform.qr.QrRenderer;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InvitationComposerTest {
 private final InvitationComposer c=new InvitationComposer(new QrRenderer());
 @Test void everyStyleProducesScannablePng() throws Exception {
  String url="https://gifts.example.test/e/"+java.util.UUID.randomUUID();
  for(InvitationStyle style:InvitationStyle.values()){
   byte[] bytes=c.render(url,"Birthday Party","Join our celebration",style,null);
   var img=ImageIO.read(new ByteArrayInputStream(bytes));assertEquals(900,img.getWidth());assertEquals(1200,img.getHeight());
   var bitmap=new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(img)));
   assertEquals(url,new MultiFormatReader().decode(bitmap).getText());
  }
 }
 @Test void photoDoesNotChangeQrDestination() throws Exception {
  var photo=new java.awt.image.BufferedImage(900,1200,java.awt.image.BufferedImage.TYPE_INT_RGB);
  var b=new java.io.ByteArrayOutputStream();ImageIO.write(photo,"png",b);
  String url="https://gifts.example.test/e/"+java.util.UUID.randomUUID();
  var img=ImageIO.read(new ByteArrayInputStream(c.render(url,"Wedding",null,InvitationStyle.PHOTO_FRAME,b.toByteArray())));
  assertEquals(url,new MultiFormatReader().decode(new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(img)))).getText());
 }
}
