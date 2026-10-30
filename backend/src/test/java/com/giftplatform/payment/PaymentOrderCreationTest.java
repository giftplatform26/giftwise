package com.giftplatform.payment;

import com.giftplatform.payment.entity.PaymentOrder;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PaymentOrderCreationTest {

    @Test
    void createsValidWalletOrder() {
        PaymentOrder order = new PaymentOrder(
                UUID.randomUUID(), null,
                "EVENT_WALLET", 200000, "wallet-key");

        assertEquals("CREATED", order.getStatus());
        assertEquals("RAZORPAY", order.getProvider());
        assertEquals("INR", order.getCurrency());
        assertEquals(200000L, order.getAmountPaise());
        assertNull(order.getGiftId());
    }

    @Test
    void createsValidPartialGiftOrder() {
        UUID gift = UUID.randomUUID();

        PaymentOrder order = new PaymentOrder(
                UUID.randomUUID(), gift,
                "GIFT_PARTIAL", 50000, "partial-key");

        assertEquals(gift, order.getGiftId());
        assertEquals("GIFT_PARTIAL", order.getContributionType());
    }

    @Test
    void createsValidFullGiftOrder() {
        PaymentOrder order = new PaymentOrder(
                UUID.randomUUID(), UUID.randomUUID(),
                "GIFT_FULL", 1000000, "full-key");

        assertEquals("GIFT_FULL", order.getContributionType());
    }

    @Test
    void rejectsZeroAmount() {
        assertThrows(IllegalArgumentException.class, () ->
                new PaymentOrder(UUID.randomUUID(), null,
                        "EVENT_WALLET", 0, "key"));
    }

    @Test
    void rejectsNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () ->
                new PaymentOrder(UUID.randomUUID(), null,
                        "EVENT_WALLET", -1, "key"));
    }

    @Test
    void rejectsInvalidContributionType() {
        assertThrows(IllegalArgumentException.class, () ->
                new PaymentOrder(UUID.randomUUID(), null,
                        "INVALID", 100, "key"));
    }

    @Test
    void rejectsGiftPaymentWithoutGift() {
        assertThrows(IllegalArgumentException.class, () ->
                new PaymentOrder(UUID.randomUUID(), null,
                        "GIFT_PARTIAL", 100, "key"));
    }

    @Test
    void rejectsWalletPaymentWithGift() {
        assertThrows(IllegalArgumentException.class, () ->
                new PaymentOrder(UUID.randomUUID(), UUID.randomUUID(),
                        "EVENT_WALLET", 100, "key"));
    }

    @Test
    void rejectsBlankIdempotencyKey() {
        assertThrows(IllegalArgumentException.class, () ->
                new PaymentOrder(UUID.randomUUID(), null,
                        "EVENT_WALLET", 100, " "));
    }

    @Test
    void rejectsMissingEvent() {
        assertThrows(NullPointerException.class, () ->
                new PaymentOrder(null, null,
                        "EVENT_WALLET", 100, "key"));
    }
}
