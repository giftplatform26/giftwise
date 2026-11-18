package com.giftplatform.hostpayment.qr;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Parses the text decoded from an uploaded QR image and builds UPI intent payloads.
 * This class DOES NOT verify account ownership, payment completion, or QR image safety.
 */
public final class UpiQrPayload {
    private UpiQrPayload() {}

    private static final Set<String> ALLOWED = Set.of("pa", "pn", "cu", "am", "tn", "tr", "mc", "mode", "purpose", "orgid", "sign", "aid");
    public record Parsed(String payeeUpiId, String payeeName, String currency, boolean hasAmount) {}

    public static Parsed parse(String raw) {
        if (raw == null || raw.length() > 4096 || raw.isBlank())
            throw new IllegalArgumentException("Invalid UPI QR payload");
        URI uri;
        try { uri = URI.create(raw); }
        catch (IllegalArgumentException ex) { throw new IllegalArgumentException("Invalid UPI QR payload"); }
        if (!"upi".equalsIgnoreCase(uri.getScheme()) ||
            !"pay".equalsIgnoreCase(uri.getHost()) ||
            (uri.getRawPath() != null && !uri.getRawPath().isEmpty()) ||
            uri.getRawFragment() != null || uri.getRawUserInfo() != null ||
            uri.getPort() != -1 || uri.getRawQuery() == null)
            throw new IllegalArgumentException("Unsupported QR destination");

        Map<String,String> values = new HashMap<>();
        for (String pair : uri.getRawQuery().split("&", -1)) {
            int eq = pair.indexOf('=');
            if (eq <= 0) throw new IllegalArgumentException("Malformed UPI parameter");
            String key = decode(pair.substring(0,eq)).toLowerCase(Locale.ROOT);
            String value = decode(pair.substring(eq+1));
            if (!ALLOWED.contains(key) || values.putIfAbsent(key,value) != null)
                throw new IllegalArgumentException("Unexpected or repeated UPI parameter");
            if (value.length() > 256 || value.chars().anyMatch(c -> Character.isISOControl(c)))
                throw new IllegalArgumentException("Invalid UPI parameter");
        }
        String pa = values.get("pa");
        if (pa == null || !pa.matches("(?i)[a-z0-9._-]{2,128}@[a-z0-9.-]{2,64}"))
            throw new IllegalArgumentException("Invalid UPI payee ID");
        String currency = values.getOrDefault("cu","INR");
        if (!"INR".equalsIgnoreCase(currency))
            throw new IllegalArgumentException("Unsupported currency");
        if (values.containsKey("am")) {
            try {
                var amount = new java.math.BigDecimal(values.get("am"));
                if (amount.signum() <= 0 || amount.scale() > 2 || amount.precision() > 12)
                    throw new IllegalArgumentException("Invalid amount");
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Invalid amount");
            }
        }
        return new Parsed(pa.toLowerCase(Locale.ROOT),values.getOrDefault("pn",""),"INR",values.containsKey("am"));
    }

    public static void requireMatchingPayee(String qrText, String registeredUpiId) {
        Parsed parsed = parse(qrText);
        if (registeredUpiId == null || !parsed.payeeUpiId().equals(registeredUpiId.trim().toLowerCase(Locale.ROOT)))
            throw new IllegalArgumentException("QR payee does not match registered UPI ID");
    }

    /** Build an amount-free QR payload; never include payment success or verification claims. */
    public static String generate(String verifiedUpiId, String payeeName) {
        if (verifiedUpiId == null || !verifiedUpiId.matches("(?i)[a-z0-9._-]{2,128}@[a-z0-9.-]{2,64}"))
            throw new IllegalArgumentException("Invalid UPI ID");
        if (payeeName == null || payeeName.isBlank() || payeeName.length() > 100 ||
            payeeName.chars().anyMatch(Character::isISOControl))
            throw new IllegalArgumentException("Invalid payee name");
        return "upi://pay?pa=" + encode(verifiedUpiId.toLowerCase(Locale.ROOT)) +
            "&pn=" + encode(payeeName.trim()) + "&cu=INR";
    }

    /**
     * Build a single-payment link/QR with the amount and a note (e.g. "GiftWise GW-7K3P9Q") pre-filled.
     * Guests can still edit them in their UPI app; the host confirms receipt separately.
     */
    public static String generatePayment(String verifiedUpiId, String payeeName, long amountPaise, String note) {
        if (amountPaise < 100 || amountPaise > 100_000_000L) throw new IllegalArgumentException("Invalid amount");
        if (note == null || !note.matches("[A-Za-z0-9 \\-]{1,50}")) throw new IllegalArgumentException("Invalid note");
        String amount = java.math.BigDecimal.valueOf(amountPaise, 2).toPlainString();
        return generate(verifiedUpiId, payeeName) + "&am=" + amount + "&tn=" + encode(note);
    }

    public static final String TRANSACTION_REF = "GWT[A-Z0-9]{12,32}";

    /**
     * Build a UPI intent URI for one payment attempt: pa, pn, am, cu, tr (unique attempt reference) and tn.
     * Values are percent-encoded per RFC 3986 (spaces as %20, which every UPI app decodes; '+' is not safe).
     * Caller MUST have authorised the payee as the event host's eligible destination.
     */
    public static String generateIntent(String verifiedUpiId, String payeeName, long amountPaise,
                                        String transactionRef, String note, boolean includeTransactionRef) {
        if (verifiedUpiId == null || !verifiedUpiId.matches("(?i)[a-z0-9._-]{2,128}@[a-z0-9.-]{2,64}"))
            throw new IllegalArgumentException("Invalid UPI ID");
        if (payeeName == null || payeeName.isBlank() || payeeName.length() > 100 ||
            payeeName.chars().anyMatch(Character::isISOControl))
            throw new IllegalArgumentException("Invalid payee name");
        if (amountPaise < 100 || amountPaise > 100_000_000L) throw new IllegalArgumentException("Invalid amount");
        if (transactionRef == null || !transactionRef.matches(TRANSACTION_REF))
            throw new IllegalArgumentException("Invalid transaction reference");
        if (note == null || !note.matches("[A-Za-z0-9 \\-]{1,50}")) throw new IllegalArgumentException("Invalid note");
        String amount = java.math.BigDecimal.valueOf(amountPaise, 2).toPlainString();
        return "upi://pay?pa=" + percent(verifiedUpiId.toLowerCase(Locale.ROOT)) +
            "&pn=" + percent(payeeName.strip()) + "&am=" + amount + "&cu=INR" +
            (includeTransactionRef ? "&tr=" + percent(transactionRef) : "") + "&tn=" + percent(note);
    }

    /** RFC 3986 unreserved characters pass through; everything else is %XX (UTF-8). */
    static String percent(String value) {
        StringBuilder out = new StringBuilder();
        for (byte b : value.getBytes(StandardCharsets.UTF_8)) {
            int c = b & 0xFF;
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') ||
                c == '-' || c == '.' || c == '_' || c == '~' || c == '@') out.append((char) c);
            else out.append('%').append(Character.toUpperCase(Character.forDigit(c >> 4, 16)))
                    .append(Character.toUpperCase(Character.forDigit(c & 0xF, 16)));
        }
        return out.toString();
    }

    private static String decode(String value) {
        try { return URLDecoder.decode(value, StandardCharsets.UTF_8); }
        catch (IllegalArgumentException ex) { throw new IllegalArgumentException("Malformed UPI encoding"); }
    }
    private static String encode(String value) {
        return URLEncoder.encode(value,StandardCharsets.UTF_8);
    }
}
