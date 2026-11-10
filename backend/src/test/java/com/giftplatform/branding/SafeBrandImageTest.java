package com.giftplatform.branding;
import org.junit.jupiter.api.Test;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import static org.assertj.core.api.Assertions.*;
class SafeBrandImageTest {
 @Test void rejectsActiveAndOversizedContent(){assertThatThrownBy(()->SafeBrandImage.png("<svg onload='alert(1)'/>".getBytes())).isInstanceOf(IllegalArgumentException.class);assertThatThrownBy(()->SafeBrandImage.png(new byte[2*1024*1024+1])).hasMessageContaining("2 MB");}
 @Test void decodesAndBoundsValidImages()throws Exception{var source=new BufferedImage(2048,512,BufferedImage.TYPE_INT_RGB);var out=new ByteArrayOutputStream();ImageIO.write(source,"png",out);var result=ImageIO.read(new ByteArrayInputStream(SafeBrandImage.png(out.toByteArray())));assertThat(result.getWidth()).isEqualTo(1024);assertThat(result.getHeight()).isEqualTo(256);}
}
