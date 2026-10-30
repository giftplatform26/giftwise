package com.giftplatform.hostpayment.qr;

import java.util.UUID;
/** Trusted bridge to the existing encrypted host payment profile module.
 * Implementations MUST check host ownership, enabled/verified status, and decrypt server-side.
 * NEVER accept a client-provided UPI ID as the source of truth.
 */
public interface RegisteredUpiResolver {
    ResolvedUpi resolve(UUID authenticatedHostId, UUID upiAccountId);
    record ResolvedUpi(String upiId, String payeeName, boolean verified) {}
}
