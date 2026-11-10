package com.giftplatform.asset;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

public final class GalleryPreviewRenderer {
 private GalleryPreviewRenderer() {}
 public static byte[] watermarked(byte[] png) {
  try {
   var source=ImageIO.read(new ByteArrayInputStream(png));
   if(source==null)throw new IOException("Invalid gallery image");
   int w=Math.min(600,source.getWidth()),h=Math.max(1,(int)((long)source.getHeight()*w/source.getWidth()));
   var image=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);var g=image.createGraphics();
   try {
    g.setColor(Color.WHITE);g.fillRect(0,0,w,h);g.drawImage(source,0,0,w,h,null);
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g.setFont(new Font(Font.SANS_SERIF,Font.BOLD,Math.max(18,w/16)));
    // Repeat over the entire image. The watermark is part of the returned pixels.
    g.setColor(new Color(90,55,76,85));
    for(int y=-100;y<h+180;y+=110)for(int x=-100;x<w+180;x+=180){var transform=g.getTransform();g.rotate(-Math.PI/7,x,y);g.drawString("GiftWise",x,y);g.setTransform(transform);}
   } finally {g.dispose();}
   var out=new ByteArrayOutputStream();ImageIO.write(image,"png",out);return out.toByteArray();
  } catch(IOException e){throw new IllegalStateException("Could not render template preview",e);}
 }
}
