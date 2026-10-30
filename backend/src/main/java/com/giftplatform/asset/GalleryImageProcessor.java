package com.giftplatform.asset;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;

@Component
public class GalleryImageProcessor {
 public record Processed(byte[] png,int width,int height) {}
 public Processed process(byte[] input,String category){
  if(input==null||input.length==0||input.length>2_000_000) throw invalid();
  boolean png=input.length>8&&(input[0]&255)==137&&input[1]==80&&input[2]==78&&input[3]==71;
  boolean jpg=input.length>3&&(input[0]&255)==255&&(input[1]&255)==216&&(input[2]&255)==255;
  boolean webp=input.length>12&&new String(input,0,4,java.nio.charset.StandardCharsets.US_ASCII).equals("RIFF")
    &&new String(input,8,4,java.nio.charset.StandardCharsets.US_ASCII).equals("WEBP");
  if(!png&&!jpg&&!webp) throw invalid();
  String format=png?"png":jpg?"jpeg":"webp";
  try {
   Iterator<ImageReader> readers=ImageIO.getImageReadersByFormatName(format);
   if(!readers.hasNext()) throw invalid(); // WebP requires an installed ImageIO reader
   ImageReader reader=readers.next();
   try(ImageInputStream stream=ImageIO.createImageInputStream(new ByteArrayInputStream(input))){
    reader.setInput(stream,true,true);
    int w=reader.getWidth(0),h=reader.getHeight(0);
    if(w<32||h<32||w>4096||h>4096||(long)w*h>12_000_000L) throw invalid();
    BufferedImage src=reader.read(0);
    if(src==null) throw invalid();
    BufferedImage output=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
    var graphics=output.createGraphics();
    try {graphics.drawImage(src,0,0,null);}finally{graphics.dispose();}
    ByteArrayOutputStream bytes=new ByteArrayOutputStream();
    if(!ImageIO.write(output,"png",bytes)||bytes.size()>8_000_000) throw invalid();
    return new Processed(bytes.toByteArray(),w,h);
   } finally {reader.dispose();}
  }catch(IOException|RuntimeException e){if(e instanceof ResponseStatusException r) throw r;throw invalid();}
 }
 private ResponseStatusException invalid(){return new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid image (PNG/JPEG or supported WebP, max 2 MB, 4096px per side)");}
}
