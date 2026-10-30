package com.giftplatform.hostpayment.qr;

import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class JdbcUpiQrImageStorage implements UpiQrImageStorage {
    private final JdbcTemplate jdbc;
    public JdbcUpiQrImageStorage(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    @Override public boolean hasUploaded(UUID hostId,UUID accountId) {
        Integer count=jdbc.queryForObject("SELECT COUNT(*) FROM host_upi_qr_images WHERE host_id=? AND upi_account_id=? AND source='UPLOADED'",Integer.class,hostId,accountId);
        return count!=null && count>0;
    }
    @Override public boolean hasValidQr(UUID hostId,UUID accountId) {
        Integer count=jdbc.queryForObject("SELECT COUNT(*) FROM host_upi_qr_images WHERE host_id=? AND upi_account_id=? AND source IN ('UPLOADED','GENERATED') AND payload_sha256 IS NOT NULL",Integer.class,hostId,accountId);
        return count!=null && count>0;
    }
    @Override public void saveUploaded(UUID hostId,UUID accountId,byte[] png,String digest) {
        int inserted=jdbc.update("""
          INSERT INTO host_upi_qr_images(host_id,upi_account_id,image_png,payload_sha256,source)
          VALUES(?,?,?,?,'UPLOADED')
          ON CONFLICT(host_id,upi_account_id,source) DO NOTHING
          """,hostId,accountId,png,digest);
        if(inserted!=1) throw new ResponseStatusException(HttpStatus.CONFLICT,"QR already uploaded for this account");
    }
    @Override public void saveGenerated(UUID hostId,UUID accountId,String digest) {
        jdbc.update("""
          INSERT INTO host_upi_qr_images(host_id,upi_account_id,image_png,payload_sha256,source)
          VALUES(?,?,NULL,?,'GENERATED')
          ON CONFLICT(host_id,upi_account_id,source) DO UPDATE SET
          image_png=NULL,payload_sha256=EXCLUDED.payload_sha256,
          updated_at=CURRENT_TIMESTAMP
          """,hostId,accountId,digest);
    }
    @Override public byte[] getUploaded(UUID hostId,UUID accountId) {
        var found=jdbc.query("""
          SELECT image_png FROM host_upi_qr_images
          WHERE host_id=? AND upi_account_id=? AND source='UPLOADED'
          """,(rs,n)->rs.getBytes(1),hostId,accountId);
        if(found.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Uploaded QR not found");
        return found.get(0);
    }
}
