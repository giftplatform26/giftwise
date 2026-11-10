package com.giftplatform.security;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
class CredentialPolicyTest {
 @Test void acceptsRequiredComposition(){ assertThatCode(()->CredentialPolicy.validatePassword("Abcdef1!")).doesNotThrowAnyException(); }
 @Test void rejectsMissingRequirements(){
  for(String value:new String[]{"Ab1!", "abcdef1!", "ABCDEF1!", "Abcdefg!", "Abcdef12", "Aa1!"+"é".repeat(35)})
   assertThatThrownBy(()->CredentialPolicy.validatePassword(value)).isInstanceOf(IllegalArgumentException.class);
 }
 @Test void usernameBoundaries(){
  for(String value:new String[]{"Ab_1","1234","A".repeat(30)}) assertThat(value.matches(CredentialPolicy.USERNAME_PATTERN)).isTrue();
  for(String value:new String[]{"_abc","abc","a".repeat(31),"ab-c","ab c"}) assertThat(value.matches(CredentialPolicy.USERNAME_PATTERN)).isFalse();
 }
}
