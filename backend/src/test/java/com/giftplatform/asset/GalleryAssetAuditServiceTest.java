package com.giftplatform.asset;

import com.giftplatform.audit.entity.AuditAction;
import com.giftplatform.audit.entity.AuditActorType;
import com.giftplatform.audit.service.AuditService;
import com.giftplatform.security.ActorContext;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GalleryAssetAuditServiceTest {
 @Test void uploadRecordsAdminCreateAudit() {
  var repo=mock(GalleryAssetRepository.class);
  var processor=mock(GalleryImageProcessor.class);
  var audit=mock(AuditService.class);
  var actor=mock(ActorContext.class);
  UUID admin=UUID.randomUUID();
  when(actor.isPlatformAdmin()).thenReturn(true);
  when(actor.requireAuthenticatedUserId()).thenReturn(admin);
  when(processor.process(any(byte[].class),eq("BACKGROUND"))).thenReturn(
   new GalleryImageProcessor.Processed(new byte[]{1,2,3},80,100));
  when(repo.save(any(GalleryAsset.class))).thenAnswer(i->i.getArgument(0));
  var service=new GalleryAssetService(repo,processor,audit,actor);
  var created=service.upload("BACKGROUND","BIRTHDAY","Test",new byte[]{1});
  assertFalse(created.published());
  verify(audit).recordChange(eq(admin),eq(AuditActorType.ADMIN),eq("INVITATION_ASSET"),
   eq(created.id()),eq(AuditAction.CREATE),isNull(),anyMap(),isNull());
 }
 @Test void publicationAndUnpublicationRecordStatusChange() {
  var repo=mock(GalleryAssetRepository.class);
  var audit=mock(AuditService.class);
  var actor=mock(ActorContext.class);
  when(actor.isPlatformAdmin()).thenReturn(true);
  UUID admin=UUID.randomUUID();
  when(actor.requireAuthenticatedUserId()).thenReturn(admin);
  GalleryAsset a=new GalleryAsset("DECORATION","BIRTHDAY","Test",new byte[]{1},80,100);
  when(repo.findById(a.getId())).thenReturn(Optional.of(a));
  var service=new GalleryAssetService(repo,mock(GalleryImageProcessor.class),audit,actor);
  service.publish(a.getId(),true);
  assertTrue(a.isPublished());
  service.publish(a.getId(),false);
  assertFalse(a.isPublished());
  verify(audit).recordChange(eq(admin),eq(AuditActorType.ADMIN),eq("INVITATION_ASSET"),
   eq(a.getId()),eq(AuditAction.STATUS_CHANGE),eq(Map.of("published",false)),
   eq(Map.of("published",true)),isNull());
  verify(audit).recordChange(eq(admin),eq(AuditActorType.ADMIN),eq("INVITATION_ASSET"),
   eq(a.getId()),eq(AuditAction.STATUS_CHANGE),eq(Map.of("published",true)),
   eq(Map.of("published",false)),isNull());
 }
}
