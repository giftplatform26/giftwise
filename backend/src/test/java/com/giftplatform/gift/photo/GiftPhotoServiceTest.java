package com.giftplatform.gift.photo;

import com.giftplatform.asset.GalleryImageProcessor;
import com.giftplatform.gift.retailer.RetailerPageClient;
import com.giftplatform.security.ActorContext;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GiftPhotoServiceTest {
    final JdbcTemplate db=mock(JdbcTemplate.class);
    final ActorContext actors=mock(ActorContext.class);
    final GiftPhotoService service=new GiftPhotoService(db,actors,new GalleryImageProcessor(),mock(RetailerPageClient.class));
    final UUID event=UUID.randomUUID(),actor=UUID.randomUUID();
    static byte[] png(int width,int height) throws Exception {
        var output=new ByteArrayOutputStream();ImageIO.write(new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB),"png",output);return output.toByteArray();
    }
    void owner(){when(actors.requireAuthenticatedUserId()).thenReturn(actor);when(db.queryForObject(anyString(),eq(Integer.class),eq(event),eq(actor))).thenReturn(1);}
    @Test void photosAreDecodedResizedAndStoredAsManagedReferences() throws Exception {
        owner();var photo=service.upload(event,png(2000,1000));
        assertTrue(photo.imageKey().startsWith("gift-photo:"));assertEquals(1200,photo.width());assertEquals(600,photo.height());
        var bytes=org.mockito.ArgumentCaptor.forClass(byte[].class);
        verify(db).update(startsWith("INSERT INTO gift_photos"),any(UUID.class),eq(event),eq(actor),bytes.capture(),eq(1200),eq(600),any(byte[].class));
        assertEquals((byte)0xff,bytes.getValue()[0]);assertEquals((byte)0xd8,bytes.getValue()[1]);
    }
    @Test void tinyFakeAndOversizedImagesAreRejected() throws Exception {
        owner();assertThrows(ResponseStatusException.class,()->service.upload(event,png(32,32)));
        assertThrows(ResponseStatusException.class,()->service.upload(event,"<svg onload='bad()'/>".getBytes()));
        assertThrows(ResponseStatusException.class,()->service.upload(event,new byte[2_000_001]));
    }
    @Test void anotherHostsEventCannotReceiveAnUpload() throws Exception {
        when(actors.requireAuthenticatedUserId()).thenReturn(actor);
        assertThrows(ResponseStatusException.class,()->service.upload(event,png(300,300)));
    }
    @Test void foreignEventPhotoReferencesCannotBeSaved(){
        assertThrows(ResponseStatusException.class,()->service.validateReference(event,"gift-photo:"+UUID.randomUUID()));
        assertThrows(ResponseStatusException.class,()->service.validateReference(event,"gift-photo:bad"));
    }
}
