package com.giftplatform.hostpayment.verification.account;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

class AccountVerificationClientTest {
    private final ObjectMapper mapper=new ObjectMapper();
    private AccountVerificationClient.Decision bank(String status,String match,String name,boolean sandbox){
        return AccountVerificationClient.interpret(mapper.readTree("{\"reference_id\":123,\"account_status\":\""+status+"\",\"name_match_result\":\""+match+"\",\"name_at_bank\":\""+name+"\"}"),"BANK","request",null,"Jane Doe",sandbox);
    }
    @Test void sandboxNeverProducesProductionVerification(){assertEquals("SANDBOX_VALID",bank("VALID","DIRECT_MATCH","JANE DOE",true).outcome());}
    @Test void productionRequiresValidAccountAndMatchingName(){
        assertEquals("VERIFIED",bank("VALID","DIRECT_MATCH","JANE DOE",false).outcome());
        assertEquals("NAME_MISMATCH",bank("VALID","GOOD_PARTIAL_MATCH","JANE DOE",false).outcome());
        assertEquals("NAME_MISMATCH",bank("VALID","DIRECT_MATCH","John Doe",false).outcome());
        assertEquals("REJECTED",bank("INVALID","DIRECT_MATCH","JANE DOE",false).outcome());
        assertEquals("INCONCLUSIVE",bank("UNABLE_TO_VALIDATE","DIRECT_MATCH","JANE DOE",false).outcome());
    }
    @Test void upiRequiresTheSameRequestAndDestination(){
        var json=mapper.readTree("{\"reference_id\":42,\"verification_id\":\"request\",\"status\":\"VALID\",\"name_at_bank\":\"Jane Doe\",\"vpa\":\"jane@bank\"}");
        assertEquals("VERIFIED",AccountVerificationClient.interpret(json,"UPI","request","jane@bank","Jane Doe",false).outcome());
        assertEquals("INCONCLUSIVE",AccountVerificationClient.interpret(json,"UPI","other","jane@bank","Jane Doe",false).outcome());
        assertEquals("INCONCLUSIVE",AccountVerificationClient.interpret(json,"UPI","request","someone@bank","Jane Doe",false).outcome());
    }
    @Test void missingProviderReferenceIsNeverVerified(){
        var json=mapper.readTree("{\"account_status\":\"VALID\",\"name_match_result\":\"DIRECT_MATCH\",\"name_at_bank\":\"Jane Doe\"}");
        assertEquals("INCONCLUSIVE",AccountVerificationClient.interpret(json,"BANK","request",null,"Jane Doe",false).outcome());
    }
    @Test void missingCredentialsAreUnavailableAndInvalidEnvironmentIsRejected(){
        var client=new AccountVerificationClient(mapper,true,"SANDBOX","","");assertFalse(client.available());
        assertThrows(IllegalStateException.class,()->client.verify("BANK",java.util.Map.of()));
        assertThrows(IllegalArgumentException.class,()->new AccountVerificationClient(mapper,true,"UNKNOWN","id","secret"));
    }
}
