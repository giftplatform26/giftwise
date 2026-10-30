package com.giftplatform.qr.design;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class InvitationImageProcessor {
 public static final int MAX_UPLOAD_BYTES=2_000_000;
 public byte[] sanitize(byte[] uploaded){
  if(uploaded==null || uploaded.length==0) return null;
  if(uploaded.length>MAX_UPLOAD_BYTES) throw invalid();
  boolean png=uploaded.length>8 && (uploaded[0]&255)==137 && uploaded[1]==80 && uploaded[2]==78 && uploaded[3]==71;
  boolean jpeg=uploaded.length>3 && (uploaded[0]&255)==255 && (uploaded[1]&255)==216 && (uploaded[2]&255)==255;
  if(!png&&!jpeg) throw invalid();
  try {
   var reader=ImageIO.getImageReadersByFormatName(png?"png":"jpeg");
   if(!reader.hasNext()) throw invalid();
   var r=reader.next();
   try(var stream=ImageIO.createImageInputStream(new ByteArrayInputStream(uploaded))){
    r.setInput(stream,true,true);
    int w=r.getWidth(0),h=r.getHeight(0);
    if(w<32||h<32||w>4096||h>4096||((long)w*h)>12_000_000L) throw invalid();
    BufferedImage src=r.read(0);
    BufferedImage dest=new BufferedImage(900,1200,BufferedImage.TYPE_INT_RGB);
    Graphics2D g=dest.createGraphics();
    try{
     g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
     double scale=Math.max(900.0/w,1200.0/h);
     int dw=(int)Math.ceil(w*scale),dh=(int)Math.ceil(h*scale);
     g.drawImage(src,(900-dw)/2,(1200-dh)/2,dw,dh,null);
    }finally{g.dispose();}
    ByteArrayOutputStream out=new ByteArrayOutputStream();ImageIO.write(dest,"png",out);
    return out.toByteArray();
   }finally{r.dispose();}
  }catch(IOException|RuntimeException ex){
   if(ex instanceof ResponseStatusException e) throw e;
   throw invalid();
  }
 }
 private ResponseStatusException invalid(){return new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid PNG/JPEG image (maximum 2 MB, 4096px per side)");}
}
