package com.giftplatform.whatsapp;

import java.util.List;

/** Sends one template message. Implementations must not log phone numbers or message bodies. */
public interface WhatsAppGateway {
    record Result(boolean sent, String providerMessageId, String errorCode, boolean retryable) {
        static Result ok(String id) { return new Result(true, id, null, false); }
        static Result fail(String code, boolean retry) { return new Result(false, null, code, retry); }
    }
    String name();
    /** True when messages actually leave the server. */
    boolean live();
    Result send(String phoneE164, String kind, List<String> params);
}
