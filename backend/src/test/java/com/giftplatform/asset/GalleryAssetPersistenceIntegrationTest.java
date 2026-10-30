package com.giftplatform.asset;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties="gift.otp.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
@Testcontainers
class GalleryAssetPersistenceIntegrationTest {
 @Container static PostgreSQLContainer postgres=new PostgreSQLContainer("postgres:17");
 @DynamicPropertySource static void database(DynamicPropertyRegistry r) {
  r.add("spring.datasource.url",postgres::getJdbcUrl);
  r.add("spring.datasource.username",postgres::getUsername);
  r.add("spring.datasource.password",postgres::getPassword);
 }
 @Autowired JdbcTemplate jdbc;
 @Autowired GalleryAssetRepository repo;
 GalleryAsset make() {
  return repo.saveAndFlush(new GalleryAsset("BACKGROUND","BIRTHDAY","Test",new byte[]{(byte)137,80,78,71},80,100));
 }
 @Test void migrationV15Applied() {
  assertEquals(1,jdbc.queryForObject("select count(*) from flyway_schema_history where version='15' and success=true",Integer.class));
 }
 @Test void newAssetsDefaultToDraftAndCanBePublished() {
  GalleryAsset a=make();
  assertFalse(repo.findById(a.getId()).orElseThrow().isPublished());
  assertTrue(repo.findByPublishedTrueOrderByTitleAsc().stream().noneMatch(x->x.getId().equals(a.getId())));
  jdbc.update("update invitation_gallery_assets set published=true where id=?",a.getId());
  assertTrue(repo.findByPublishedTrueOrderByTitleAsc().stream().anyMatch(x->x.getId().equals(a.getId())));
  jdbc.update("update invitation_gallery_assets set published=false where id=?",a.getId());
  assertTrue(repo.findByPublishedTrueOrderByTitleAsc().stream().noneMatch(x->x.getId().equals(a.getId())));
 }
 @Test void invalidCategoryRejectedByDatabase() {
  UUID id=UUID.randomUUID();
  assertThrows(Exception.class,()->jdbc.update(
   "insert into invitation_gallery_assets(id,category,occasion,title,content_type,image_png,width,height) values (?,?,?,?,?,?,?,?)",
   id,"INVALID","BIRTHDAY","Test","image/png",new byte[]{1},80,100));
 }
}
