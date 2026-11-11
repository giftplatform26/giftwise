package com.giftplatform.branding;

import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

/** Decode, bound dimensions before allocation, and re-encode without metadata or active content. */
public final class SafeBrandImage {
 private SafeBrandImage(){}
 public static byte[] png(byte[] bytes) { return png(bytes,1024); }
 /** Same safety rules, scaled to fit within {@code max} pixels (e.g. small receipt logos). */
 public static byte[] png(byte[] bytes,int max) {
  if(bytes==null||bytes.length==0||bytes.length>2*1024*1024) throw new IllegalArgumentException("Choose a PNG or JPEG image up to 2 MB.");
  try(var stream=ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))){
   var readers=ImageIO.getImageReaders(stream);
   if(!readers.hasNext())throw new IllegalArgumentException("Choose a valid PNG or JPEG image.");
   var reader=readers.next();
   try{
    String format=reader.getFormatName();
    if(!format.equalsIgnoreCase("png")&&!format.equalsIgnoreCase("jpeg"))throw new IllegalArgumentException("Only PNG and JPEG images are supported.");
    reader.setInput(stream,true,true);int w=reader.getWidth(0),h=reader.getHeight(0);
    if(w<16||h<16||w>4096||h>4096||(long)w*h>8000000)throw new IllegalArgumentException("Image dimensions must be 16–4096 pixels, up to 8 megapixels.");
    BufferedImage source=reader.read(0);double scale=Math.min(1d,Math.min((double)max/w,(double)max/h));
    BufferedImage output=new BufferedImage(Math.max(1,(int)(w*scale)),Math.max(1,(int)(h*scale)),BufferedImage.TYPE_INT_ARGB);
    var g=output.createGraphics();try{g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);g.drawImage(source,0,0,output.getWidth(),output.getHeight(),null);}finally{g.dispose();}
    var out=new ByteArrayOutputStream();ImageIO.write(output,"png",out);return out.toByteArray();
   }finally{reader.dispose();}
  }catch(IOException ex){throw new IllegalArgumentException("Unable to read this image. Choose a valid PNG or JPEG.");}
 }
}
