package com.giftplatform.qr.design;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InvitationImageProcessorTest {
 private final InvitationImageProcessor p=new InvitationImageProcessor();
 @Test void missingImageIsAllowed(){assertNull(p.sanitize(null));}
 @Test void rejectsOversize(){assertThrows(RuntimeException.class,()->p.sanitize(new byte[2_000_001]));}
 @Test void rejectsSvg(){assertThrows(RuntimeException.class,()->p.sanitize("<svg/>".getBytes()));}
 @Test void rejectsRandomBytes(){assertThrows(RuntimeException.class,()->p.sanitize(new byte[]{1,2,3,4}));}
 @Test void normalizesPngToFixedSize() throws Exception {
  BufferedImage b=new BufferedImage(64,64,BufferedImage.TYPE_INT_RGB);ByteArrayOutputStream out=new ByteArrayOutputStream();ImageIO.write(b,"png",out);
  BufferedImage decoded=ImageIO.read(new java.io.ByteArrayInputStream(p.sanitize(out.toByteArray())));
  assertEquals(900,decoded.getWidth());assertEquals(1200,decoded.getHeight());
 }
}
