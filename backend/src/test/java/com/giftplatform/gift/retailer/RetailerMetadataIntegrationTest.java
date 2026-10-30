package com.giftplatform.gift.retailer;

import com.giftplatform.security.ActorContext;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Testcontainers
class RetailerMetadataIntegrationTest {
    @Container static PostgreSQLContainer postgres=new PostgreSQLContainer("postgres:17-alpine");
    static JdbcTemplate db;
    UUID actor,event,gift;RetailerMetadataService service;RetailerPageClient client;
    @BeforeAll static void database(){var source=new DriverManagerDataSource(postgres.getJdbcUrl(),postgres.getUsername(),postgres.getPassword());Flyway.configure().dataSource(source).load().migrate();db=new JdbcTemplate(source);}
    @BeforeEach void setup() throws Exception {
        actor=UUID.randomUUID();event=UUID.randomUUID();gift=UUID.randomUUID();
        db.update("INSERT INTO users(id,email,password_hash,username) VALUES (?,?,?,?)",actor,actor+"@example.test","hash","u_"+actor.toString().replace("-","").substring(0,20));
        db.update("INSERT INTO events(id,title,occasion_type,status,owner_id,host_display_name) VALUES (?,?,?,?,?,?)",event,"Test","BIRTHDAY","DRAFT",actor,"Host");
        db.update("INSERT INTO gifts(id,event_id,name,target_amount) VALUES (?,?,?,?)",gift,event,"Camera",1000);
        ActorContext actors=mock(ActorContext.class);when(actors.requireAuthenticatedUserId()).thenReturn(actor);
        client=mock(RetailerPageClient.class);when(client.fetch(anyString())).thenReturn("<meta property='og:title' content='Camera'><meta property='product:price:currency' content='INR'><meta property='product:price:amount' content='999.95'>");
        service=new RetailerMetadataService(db,actors,client,new ObjectMapper());
    }
    @Test void fetchedJsonIsAssociatedWithGiftWhenSavedAndCannotBeReusedElsewhere(){
        String url="https://retailer.com/camera";var preview=service.lookup(event,url);
        assertNull(db.queryForObject("SELECT gift_id FROM gift_retailer_metadata_audit WHERE id=?",UUID.class,preview.lookupId()));
        service.attach(preview.lookupId(),event,gift,actor,url);
        assertEquals(gift,db.queryForObject("SELECT gift_id FROM gift_retailer_metadata_audit WHERE id=?",UUID.class,preview.lookupId()));
        assertEquals("999.95",db.queryForObject("SELECT metadata->'metaTags'->>'product:price:amount' FROM gift_retailer_metadata_audit WHERE id=?",String.class,preview.lookupId()));
        assertNotNull(db.queryForObject("SELECT saved_at FROM gift_retailer_metadata_audit WHERE id=?",java.time.OffsetDateTime.class,preview.lookupId()));
        assertThrows(ResponseStatusException.class,()->service.attach(preview.lookupId(),event,UUID.randomUUID(),actor,url));
    }
    @Test void actorEventSourceAndExpiryAreChecked(){
        String url="https://retailer.com/camera";var preview=service.lookup(event,url);
        assertThrows(ResponseStatusException.class,()->service.attach(preview.lookupId(),event,gift,UUID.randomUUID(),url));
        assertThrows(ResponseStatusException.class,()->service.attach(preview.lookupId(),UUID.randomUUID(),gift,actor,url));
        assertThrows(ResponseStatusException.class,()->service.attach(preview.lookupId(),event,gift,actor,url+"/other"));
        db.update("UPDATE gift_retailer_metadata_audit SET fetched_at=CURRENT_TIMESTAMP-INTERVAL '2 days' WHERE id=?",preview.lookupId());
        assertThrows(ResponseStatusException.class,()->service.attach(preview.lookupId(),event,gift,actor,url));
    }
    @Test void photosRemainPrivateUntilTheirGiftAndEventArePublished() throws Exception {
        ActorContext actors=mock(ActorContext.class);when(actors.requireAuthenticatedUserId()).thenReturn(actor);
        var photos=new com.giftplatform.gift.photo.GiftPhotoService(db,actors,new com.giftplatform.asset.GalleryImageProcessor(),client);
        var source=new java.awt.image.BufferedImage(300,300,java.awt.image.BufferedImage.TYPE_INT_RGB);
        var output=new java.io.ByteArrayOutputStream();javax.imageio.ImageIO.write(source,"png",output);
        var photo=photos.upload(event,output.toByteArray());
        photos.validateReference(event,photo.imageKey());
        assertThrows(ResponseStatusException.class,()->photos.validateReference(UUID.randomUUID(),photo.imageKey()));
        byte[] content=photos.owned(event,UUID.fromString(photo.imageKey().substring(11)));
        assertTrue(content.length>0);
        UUID token=db.queryForObject("SELECT public_token FROM events WHERE id=?",UUID.class,event);
        db.update("UPDATE gifts SET image_key=? WHERE id=?",photo.imageKey(),gift);
        assertThrows(ResponseStatusException.class,()->photos.publicPhoto(token,gift));
        db.update("UPDATE events SET status='PUBLISHED' WHERE id=?",event);
        assertThrows(ResponseStatusException.class,()->photos.publicPhoto(token,gift));
        db.update("UPDATE gifts SET visibility='PUBLIC' WHERE id=?",gift);
        assertArrayEquals(content,photos.publicPhoto(token,gift));
        db.update("UPDATE gifts SET visibility='DRAFT' WHERE id=?",gift);
        assertThrows(ResponseStatusException.class,()->photos.publicPhoto(token,gift));
    }
    @Test void foreignEventDoesNotTriggerNetworkFetch(){
        assertThrows(ResponseStatusException.class,()->service.lookup(UUID.randomUUID(),"https://retailer.com/item"));verifyNoInteractions(client);
    }
}
