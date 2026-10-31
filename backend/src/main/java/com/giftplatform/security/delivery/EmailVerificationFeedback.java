package com.giftplatform.security.delivery;

/** Provider acceptance is not confirmation that a mailbox exists or received the code. */
public final class EmailVerificationFeedback {
    private EmailVerificationFeedback() {}

    public static final String REQUESTED = "Verification code requested. We cannot confirm delivery yet. Check your inbox and spam folder. "
            + "If no code arrives, check for a typo or use an email address you can access. "
            + "Your email remains unverified until you enter the code.";
}
