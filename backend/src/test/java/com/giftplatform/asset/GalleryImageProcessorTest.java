package com.giftplatform.asset;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import static org.junit.jupiter.api.Assertions.*;
class GalleryImageProcessorTest {
 private final GalleryImageProcessor processor=new GalleryImageProcessor();
 @Test void rejectsInvalidBytes(){assertThrows(ResponseStatusException.class,()->processor.process(new byte[]{1,2,3},"BACKGROUND"));}
 @Test void rejectsOversize(){assertThrows(ResponseStatusException.class,()->processor.process(new byte[2_000_001],"DECORATION"));}
 @Test void preservesDimensionsAndTransparency() throws Exception {
  BufferedImage img=new BufferedImage(40,60,BufferedImage.TYPE_INT_ARGB);
  ByteArrayOutputStream b=new ByteArrayOutputStream();ImageIO.write(img,"png",b);
  var result=processor.process(b.toByteArray(),"DECORATION");
  assertEquals(40,result.width());assertEquals(60,result.height());
  var restored=ImageIO.read(new java.io.ByteArrayInputStream(result.png()));
  assertEquals(0,(restored.getRGB(0,0)>>>24)&255);
 }
}
