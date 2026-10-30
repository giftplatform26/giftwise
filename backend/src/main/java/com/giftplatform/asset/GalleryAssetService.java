package com.giftplatform.asset;
import com.giftplatform.audit.entity.*;
import com.giftplatform.audit.service.AuditService;
import com.giftplatform.security.ActorContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Service
public class GalleryAssetService {
 private final GalleryAssetRepository repo; private final GalleryImageProcessor processor;
 private final AuditService audit; private final ActorContext actor;
 public GalleryAssetService(GalleryAssetRepository repo,GalleryImageProcessor processor,AuditService audit,ActorContext actor){
  this.repo=repo;this.processor=processor;this.audit=audit;this.actor=actor;
 }
 public record AssetInfo(UUID id,String category,String occasion,String title,boolean published,int width,int height,String tier) {}
 public AssetInfo info(GalleryAsset a){return new AssetInfo(a.getId(),a.getCategory(),a.getOccasion(),a.getTitle(),a.isPublished(),a.getWidth(),a.getHeight(),a.getTier());}
 public List<AssetInfo> published(){return repo.findByPublishedTrueOrderByTitleAsc().stream().map(this::info).toList();}
 public List<AssetInfo> all(){requireAdmin();return repo.findAll().stream().map(this::info).toList();}
 public GalleryAsset publishedImage(UUID id){GalleryAsset a=get(id);if(!a.isPublished() || !"FREE".equals(a.getTier())) throw new ResponseStatusException(HttpStatus.NOT_FOUND);return a;}
 public GalleryAsset adminImage(UUID id){requireAdmin();return get(id);}
 private GalleryAsset get(UUID id){return repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));}
 private void requireAdmin(){if(!actor.isPlatformAdmin()) throw new ResponseStatusException(HttpStatus.FORBIDDEN);}
 @Transactional public AssetInfo upload(String category,String occasion,String title,byte[] image){
  requireAdmin();
  if(!Set.of("BACKGROUND","DECORATION").contains(category)||occasion==null||!occasion.matches("[A-Za-z0-9_-]{1,50}")
     ||title==null||title.isBlank()||title.length()>120) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid asset metadata");
  var processed=processor.process(image,category);
  GalleryAsset asset=repo.save(new GalleryAsset(category,occasion,title.trim(),processed.png(),processed.width(),processed.height()));
  audit.recordChange(actor.requireAuthenticatedUserId(),AuditActorType.ADMIN,"INVITATION_ASSET",asset.getId(),AuditAction.CREATE,
   null,Map.of("category",asset.getCategory(),"occasion",asset.getOccasion(),"title",asset.getTitle(),"published",false),null);
  return info(asset);
 }
 @Transactional public AssetInfo publish(UUID id,boolean published){
  requireAdmin();GalleryAsset a=get(id);boolean old=a.isPublished();a.setPublished(published);
  audit.recordChange(actor.requireAuthenticatedUserId(),AuditActorType.ADMIN,"INVITATION_ASSET",id,AuditAction.STATUS_CHANGE,
   Map.of("published",old),Map.of("published",published),null);
  return info(a);
 }
}
