package com.giftplatform.security.delivery;

/** Delivery transport only; it must never decide whether an OTP is valid. */
public interface MessageDelivery {
    void send(String destination, String message);
    /** Local configuration readiness, not proof of delivery. */
    default boolean isAvailable() { return true; }
}
