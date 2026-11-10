package db.migration;

import com.giftplatform.asset.PremiumInvitationArtwork;
import org.flywaydb.core.api.migration.*;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

public class V102__expanded_premium_stationery extends BaseJavaMigration {
 @Override public void migrate(Context context)throws Exception{
  String[][] designs={{"Amber Diwali","FESTIVAL"},{"Ivory & Ink","WEDDING"},{"Peacock Pavilion","ENGAGEMENT"},{"Garden Reverie","ANNIVERSARY"}};
  try(var statement=context.getConnection().prepareStatement("INSERT INTO invitation_gallery_assets(id,category,occasion,title,content_type,image_png,width,height,published,tier) VALUES(?,'BACKGROUND',?,?,'image/png',?,600,800,TRUE,'PREMIUM') ON CONFLICT(id) DO NOTHING")){
   for(int i=0;i<designs.length;i++){statement.setObject(1,UUID.nameUUIDFromBytes(("giftwise-premium-"+designs[i][0]).getBytes(StandardCharsets.UTF_8)));statement.setString(2,designs[i][1]);statement.setString(3,designs[i][0]);statement.setBytes(4,PremiumInvitationArtwork.render(i+6));statement.executeUpdate();}
  }
 }
}
