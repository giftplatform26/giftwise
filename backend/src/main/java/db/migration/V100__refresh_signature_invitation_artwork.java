package db.migration;

import com.giftplatform.asset.PremiumInvitationArtwork;
import org.flywaydb.core.api.migration.*;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

public class V100__refresh_signature_invitation_artwork extends BaseJavaMigration {
 @Override public void migrate(Context context) throws Exception {
  String[] titles={"Rosewood Romance","Birthday Carnival","Moonbeam Baby","Botanical Haven","Golden Together","Palace of Petals"};
  try(var statement=context.getConnection().prepareStatement("UPDATE invitation_gallery_assets SET image_png=?,updated_at=now() WHERE id=? AND title=? AND category='BACKGROUND'")){
   for(int i=0;i<titles.length;i++){statement.setBytes(1,PremiumInvitationArtwork.render(i));statement.setObject(2,UUID.nameUUIDFromBytes(("giftwise-premium-"+titles[i]).getBytes(StandardCharsets.UTF_8)));statement.setString(3,titles[i]);statement.executeUpdate();}
  }
 }
}
