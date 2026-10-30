package com.giftplatform.hostpayment.qr;

import java.util.UUID;
/** Server-controlled persistence; implementations must scope all reads to hostId. */
public interface UpiQrImageStorage {
    void saveUploaded(UUID hostId, UUID upiAccountId, byte[] png, String payloadSha256);
    void saveGenerated(UUID hostId, UUID upiAccountId, String payloadSha256);
    byte[] getUploaded(UUID hostId, UUID upiAccountId);
    boolean hasUploaded(UUID hostId, UUID upiAccountId);
    boolean hasValidQr(UUID hostId, UUID upiAccountId);
}
