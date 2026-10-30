package com.giftplatform.asset;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="invitation_gallery_assets")
public class GalleryAsset {
 @Id private UUID id;
 @Column(nullable=false,length=20) private String category;
 @Column(nullable=false,length=50) private String occasion;
 @Column(nullable=false,length=120) private String title;
 @Column(name="content_type",nullable=false,length=20) private String contentType;
 @Column(name="image_png",nullable=false,columnDefinition="bytea") private byte[] imagePng;
 @Column(nullable=false) private int width;
 @Column(nullable=false) private int height;
 @Column(nullable=false) private boolean published;
 @Column(nullable=false,length=20) private String tier="FREE";
 @Column(name="created_at",insertable=false,updatable=false) private Instant createdAt;
 @Column(name="updated_at",insertable=false) private Instant updatedAt;
 protected GalleryAsset() {}
 public GalleryAsset(String category,String occasion,String title,byte[] imagePng,int width,int height){
  this.id=UUID.randomUUID();this.category=category;this.occasion=occasion;this.title=title;
  this.imagePng=imagePng;this.width=width;this.height=height;this.contentType="image/png";
 }
 public UUID getId(){return id;} public String getCategory(){return category;}
 public String getOccasion(){return occasion;} public String getTitle(){return title;}
 public String getContentType(){return contentType;} public byte[] getImagePng(){return imagePng;}
 public int getWidth(){return width;} public int getHeight(){return height;}
 public boolean isPublished(){return published;}
 public String getTier(){return tier;}
 public Instant getCreatedAt(){return createdAt;}
 public void setPublished(boolean value){published=value;updatedAt=Instant.now();}
}
