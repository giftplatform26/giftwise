package com.giftplatform.qr.design;

import com.giftplatform.event.entity.Event;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name="qr_invitation_designs")
public class InvitationDesign {
 @Id @Column(nullable=false) private UUID id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="event_id",nullable=false) private Event event;
 @Column(nullable=false,length=100) private String name;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=30) private InvitationStyle style;
 @Column(length=180) private String greeting;
 @Column(name="image_png",columnDefinition="bytea") private byte[] imagePng;
 protected InvitationDesign() {}
 public InvitationDesign(Event event,String name,InvitationStyle style,String greeting,byte[] imagePng){
  this.id=UUID.randomUUID();this.event=event;this.name=name;this.style=style;this.greeting=greeting;this.imagePng=imagePng;
 }
 public UUID getId(){return id;}
 public Event getEvent(){return event;}
 public String getName(){return name;}
 public InvitationStyle getStyle(){return style;}
 public String getGreeting(){return greeting;}
 public byte[] getImagePng(){return imagePng;}
}
