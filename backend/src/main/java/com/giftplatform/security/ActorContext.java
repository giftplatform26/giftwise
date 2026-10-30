package com.giftplatform.security;

import java.util.UUID;

public interface ActorContext {

    UUID requireAuthenticatedUserId();

    boolean isPlatformAdmin();
}