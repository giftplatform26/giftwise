package com.giftplatform.security;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import static org.assertj.core.api.Assertions.*;
class HostSessionActivityTest {
 @Test void backgroundPollingDoesNotKeepIdleSessionAlive(){var session=new MockHttpSession();session.setMaxInactiveInterval(60);assertThat(HostSessionActivity.expired(session,1000,false)).isFalse();assertThat(HostSessionActivity.expired(session,31000,false)).isFalse();assertThat(HostSessionActivity.expired(session,61000,false)).isTrue();}
 @Test void userInteractionExtendsDeadlineButCannotReviveExpiredSession(){var session=new MockHttpSession();session.setMaxInactiveInterval(60);HostSessionActivity.expired(session,1000,false);assertThat(HostSessionActivity.expired(session,31000,true)).isFalse();assertThat(HostSessionActivity.expired(session,61000,false)).isFalse();assertThat(HostSessionActivity.expired(session,91000,true)).isTrue();}
}
