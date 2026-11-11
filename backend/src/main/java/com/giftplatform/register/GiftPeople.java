package com.giftplatform.register;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;

/** Shared validation for in-person gift records (shagun register and personal ledger). */
public final class GiftPeople {
    public static final Set<String> KINDS = Set.of("CASH", "ENVELOPE", "UPI", "GIFT_ITEM", "OTHER");
    public static final long MAX_PAISE = 100_000_000_000L;
    private GiftPeople() {}

    /** Matching key: case, accents, punctuation and extra spaces ignored, so "Ravi  Kumar." and "ravi kumar" match. */
    public static String key(String name) {
        String n = Normalizer.normalize(name == null ? "" : name, Normalizer.Form.NFKD).replaceAll("\\p{M}", "");
        n = n.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N} ]", " ").replaceAll("\\s+", " ").strip();
        return n.length() > 120 ? n.substring(0, 120) : n;
    }

    public static String name(String value, String label) {
        String v = text(value, 120, label);
        if (v == null) throw bad("Add " + label.toLowerCase(Locale.ROOT));
        if (key(v).isEmpty()) throw bad("Enter a real " + label.toLowerCase(Locale.ROOT));
        return v;
    }

    /** Keeps an optional leading + and digits; spaces, dashes and brackets are dropped. */
    public static String phone(String value) {
        if (value == null || value.isBlank()) return null;
        String v = value.strip();
        String digits = (v.startsWith("+") ? "+" : "") + v.replaceAll("[\\s()\\-.]", "").replace("+", "");
        if (!digits.matches("\\+?[0-9]{7,15}")) throw bad("Enter a valid phone number, or leave it empty");
        return digits;
    }

    public static String kind(String value) {
        String k = value == null ? "" : value.strip().toUpperCase(Locale.ROOT);
        if (!KINDS.contains(k)) throw bad("Choose what was given");
        return k;
    }

    public static Long amount(Long paise, String item) {
        if (paise == null) {
            if (item == null) throw bad("Add an amount or describe the gift");
            return null;
        }
        if (paise < 100 || paise > MAX_PAISE) throw bad("Amount must be between ₹1 and ₹1,00,00,00,000");
        return paise;
    }

    public static String text(String value, int max, String label) {
        if (value == null || value.isBlank()) return null;
        String v = value.strip();
        if (v.length() > max) throw bad(label + " must be at most " + max + " characters");
        if (v.chars().anyMatch(c -> Character.isISOControl(c) && c != '\n')) throw bad(label + " contains unsupported characters");
        return v;
    }

    /** CSV cell safe against spreadsheet formula injection. */
    public static String csv(Object value) {
        String s = value == null ? "" : value.toString();
        if (!s.isEmpty() && "=+-@\t\r".indexOf(s.charAt(0)) >= 0) s = "'" + s;
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    public static ResponseStatusException bad(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, m); }
    public static ResponseStatusException conflict() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "This entry changed in another tab. Reload and try again.");
    }
}
