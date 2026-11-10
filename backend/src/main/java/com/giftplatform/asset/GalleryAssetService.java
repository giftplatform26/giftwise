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
 @org.springframework.beans.factory.annotation.Autowired private com.giftplatform.monetization.CommercialAccessService commerce;
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
 public byte[] preview(UUID id){
  actor.requireAuthenticatedUserId();GalleryAsset asset=get(id);
  if(!asset.isPublished())throw new ResponseStatusException(HttpStatus.NOT_FOUND);
  return GalleryPreviewRenderer.watermarked(asset.getImagePng());
 }
 public GalleryAsset purchasedImage(UUID id){
  UUID host=actor.requireAuthenticatedUserId();GalleryAsset asset=get(id);
  if(!asset.isPublished())throw new ResponseStatusException(HttpStatus.NOT_FOUND);
  if(!"FREE".equals(asset.getTier())&&!commerce.template(host,id))throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED,"Purchase this template or upgrade your host plan");
  return asset;
 }
 @Transactional public AssetInfo tier(UUID id,String tier){
  requireAdmin();if(!Set.of("FREE","PREMIUM").contains(tier==null?"":tier))throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
  GalleryAsset asset=get(id);String old=asset.getTier();asset.setTier(tier);
  audit.recordChange(actor.requireAuthenticatedUserId(),AuditActorType.ADMIN,"INVITATION_ASSET",id,AuditAction.UPDATE,Map.of("tier",old),Map.of("tier",tier),null);
  return info(asset);
 }
 public GalleryAsset adminImage(UUID id){requireAdmin();return get(id);}
 private GalleryAsset get(UUID id){return repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));}
 private void requireAdmin(){if(!actor.isPlatformAdmin()) throw new ResponseStatusException(HttpStatus.FORBIDDEN);}
 @Transactional public AssetInfo upload(String category,String occasion,String title,byte[] image){return upload(category,occasion,title,image,"FREE");}
 @Transactional public AssetInfo upload(String category,String occasion,String title,byte[] image,String tier){
  requireAdmin();
  if(!Set.of("FREE","PREMIUM").contains(tier==null?"":tier))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Choose Free or Premium access");
  if(!Set.of("BACKGROUND","DECORATION").contains(category)||occasion==null||!occasion.matches("[A-Za-z0-9_-]{1,50}")
     ||title==null||title.isBlank()||title.length()>120) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid asset metadata");
  var processed=processor.process(image,category);
  GalleryAsset draft=new GalleryAsset(category,occasion,title.trim(),processed.png(),processed.width(),processed.height());draft.setTier(tier);
  GalleryAsset asset=repo.save(draft);
  audit.recordChange(actor.requireAuthenticatedUserId(),AuditActorType.ADMIN,"INVITATION_ASSET",asset.getId(),AuditAction.CREATE,
   null,Map.of("category",asset.getCategory(),"occasion",asset.getOccasion(),"title",asset.getTitle(),"tier",asset.getTier(),"published",false),null);
  return info(asset);
 }
 @Transactional public AssetInfo publish(UUID id,boolean published){
  requireAdmin();GalleryAsset a=get(id);boolean old=a.isPublished();a.setPublished(published);
  audit.recordChange(actor.requireAuthenticatedUserId(),AuditActorType.ADMIN,"INVITATION_ASSET",id,AuditAction.STATUS_CHANGE,
   Map.of("published",old),Map.of("published",published),null);
  return info(a);
 }
}
