package com.giftplatform.user.controller;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
class ProfilePreferencesControllerTest {
 @Test void acceptsKnownTimezoneAndExplicitNotificationChoices(){assertThatCode(()->ProfilePreferencesController.validate(new ProfilePreferencesController.Input("Asia/Kolkata",true,false,false))).doesNotThrowAnyException();}
 @Test void rejectsUnknownTimezoneAndMissingChoices(){assertThatThrownBy(()->ProfilePreferencesController.validate(new ProfilePreferencesController.Input("Unknown/Zone",true,true,false))).hasMessageContaining("time zone");assertThatThrownBy(()->ProfilePreferencesController.validate(new ProfilePreferencesController.Input("UTC",null,true,false))).hasMessageContaining("preferences");}
}
