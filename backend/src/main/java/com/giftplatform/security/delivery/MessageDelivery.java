package com.giftplatform.security.delivery;

/** Delivery transport only; it must never decide whether an OTP is valid. */
public interface MessageDelivery {
    void send(String destination, String message);
    default void sendCode(String destination,String code) {
        if(code==null||!code.matches("[0-9]{6}"))throw new IllegalArgumentException("Invalid OTP");
        send(destination,"Your GiftWiser mobile verification code is "+code+". Do not share this code.");
    }
    default boolean establishesMobileOwnership() { return false; }
    /** Local configuration readiness, not proof of delivery. */
    default boolean isAvailable() { return true; }
}
