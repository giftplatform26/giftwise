package com.giftplatform.asset;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Random;
import javax.imageio.ImageIO;

/** Original, code-drawn stationery with an open centre for editable event details. */
public final class PremiumInvitationArtwork {
 private PremiumInvitationArtwork(){}
 public static byte[] render(int variant) throws Exception {
  if(variant<0||variant>9)throw new IllegalArgumentException("Unknown stationery design");
  var image=new BufferedImage(600,800,BufferedImage.TYPE_INT_RGB);var g=image.createGraphics();
  try{
   g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
   g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
   String[][] papers={{"#fff8f1","#f3dfe3"},{"#fff7e6","#f3e5ec"},{"#eef0ff","#e3edf6"},{"#f6f5e8","#e0eadc"},{"#173f37","#31564c"},{"#fff1f5","#eadfea"},{"#fff4d8","#f3dfc7"},{"#fbf8f1","#e5e7ea"},{"#edf7f0","#e0ede6"},{"#fcf5ee","#e7edde"}};
   g.setPaint(new GradientPaint(0,0,c(papers[variant][0]),600,800,c(papers[variant][1])));g.fillRect(0,0,600,800);
   texture(g,variant);
   switch(variant){case 0->romance(g);case 1->carnival(g);case 2->moonbeam(g);case 3->botanical(g);case 4->golden(g);case 5->palace(g);case 6->festival(g);case 7->ink(g);case 8->peacock(g);case 9->garden(g);default->throw new IllegalStateException();}
  }finally{g.dispose();}
  var bytes=new ByteArrayOutputStream();ImageIO.write(image,"png",bytes);return bytes.toByteArray();
 }
 private static Color c(String hex){return Color.decode(hex);}
 private static void texture(Graphics2D g,int variant){var r=new Random(1900+variant);g.setColor(new Color(114,82,67,9));for(int i=0;i<6500;i++)g.fillRect(r.nextInt(600),r.nextInt(800),1,1);}
 private static void line(Graphics2D g,double x1,double y1,double x2,double y2){g.draw(new Line2D.Double(x1,y1,x2,y2));}
 private static void border(Graphics2D g,Color ink,int inset,int radius){g.setColor(ink);g.setStroke(new BasicStroke(1));g.drawRoundRect(inset,inset,600-2*inset,800-2*inset,radius,radius);}
 private static void leaf(Graphics2D g,double x,double y,double size,double angle,Color shade){
  var tx=g.getTransform();g.translate(x,y);g.rotate(angle);var p=new Path2D.Double();p.moveTo(0,0);p.curveTo(-size*.62,-size*.3,-size*.28,-size*.92,0,-size);p.curveTo(size*.36,-size*.7,size*.48,-size*.32,0,0);
  g.setPaint(new GradientPaint(0,0,shade,0,(float)-size,shade.brighter()));g.fill(p);g.setColor(new Color(255,255,245,100));g.setStroke(new BasicStroke(.65f));line(g,0,0,0,-size*.86);g.setTransform(tx);
 }
 private static void flower(Graphics2D g,double x,double y,double radius,Color shade){
  var tx=g.getTransform();g.translate(x,y);
  for(int layer=0;layer<4;layer++){
   double r=radius*(1-layer*.18);int petals=layer%2==0?9:7;
   for(int i=0;i<petals;i++){var turn=g.getTransform();g.rotate(i*Math.PI*2/petals+layer*.4);g.setPaint(new GradientPaint(0,(float)-r,shade.brighter(),0,5,shade));g.fill(new Ellipse2D.Double(-r*.30,-r,r*.60,r*1.12));g.setColor(new Color(255,247,242,65));g.draw(new Arc2D.Double(-r*.27,-r,r*.54,r*.95,50,200,Arc2D.OPEN));g.setTransform(turn);}
  }
  g.setColor(c("#cba55e"));g.fill(new Ellipse2D.Double(-radius*.12,-radius*.12,radius*.24,radius*.24));g.setTransform(tx);
 }
 private static void star(Graphics2D g,double x,double y,double r,Color shade){g.setColor(shade);var p=new Path2D.Double();for(int i=0;i<8;i++){double a=i*Math.PI/4-Math.PI/2,rr=i%2==0?r:r*.23;double px=x+Math.cos(a)*rr,py=y+Math.sin(a)*rr;if(i==0)p.moveTo(px,py);else p.lineTo(px,py);}p.closePath();g.fill(p);}
 private static void bouquet(Graphics2D g,int x,int y,boolean flip,Color shade){
  var tx=g.getTransform();g.translate(x,y);if(flip)g.rotate(Math.PI);
  for(int i=0;i<9;i++){double angle=-1.6+i*.38;leaf(g,Math.sin(angle)*30,Math.cos(angle)*30,38+i%3*11,angle,c(i%2==0?"#78927c":"#9eac8a"));}
  flower(g,-20,-12,35,shade);flower(g,35,19,29,shade.brighter());flower(g,-4,48,24,c("#e6bdb3"));flower(g,49,-24,18,c("#f6e5d6"));
  g.setColor(c("#d0ae72"));for(int i=0;i<7;i++)g.fillOval(-60+i*13,-45+(i%3)*10,3,3);g.setTransform(tx);
 }
 private static void romance(Graphics2D g){
  border(g,c("#caa875"),22,26);border(g,new Color(202,168,117,90),29,22);
  g.setColor(new Color(255,253,247,180));g.fillRoundRect(64,92,472,637,110,110);
  g.setColor(c("#d2b381"));g.setStroke(new BasicStroke(1));g.draw(new Arc2D.Double(108,30,384,180,0,180,Arc2D.OPEN));line(g,108,120,108,180);line(g,492,120,492,180);
  bouquet(g,42,63,false,c("#c58291"));bouquet(g,556,735,true,c("#b9778d"));
  for(int i=0;i<5;i++){leaf(g,24,330+i*30,33,1.0,c("#a4b09a"));leaf(g,576,470-i*30,33,-2.1,c("#a4b09a"));}
  star(g,300,67,12,c("#ba965e"));
 }
 private static void balloon(Graphics2D g,int x,int y,int w,int h,Color shade){g.setPaint(new GradientPaint(x,y,shade.brighter(),x+w,y+h,shade));g.fillOval(x,y,w,h);g.setColor(new Color(255,255,255,130));g.fillOval(x+w/5,y+h/6,Math.max(3,w/8),h/3);g.setColor(shade.darker());var triangle=new Polygon(new int[]{x+w/2,x+w/2-4,x+w/2+4},new int[]{y+h-2,y+h+7,y+h+7},3);g.fill(triangle);g.setStroke(new BasicStroke(.8f));g.draw(new CubicCurve2D.Double(x+w/2.,y+h+6,x-10,y+h+35,x+w+20,y+h+55,x+w/2.,y+h+100));}
 private static void carnival(Graphics2D g){
  Color[] rainbow={c("#e6b5be"),c("#efd09c"),c("#b8cbbc"),c("#bfc4df")};g.setStroke(new BasicStroke(10));for(int i=0;i<4;i++){g.setColor(rainbow[i]);g.draw(new Arc2D.Double(150+i*12,-66+i*12,300-i*24,260-i*24,0,180,Arc2D.OPEN));}
  border(g,c("#d8b18b"),23,38);
  balloon(g,-10,25,70,91,c("#cc829d"));balloon(g,42,1,58,77,c("#e5b66e"));balloon(g,525,51,69,91,c("#9eafcd"));
  balloon(g,532,659,57,76,c("#d79bad"));balloon(g,-15,682,67,89,c("#95b5a7"));
  var r=new Random(93);for(int i=0;i<80;i++){int x=i%2==0?r.nextInt(62):538+r.nextInt(62),y=180+r.nextInt(450);g.setColor(rainbow[i%4]);var tx=g.getTransform();g.rotate(i,x,y);g.fillRoundRect(x,y,3,10,2,2);g.setTransform(tx);}
  star(g,300,70,15,c("#c39c60"));for(int i=0;i<6;i++)star(g,85+i*85,760+i%2*8,4,rainbow[i%4]);
 }
 private static void cloud(Graphics2D g,int x,int y,int width,Color shade){g.setColor(shade);g.fillOval(x,y,width,90);g.fillOval(x+width/4,y-25,width/2,90);g.fillOval(x+width/2,y-2,width/2+20,80);}
 private static void moonbeam(Graphics2D g){
  border(g,new Color(155,163,198,100),25,160);
  cloud(g,-95,682,280,c("#d0d6ec"));cloud(g,380,705,270,c("#d6d4e9"));cloud(g,-70,733,350,c("#f3f0fa"));cloud(g,275,750,350,c("#f6f3fb"));
  g.setPaint(new GradientPaint(270,22,c("#f3db9e"),320,100,c("#cfad68")));g.fillOval(268,25,65,65);g.setColor(c("#edf0fc"));g.fillOval(290,14,56,64);
  Color gold=c("#c5a574");for(int i=0;i<12;i++){int x=i%2==0?34+i%3*17:530+i%3*17,y=125+i*45;star(g,x,y,4+i%3*2,gold);}
  g.setColor(new Color(143,155,190,80));g.setStroke(new BasicStroke(.7f));line(g,40,200,65,260);line(g,65,260,34,310);line(g,560,380,542,440);line(g,542,440,568,510);
  star(g,218,62,5,gold);star(g,374,53,6,gold);star(g,345,103,3,gold);
 }
 private static void fern(Graphics2D g,double x,double y,double rotation,double length){var tx=g.getTransform();g.translate(x,y);g.rotate(rotation);g.setColor(c("#a4ad86"));g.setStroke(new BasicStroke(1.2f));line(g,0,0,0,-length);for(int i=1;i<12;i++){double yy=-length*i/12,size=length*.18*(1-i/17.);leaf(g,0,yy,size,-.95,c(i%2==0?"#728d71":"#9aae8c"));leaf(g,0,yy,size,.95,c(i%2==0?"#9aae8c":"#7b987a"));}g.setTransform(tx);}
 private static void botanical(Graphics2D g){
  border(g,new Color(135,153,112,130),25,14);border(g,new Color(176,160,112,100),33,12);
  fern(g,14,780,-.16,300);fern(g,-15,730,.55,240);fern(g,582,30,Math.PI-.2,240);fern(g,621,56,Math.PI+.4,240);
  g.setColor(new Color(255,253,246,140));g.fillRoundRect(82,106,436,600,28,28);
  g.setColor(c("#b9a777"));g.setStroke(new BasicStroke(1));g.draw(new Arc2D.Double(265,42,70,70,0,360,Arc2D.OPEN));leaf(g,297,92,26,.4,c("#829675"));leaf(g,303,82,20,-.6,c("#a1ac87"));
  for(int i=0;i<7;i++){g.setColor(c("#c6b78a"));g.fillOval(530+i%3*13,650+i*12,3,3);}
 }
 private static void golden(Graphics2D g){
  border(g,c("#bca16a"),18,4);border(g,new Color(188,161,106,110),26,4);
  g.setColor(c("#fff9eb"));g.fillRoundRect(43,100,514,645,18,18);g.setColor(c("#d1af70"));g.setStroke(new BasicStroke(1));g.drawRoundRect(51,108,498,629,14,14);
  for(int corner=0;corner<4;corner++){var tx=g.getTransform();g.translate(corner%2==0?27:573,corner<2?27:773);g.rotate(corner<2?0:Math.PI);g.setColor(c("#c2a569"));for(int i=0;i<9;i++){double a=i*Math.PI/16;line(g,0,0,Math.sin(a)*78,Math.cos(a)*62);}g.setTransform(tx);}
  g.setColor(c("#d1b779"));g.setStroke(new BasicStroke(1));g.draw(new Arc2D.Double(215,8,170,140,0,180,Arc2D.OPEN));g.draw(new Arc2D.Double(230,22,140,112,0,180,Arc2D.OPEN));star(g,300,58,17,c("#e0c284"));
  for(int y=190;y<650;y+=85){star(g,23,y,5,c("#c3ab76"));star(g,577,y,5,c("#c3ab76"));}
 }
 private static void palace(Graphics2D g){
  border(g,c("#c6a06d"),22,16);g.setColor(new Color(255,251,247,135));g.fillRoundRect(74,106,452,621,30,30);
  g.setColor(c("#bd9a70"));g.setStroke(new BasicStroke(1));
  var arch=new Path2D.Double();arch.moveTo(90,180);arch.lineTo(90,115);arch.curveTo(90,46,240,90,300,26);arch.curveTo(360,90,510,46,510,115);arch.lineTo(510,180);g.draw(arch);
  g.setColor(new Color(191,155,120,120));for(int i=0;i<9;i++){line(g,30+i*67,23,30+i*67,50+i%3*15);g.fillOval(27+i*67,50+i%3*15,6,9);}
  bouquet(g,42,695,true,c("#b280a4"));bouquet(g,548,103,false,c("#cf9db1"));
  star(g,300,80,8,c("#b08a5e"));
 }
 private static void mandala(Graphics2D g,double x,double y,double radius){var original=g.getTransform();g.translate(x,y);g.setColor(new Color(184,140,71,140));g.setStroke(new BasicStroke(.8f));for(int i=0;i<24;i++){var turn=g.getTransform();g.rotate(i*Math.PI/12);g.draw(new Ellipse2D.Double(-radius*.11,-radius,radius*.22,radius*.58));line(g,0,-radius*.35,0,-radius*.88);g.setTransform(turn);}for(int i=1;i<5;i++)g.draw(new Ellipse2D.Double(-radius*i/5,-radius*i/5,radius*2*i/5,radius*2*i/5));g.setTransform(original);}
 private static void festival(Graphics2D g){border(g,c("#bd995f"),22,20);mandala(g,0,0,185);mandala(g,600,800,185);mandala(g,600,0,115);mandala(g,0,800,115);g.setColor(new Color(255,251,239,150));g.fillRoundRect(66,105,468,620,35,35);for(int i=0;i<9;i++){g.setColor(c(i%2==0?"#c6a36f":"#c58670"));line(g,65+i*60,22,65+i*60,52+i%3*15);g.fillOval(61+i*60,52+i%3*15,8,12);}star(g,300,83,9,c("#b68c4e"));}
 private static void ink(Graphics2D g){border(g,c("#6c788a"),24,6);border(g,new Color(108,120,138,100),31,5);fern(g,18,795,-.2,300);fern(g,585,12,3.0,240);g.setColor(new Color(252,250,243,165));g.fillRoundRect(75,105,450,625,22,22);g.setColor(c("#7a879a"));g.setStroke(new BasicStroke(.8f));g.draw(new Arc2D.Double(245,30,110,65,0,360,Arc2D.OPEN));g.draw(new Arc2D.Double(251,36,98,53,0,360,Arc2D.OPEN));star(g,300,63,10,c("#a69169"));}
 private static void feather(Graphics2D g,double x,double y,double rotation){var tx=g.getTransform();g.translate(x,y);g.rotate(rotation);g.setColor(c("#b6a26c"));g.setStroke(new BasicStroke(1));line(g,0,0,0,-160);for(int i=0;i<13;i++){g.setColor(c(i%2==0?"#76a89a":"#8dafb2"));g.draw(new QuadCurve2D.Double(0,-i*11,-58,-i*11-28,-38,-i*11-38));g.draw(new QuadCurve2D.Double(0,-i*11,58,-i*11-28,38,-i*11-38));}g.setColor(c("#6eab98"));g.fillOval(-27,-182,54,75);g.setPaint(new GradientPaint(-15,-180,c("#dec68c"),15,-110,c("#b59b59")));g.fillOval(-17,-170,34,54);g.setColor(c("#526d8c"));g.fillOval(-10,-159,20,33);g.setColor(c("#b4d0c8"));g.fillOval(-5,-154,10,21);g.setTransform(tx);}
 private static void peacock(Graphics2D g){border(g,c("#b9a56e"),23,14);feather(g,14,790,-.3);feather(g,-5,735,.32);feather(g,589,8,Math.PI-.25);g.setColor(new Color(251,251,238,165));g.fillRoundRect(78,106,444,620,50,50);mandala(g,300,65,27);}
 private static void garden(Graphics2D g){border(g,c("#b5a175"),24,28);fern(g,10,800,-.26,235);fern(g,589,0,Math.PI-.2,220);bouquet(g,35,730,true,c("#c28e9e"));bouquet(g,561,72,false,c("#b693af"));flower(g,30,353,23,c("#d1b5aa"));flower(g,567,478,22,c("#cba3b6"));g.setColor(new Color(255,253,242,130));g.fillRoundRect(82,108,436,617,55,55);star(g,300,64,11,c("#b49869"));}

}
