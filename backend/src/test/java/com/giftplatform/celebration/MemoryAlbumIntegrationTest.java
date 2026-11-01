package com.giftplatform.celebration;

import com.giftplatform.admin.*;
import com.giftplatform.event.entity.Event;
import com.giftplatform.event.repository.EventRepository;
import com.giftplatform.security.ActorContext;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.junit.jupiter.*;
import org.testcontainers.postgresql.PostgreSQLContainer;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Testcontainers
class MemoryAlbumIntegrationTest {
 @Container static PostgreSQLContainer postgres=new PostgreSQLContainer("postgres:17");
 static JdbcTemplate db;
 @TempDir Path storage;
 UUID host,event,admin;EventRepository events;ActorContext actors,admins;EffectiveModulePermissionService permissions;MemoryAlbumController album;PlatformAdminConsoleController controls;
 @BeforeAll static void migrate(){var ds=new DriverManagerDataSource(postgres.getJdbcUrl(),postgres.getUsername(),postgres.getPassword());Flyway.configure().dataSource(ds).load().migrate();db=new JdbcTemplate(ds);}
 @BeforeEach void fixture(){
  host=UUID.randomUUID();event=UUID.randomUUID();admin=UUID.randomUUID();
  db.update("INSERT INTO users(id,email,password_hash,role) VALUES(?,?,'hash','CUSTOMER'),(?,?,'hash','PLATFORM_ADMIN')",host,host+"@example.test",admin,admin+"@example.test");
  db.update("INSERT INTO events(id,title,occasion_type,owner_id,host_display_name) VALUES(?,'A day to remember','BIRTHDAY',?,'Our family')",event,host);
  events=mock(EventRepository.class);actors=mock(ActorContext.class);admins=mock(ActorContext.class);
  when(actors.requireAuthenticatedUserId()).thenReturn(host);when(admins.isPlatformAdmin()).thenReturn(true);when(admins.requireAuthenticatedUserId()).thenReturn(admin);
  var e=mock(Event.class);when(e.getOwnerId()).thenReturn(host);when(e.getTitle()).thenReturn("A day to remember");when(e.getEventDate()).thenReturn(LocalDate.of(2026,11,1));when(events.findById(event)).thenReturn(Optional.of(e));
  permissions=new EffectiveModulePermissionService(db);album=new MemoryAlbumController(db,events,actors,permissions);controls=new PlatformAdminConsoleController(db,admins);
  controls.platformModule("MEMORY_ALBUM",new PlatformAdminConsoleController.Toggle(true));controls.hostModule(host,"MEMORY_ALBUM",new PlatformAdminConsoleController.Toggle(true));controls.albumSettings(event,new PlatformAdminConsoleController.Toggle(true));
 }
 UUID memory(String status,boolean photo,int order){UUID id=UUID.randomUUID();db.update("INSERT INTO event_guestbook(id,event_id,guest_name,message,status,photo_path,created_at) VALUES(?,?,'Sam','May this joy stay with you always',?,?,TIMESTAMPTZ '2026-11-01 12:00:00+00' + (? * INTERVAL '1 second'))",id,event,status,photo?id+".jpg":null,order);return id;}
 @Test void albumIncludesPhotosAndTextWishesWithStablePagination(){
  UUID first=memory("PENDING",false,1),second=memory("APPROVED",true,2);memory("HIDDEN",true,3);
  var response=album.album(event,1,0,false);var page=response.getBody();assertNotNull(page);assertTrue(response.getHeaders().getCacheControl().contains("no-store"));
  assertEquals("A day to remember",page.title());assertEquals(2,page.totalCount());assertEquals(1,page.photoCount());assertEquals(first,page.memories().getFirst().id());assertTrue(page.hasMore());
  var last=album.album(event,1,page.nextOffset(),false).getBody();assertEquals(second,last.memories().getFirst().id());assertFalse(last.hasMore());
  assertEquals(3,album.album(event,60,0,true).getBody().totalCount());
 }
 @Test void platformHostAndEventMustAllBeEnabled(){
  for(boolean platform:new boolean[]{false,true})for(boolean grant:new boolean[]{false,true})for(boolean enabled:new boolean[]{false,true}){
   controls.platformModule("MEMORY_ALBUM",new PlatformAdminConsoleController.Toggle(platform));controls.hostModule(host,"MEMORY_ALBUM",new PlatformAdminConsoleController.Toggle(grant));controls.albumSettings(event,new PlatformAdminConsoleController.Toggle(enabled));
   if(platform&&grant&&enabled)assertDoesNotThrow(()->album.album(event,60,0,false));else assertEquals(404,assertThrows(ResponseStatusException.class,()->album.album(event,60,0,false)).getStatusCode().value());
  }
 }
 @Test void anotherHostCannotReadAlbumOrPhotos() throws Exception {
  UUID wish=memory("APPROVED",true,1);when(actors.requireAuthenticatedUserId()).thenReturn(UUID.randomUUID());
  assertThrows(ResponseStatusException.class,()->album.album(event,60,0,false));
  var photos=new GuestbookPhotoController(db,events,actors,permissions,storage.toString());assertThrows(ResponseStatusException.class,()->photos.albumPhoto(event,wish));
 }
 @Test void privateAlbumPreservesMemoriesAfterGuestbookCollectionIsDisabled() throws Exception {
  UUID wish=memory("PENDING",true,1);byte[] image={1,2,3};Files.write(storage.resolve(wish+".jpg"),image);
  db.update("UPDATE platform_module_controls SET enabled=FALSE WHERE module_code='GUESTBOOK'");
  var photos=new GuestbookPhotoController(db,events,actors,permissions,storage.toString());assertArrayEquals(image,photos.albumPhoto(event,wish).getBody());
  assertEquals(1,album.album(event,60,0,false).getBody().totalCount());
  controls.albumSettings(event,new PlatformAdminConsoleController.Toggle(false));assertThrows(ResponseStatusException.class,()->photos.albumPhoto(event,wish));
  assertEquals(1,db.queryForObject("SELECT count(*) FROM event_guestbook WHERE event_id=?",Integer.class,event));
 }
 @Test void hostCannotChangeAlbumAccessAndAdminChangesAreAudited(){
  when(actors.isPlatformAdmin()).thenReturn(false);var hostControls=new PlatformAdminConsoleController(db,actors);
  assertEquals(403,assertThrows(ResponseStatusException.class,()->hostControls.albumSettings(event,new PlatformAdminConsoleController.Toggle(false))).getStatusCode().value());
  controls.albumSettings(event,new PlatformAdminConsoleController.Toggle(false));
  assertEquals(2,db.queryForObject("SELECT count(*) FROM platform_admin_action_audit WHERE target_id=? AND action='EVENT_ALBUM_CHANGED'",Integer.class,event));
  assertFalse(permissions.eventModules(host,event).get("MEMORY_ALBUM"));
 }
 @Test void photosAndAlbumGrantsCannotBeUsedForAnotherEvent() throws Exception {
  UUID other=UUID.randomUUID();db.update("INSERT INTO events(id,title,occasion_type,owner_id,host_display_name) VALUES(?,'Another day','BIRTHDAY',?,'Our family')",other,host);
  UUID photo=UUID.randomUUID();db.update("INSERT INTO event_guestbook(id,event_id,guest_name,message,status,photo_path) VALUES(?,?,'Jo','Love always','APPROVED',?)",photo,other,photo+".jpg");Files.write(storage.resolve(photo+".jpg"),new byte[]{4});
  assertFalse(permissions.albumEnabled(host,other));
  assertThrows(ResponseStatusException.class,()->new GuestbookPhotoController(db,events,actors,permissions,storage.toString()).albumPhoto(event,photo));
 }
}
