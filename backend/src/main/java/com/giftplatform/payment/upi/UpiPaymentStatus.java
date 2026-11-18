package com.giftplatform.payment.upi;

import java.util.Map;
import java.util.Set;

/**
 * Unified status model for direct guest -> host payments.
 *
 * <p>Attempt rows use INITIATED / PENDING / FAILED / PAYMENT_REPORTED / EXPIRED. Once reported, the claim row
 * (guest_direct_transfer_claims) carries the status; its stored value AWAITING_VERIFICATION maps to
 * PAYMENT_REPORTED so existing reports, receipts and gift progress keep working unchanged.
 *
 * <p>INDEPENDENTLY_VERIFIED is reserved for evidence from an independent source (for example a bank statement
 * feed). No such source is connected, so no API can reach it today. A UPI app callback, a guest UTR, a screenshot
 * or a host confirmation can never produce it.
 */
public enum UpiPaymentStatus {
    INITIATED, PENDING, FAILED, PAYMENT_REPORTED, HOST_CONFIRMED, INDEPENDENTLY_VERIFIED, REJECTED, EXPIRED;

    public enum Actor { GUEST, HOST, SYSTEM, PLATFORM_VERIFIER }

    private record Rule(Actor actor, Set<UpiPaymentStatus> targets) {}

    private static final Map<UpiPaymentStatus, Set<Rule>> RULES = Map.of(
        INITIATED, Set.of(new Rule(Actor.GUEST, Set.of(PENDING, FAILED, PAYMENT_REPORTED)),
                          new Rule(Actor.SYSTEM, Set.of(EXPIRED))),
        PENDING, Set.of(new Rule(Actor.GUEST, Set.of(FAILED, PAYMENT_REPORTED)),
                        new Rule(Actor.SYSTEM, Set.of(EXPIRED))),
        // A guest who chose "failed" may later find the money did leave their account.
        FAILED, Set.of(new Rule(Actor.GUEST, Set.of(PENDING, PAYMENT_REPORTED)),
                       new Rule(Actor.SYSTEM, Set.of(EXPIRED))),
        PAYMENT_REPORTED, Set.of(new Rule(Actor.HOST, Set.of(HOST_CONFIRMED, REJECTED)),
                                 new Rule(Actor.PLATFORM_VERIFIER, Set.of(INDEPENDENTLY_VERIFIED))),
        // Hosts can undo or change their decision; that is their own statement, not verification.
        HOST_CONFIRMED, Set.of(new Rule(Actor.HOST, Set.of(PAYMENT_REPORTED, REJECTED)),
                               new Rule(Actor.PLATFORM_VERIFIER, Set.of(INDEPENDENTLY_VERIFIED))),
        REJECTED, Set.of(new Rule(Actor.HOST, Set.of(PAYMENT_REPORTED, HOST_CONFIRMED))),
        INDEPENDENTLY_VERIFIED, Set.of(),
        EXPIRED, Set.of());

    public static boolean allowed(UpiPaymentStatus from, UpiPaymentStatus to, Actor actor) {
        if (from == null || to == null || actor == null || from == to) return false;
        return RULES.getOrDefault(from, Set.of()).stream()
            .anyMatch(rule -> rule.actor() == actor && rule.targets().contains(to));
    }

    /** Maps a stored claim/attempt status to the unified model. */
    public static UpiPaymentStatus fromStored(String stored) {
        if (stored == null) return null;
        return "AWAITING_VERIFICATION".equals(stored) ? PAYMENT_REPORTED : valueOf(stored);
    }

    /** The value stored on guest_direct_transfer_claims for a unified claim status. */
    public String claimColumnValue() {
        return switch (this) {
            case PAYMENT_REPORTED -> "AWAITING_VERIFICATION";
            case HOST_CONFIRMED, REJECTED, EXPIRED -> name();
            default -> throw new IllegalArgumentException(name() + " is not a claim status");
        };
    }
}
