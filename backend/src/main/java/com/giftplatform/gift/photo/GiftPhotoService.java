package com.giftplatform.gift.photo;

import com.giftplatform.asset.GalleryImageProcessor;
import com.giftplatform.security.ActorContext;
import com.giftplatform.gift.retailer.RetailerPageClient;
import java.util.UUID;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GiftPhotoService {
    private final JdbcTemplate db;
    private final ActorContext actors;
    private final GalleryImageProcessor processor;
    private final RetailerPageClient retailer;
    public GiftPhotoService(JdbcTemplate db,ActorContext actors,GalleryImageProcessor processor,RetailerPageClient retailer){
        this.db=db;this.actors=actors;this.processor=processor;this.retailer=retailer;
    }
    public record Photo(String imageKey,int width,int height){}
    public Photo upload(UUID eventId,byte[] input){
        UUID actor=actors.requireAuthenticatedUserId();
        requireOwner(eventId,actor);
        return store(eventId,actor,input);
    }
    public Photo fromRetailer(UUID eventId,UUID actor,String url) throws IOException {
        requireOwner(eventId,actor);
        return store(eventId,actor,retailer.fetchImage(url));
    }
    private void requireOwner(UUID eventId,UUID actor){
        Integer n=db.queryForObject("SELECT count(*) FROM events WHERE id=? AND owner_id=? AND status IN ('DRAFT','PUBLISHED')",Integer.class,eventId,actor);
        if(n==null||n!=1)throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Editable event not found");
    }
    public Photo store(UUID eventId,UUID actor,byte[] input){
        var processed=processor.process(input,"GIFT");
        if(processed.width()<160||processed.height()<160)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Choose a clear photo at least 160 × 160 pixels");
        try {
            BufferedImage source=ImageIO.read(new ByteArrayInputStream(processed.png()));
            double scale=Math.min(1,1200.0/Math.max(source.getWidth(),source.getHeight()));
            int w=(int)Math.round(source.getWidth()*scale),h=(int)Math.round(source.getHeight()*scale);
            if(w<160||h<160)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Photo is too narrow. Use a less panoramic image.");
            BufferedImage result=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
            var graphics=result.createGraphics();
            try{graphics.setColor(java.awt.Color.WHITE);graphics.fillRect(0,0,w,h);graphics.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);graphics.drawImage(source,0,0,w,h,null);}finally{graphics.dispose();}
            ByteArrayOutputStream output=new ByteArrayOutputStream();ImageIO.write(result,"jpeg",output);
            UUID id=UUID.randomUUID();
            db.update("INSERT INTO gift_photos(id,event_id,actor_id,content,width,height) VALUES (?,?,?,?,?,?)",id,eventId,actor,output.toByteArray(),w,h);
            return new Photo("gift-photo:"+id,w,h);
        }catch(IOException e){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Photo could not be read. Choose a JPEG or PNG.");}
    }
    public void validateReference(UUID eventId,String key){
        if(key==null||!key.startsWith("gift-photo:"))return; // Keep existing gallery/storage keys compatible.
        UUID id;
        try{id=UUID.fromString(key.substring(11));}catch(Exception e){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid gift photo");}
        Integer n=db.queryForObject("SELECT count(*) FROM gift_photos WHERE id=? AND event_id=?",Integer.class,id,eventId);
        if(n==null||n!=1)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Photo does not belong to this event");
    }
    public byte[] owned(UUID eventId,UUID id){
        Integer n=db.queryForObject("SELECT count(*) FROM events WHERE id=? AND owner_id=?",Integer.class,eventId,actors.requireAuthenticatedUserId());
        if(n==null||n!=1)throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return db.query("SELECT content FROM gift_photos WHERE id=? AND event_id=?",(rs,i)->rs.getBytes(1),id,eventId).stream().findFirst().orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
    public byte[] publicPhoto(UUID token,UUID giftId){
        return db.query("""
            SELECT p.content FROM gift_photos p JOIN gifts g ON g.image_key='gift-photo:'||p.id::text AND g.event_id=p.event_id
            JOIN events e ON e.id=g.event_id WHERE e.public_token=? AND e.status='PUBLISHED'
            AND g.id=? AND g.visibility='PUBLIC' AND g.status='ACTIVE'
            """,(rs,i)->rs.getBytes(1),token,giftId).stream().findFirst().orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
