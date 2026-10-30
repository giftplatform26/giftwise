package com.giftplatform.hostpayment.qr;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

/** Host-scoped QR operations. A QR match is NOT proof of ownership or payment. */
public final class UpiQrImageService {
    private final RegisteredUpiResolver accounts;
    private final UpiQrImageStorage storage;

    public UpiQrImageService(RegisteredUpiResolver accounts, UpiQrImageStorage storage) {
        this.accounts = java.util.Objects.requireNonNull(accounts);
        this.storage = java.util.Objects.requireNonNull(storage);
    }

    public void upload(UUID hostId, UUID accountId, byte[] image) {
        var account = resolve(hostId, accountId);
        if(storage.hasUploaded(hostId,accountId)) throw new org.springframework.web.server.ResponseStatusException(
            org.springframework.http.HttpStatus.CONFLICT,"QR already uploaded for this account");
        // Upload validation matches the registered ID but does not establish account ownership.
        var parsed = UpiQrImageCodec.validateUpload(image, account.upiId());
        // Do not persist arbitrary client image bytes; re-encode a canonical QR PNG from parsed payee.
        // This removes image metadata, polyglot payloads and untrusted decorative content.
        byte[] cleanPng = UpiQrImageCodec.generatePng(parsed.payeeUpiId(),
            parsed.payeeName().isBlank() ? safeName(account.payeeName()) : safeName(parsed.payeeName()));
        storage.saveUploaded(hostId, accountId, cleanPng, sha256(parsed.payeeUpiId()));
    }

    public byte[] generate(UUID hostId, UUID accountId) {
        var account = resolve(hostId, accountId);
        // Generation from the registered server-side UPI ID is permitted before provider verification.
        byte[] png = UpiQrImageCodec.generatePng(account.upiId(), safeName(account.payeeName()));
        storage.saveGenerated(hostId, accountId, sha256(account.upiId().toLowerCase(java.util.Locale.ROOT)));
        return png;
    }

    public boolean hasUploaded(UUID hostId, UUID accountId) {
        resolve(hostId,accountId);
        return storage.hasUploaded(hostId,accountId);
    }
    public byte[] uploaded(UUID hostId, UUID accountId) {
        resolve(hostId, accountId);
        return storage.getUploaded(hostId, accountId);
    }

    private RegisteredUpiResolver.ResolvedUpi resolve(UUID hostId, UUID accountId) {
        if (hostId == null || accountId == null) throw new IllegalArgumentException("Host and account required");
        var account = accounts.resolve(hostId, accountId);
        if (account == null || account.upiId() == null) throw new IllegalArgumentException("UPI account unavailable");
        return account;
    }
    private static String safeName(String name) {
        if (name == null || name.isBlank()) return "Gift Host";
        String clean = name.strip();
        return clean.length() > 100 ? clean.substring(0, 100) : clean;
    }
    private static String sha256(String s) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
}
