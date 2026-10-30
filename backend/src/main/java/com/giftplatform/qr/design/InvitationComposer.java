package com.giftplatform.qr.design;

import com.giftplatform.qr.QrRenderer;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Component;

@Component
public class InvitationComposer {
 private final QrRenderer qr;
 public InvitationComposer(QrRenderer qr){this.qr=qr;}
 public byte[] render(String url,String title,String greeting,InvitationStyle style,byte[] imagePng){
  try{
   BufferedImage out=new BufferedImage(900,1200,BufferedImage.TYPE_INT_RGB);
   Graphics2D g=out.createGraphics();
   try{
    Color base=switch(style){case FLORAL->new Color(249,239,233);case CELEBRATION->new Color(242,232,255);case PHOTO_FRAME->new Color(235,231,225);case MINIMAL->Color.WHITE;};
    g.setColor(base);g.fillRect(0,0,900,1200);
    if(imagePng!=null){BufferedImage bg=ImageIO.read(new ByteArrayInputStream(imagePng));g.drawImage(bg,0,0,900,1200,null);}
    g.setColor(new Color(255,255,255,245));g.fillRoundRect(80,95,740,1010,48,48);
    g.setColor(new Color(40,40,40));g.setFont(new Font("SansSerif",Font.BOLD,36));
    drawCentered(g,trim(title,34),450,205);
    if(greeting!=null&&!greeting.isBlank()){g.setFont(new Font("SansSerif",Font.PLAIN,24));drawCentered(g,trim(greeting,48),450,262);}
    // The complete quiet zone stays inside an opaque white panel.
    g.setColor(Color.WHITE);g.fillRect(130,340,640,640);
    BufferedImage code=ImageIO.read(new ByteArrayInputStream(qr.png(url)));
    g.drawImage(code,154,364,592,592,null);
    g.setColor(new Color(50,50,50));g.setFont(new Font("SansSerif",Font.PLAIN,22));
    drawCentered(g,"Scan to celebrate & explore gifts",450,1040);
   }finally{g.dispose();}
   // Decode the final composition, not merely the underlying QR bytes.
   var decoded=new MultiFormatReader().decode(new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(out))));
   if(!url.equals(decoded.getText()))throw new IllegalStateException("Composed invitation failed QR validation");
   ByteArrayOutputStream bytes=new ByteArrayOutputStream();ImageIO.write(out,"png",bytes);return bytes.toByteArray();
  }catch(Exception ex){throw new IllegalStateException("Could not produce scannable invitation",ex);}
 }
 private static String trim(String text,int limit){return text==null?"":text.length()>limit?text.substring(0,limit-1)+"…":text;}
 private static void drawCentered(Graphics2D g,String s,int x,int y){var fm=g.getFontMetrics();g.drawString(s,x-fm.stringWidth(s)/2,y);}
}
