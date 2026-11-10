package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.UUID;
import javax.imageio.ImageIO;

/** Code-drawn artwork; clean images are served exclusively through commerce access checks. */
public class V98__premium_invitation_collection extends BaseJavaMigration {
    private static final String[][] COLLECTION = {
        {"Rosewood Romance", "WEDDING", "#fff4f1", "#a85671"},
        {"Birthday Carnival", "BIRTHDAY", "#fff8df", "#a65a94"},
        {"Moonbeam Baby", "BABY_SHOWER", "#eef4ff", "#657cb2"},
        {"Botanical Haven", "HOUSEWARMING", "#f2f6e9", "#52785d"},
        {"Golden Together", "ANNIVERSARY", "#fff7e8", "#a17a37"},
        {"Palace of Petals", "ENGAGEMENT", "#fff0f6", "#a05782"}
    };
    @Override public void migrate(Context context) throws Exception {
        try (var statement = context.getConnection().prepareStatement("INSERT INTO invitation_gallery_assets(id,category,occasion,title,content_type,image_png,width,height,published,tier) VALUES(?,'BACKGROUND',?,?,'image/png',?,600,800,TRUE,'PREMIUM')")) {
            for (int i=0;i<COLLECTION.length;i++) {
                var t=COLLECTION[i];
                statement.setObject(1, UUID.nameUUIDFromBytes(("giftwise-premium-"+t[0]).getBytes(java.nio.charset.StandardCharsets.UTF_8)));
                statement.setString(2,t[1]);statement.setString(3,t[0]);statement.setBytes(4,artwork(i));statement.executeUpdate();
            }
        }
    }
    public static byte[] artwork(int variant) throws Exception {
        var t=COLLECTION[variant];var image=new BufferedImage(600,800,BufferedImage.TYPE_INT_RGB);var g=image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.decode(t[2]));g.fillRect(0,0,600,800);
            Color ink=Color.decode(t[3]);g.setColor(new Color(ink.getRed(),ink.getGreen(),ink.getBlue(),100));g.setStroke(new BasicStroke(2));
            g.drawRoundRect(22,22,556,756,100,100);g.drawRoundRect(32,32,536,736,90,90);
            for(int corner=0;corner<4;corner++) {
                var original=g.getTransform();g.translate(corner%2==0?55:545,corner<2?65:735);
                if(variant==1) { // Balloons and confetti
                    for(int j=0;j<5;j++){g.setColor(new Color(ink.getRed(),ink.getGreen(),ink.getBlue(),90+j*20));g.fillOval(-35+j*12,-45+j%2*20,25,35);g.drawLine(-23+j*12,-10+j%2*20,0,60);}
                } else if(variant==2) { // Moon and stars
                    g.setColor(ink);g.fillOval(-30,-30,60,60);g.setColor(Color.decode(t[2]));g.fillOval(-12,-38,56,56);
                    g.setColor(ink);for(int j=0;j<5;j++){int x=-40+j*20,y=40+j%2*18;g.drawLine(x-4,y,x+4,y);g.drawLine(x,y-4,x,y+4);}
                } else if(variant==3) { // Botanical branches
                    for(int j=0;j<8;j++){g.rotate(Math.PI/4);g.fill(new Ellipse2D.Double(0,0,18,65));}
                } else { // Layered floral rosettes
                    for(int j=0;j<10;j++){g.rotate(Math.PI/5);g.setColor(new Color(ink.getRed(),ink.getGreen(),ink.getBlue(),65));g.fill(new Ellipse2D.Double(-12,-65,24,60));}
                    g.setColor(new Color(196,158,82));g.fillOval(-9,-9,18,18);
                }
                g.setTransform(original);
            }
            g.setColor(new Color(196,158,82,160));
            for(int y=125;y<700;y+=70){g.fillOval(42,y,4,4);g.fillOval(554,y,4,4);}
        } finally {g.dispose();}
        var bytes=new ByteArrayOutputStream();ImageIO.write(image,"png",bytes);return bytes.toByteArray();
    }
}
