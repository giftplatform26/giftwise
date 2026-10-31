package com.giftplatform.security.delivery;

/** Contains only an approved user-facing explanation, never the provider response body. */
public final class EmailDeliveryException extends IllegalStateException {
    public EmailDeliveryException(String message){super(message);}
    static EmailDeliveryException rejected(int status,String body){
        String detail=body==null?"":body.toLowerCase(java.util.Locale.ROOT);
        if((status==400||status==422) && (detail.contains("invalid recipient")
                ||detail.contains("invalid `to`")||detail.contains("invalid to field")
                ||detail.contains("recipient address rejected")||detail.contains("mailbox unavailable")
                ||detail.contains("no such user")))
            return new EmailDeliveryException("The recipient email address was rejected. Check for a typo or use an email address that can receive mail.");
        if(detail.contains("domain")&&detail.contains("verif"))
            return new EmailDeliveryException("The email sender domain is not verified. Verify the sending domain in Resend, then try again.");
        if(status==401||detail.contains("api key")&&detail.contains("invalid"))
            return new EmailDeliveryException("The email provider rejected its API key. Ask support to check email configuration.");
        if(status==429)return new EmailDeliveryException("The email provider is temporarily rate limited. Please try again later.");
        if(status==403)return new EmailDeliveryException("The email provider blocked this sender or recipient. Check the verified sender domain and API key permissions.");
        return new EmailDeliveryException("The email provider could not accept the message. Please try again later.");
    }
}
