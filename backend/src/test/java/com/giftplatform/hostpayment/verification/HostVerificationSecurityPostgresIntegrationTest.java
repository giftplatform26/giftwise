package com.giftplatform.hostpayment.verification;

import com.giftplatform.security.ActorContext;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Real Spring Security filters + Flyway/Postgres. ActorContext is mocked only to select
 * the host identity; access-control and CSRF are enforced by the actual filter chain. */
@SpringBootTest(properties={
    "gift.payment.profiles.enabled=true",
    "gift.payment.destination-verification.enabled=false",
    "gift.payment.settlement-ui.enabled=false"
})
@AutoConfigureMockMvc
class HostVerificationSecurityPostgresIntegrationTest {
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");
    static { postgres.start(); }
    private static byte[] randomTestKeyBytes() {
        byte[] bytes = new byte[32];
        new java.security.SecureRandom().nextBytes(bytes);
        return bytes;
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
        // PaymentDataProtector requires two distinct 32-byte keys if profiles are enabled.
        r.add("gift.payment.encryption-key-base64", () -> Base64.getEncoder().encodeToString(randomTestKeyBytes()));
        r.add("gift.payment.fingerprint-key-base64", () -> Base64.getEncoder().encodeToString(randomTestKeyBytes()));
    }
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper mapper;
    @MockitoBean ActorContext actor;
    UUID hostA, hostB, upi, bank;
    String base = "/api/v1/host/payment-profiles/verification/";

    @BeforeEach void seed() {
        hostA=UUID.randomUUID();hostB=UUID.randomUUID();upi=UUID.randomUUID();bank=UUID.randomUUID();
        insertFixture("users", hostA, Map.of("email","hosta-"+hostA+"@example.test","username","hosta"+hostA.toString().replace("-", "").substring(0,12),"role","CUSTOMER"));
        insertFixture("users", hostB, Map.of("email","hostb-"+hostB+"@example.test","username","hostb"+hostB.toString().replace("-", "").substring(0,12),"role","CUSTOMER"));
        insertFixture("host_upi_accounts", upi, Map.of("host_id",hostA,"verification_status","PENDING"));
        insertFixture("host_bank_accounts", bank, Map.of("host_id",hostB,"verification_status","PENDING"));
        when(actor.requireAuthenticatedUserId()).thenReturn(hostA);
    }
    @AfterEach void clean() {
        if(hostA!=null) {
            jdbc.update("DELETE FROM host_destination_verification_attempts WHERE host_id IN (?,?)",hostA,hostB);
            jdbc.update("DELETE FROM host_upi_accounts WHERE id=?",upi);
            jdbc.update("DELETE FROM host_bank_accounts WHERE id=?",bank);
            jdbc.update("DELETE FROM users WHERE id IN (?,?)",hostA,hostB);
        }
    }
    // Fixture creation uses the actual migrated schema. No production tables are altered.
    // Required columns are populated with safe dummy values; the test fails loudly if a
    // new schema requirement cannot be represented rather than bypassing constraints.
    void insertFixture(String table,UUID id,Map<String,Object> explicit) {
        if(!Set.of("users","host_upi_accounts","host_bank_accounts").contains(table)) throw new IllegalArgumentException();
        var cols=jdbc.query("""
            SELECT column_name,data_type,udt_name,is_nullable,column_default,is_identity
            FROM information_schema.columns WHERE table_schema='public' AND table_name=?
            ORDER BY ordinal_position
            """,(rs,n)->new String[]{rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getString(6)},table);
        LinkedHashMap<String,Object> values=new LinkedHashMap<>();
        for(var c:cols){
            String name=c[0], type=c[1], udt=c[2];
            if(name.equals("id")){values.put(name,id);continue;}
            if(explicit.containsKey(name)){values.put(name,explicit.get(name));continue;}
            if(c[3].equals("YES") || c[4]!=null || "YES".equals(c[5]))continue;
            Object v;
            if(name.equals("host_id")) v=hostA;
            else if(name.equals("owner_id")) v=hostA;
            else if(name.equals("enabled") || name.equals("is_active"))v=true;
            else if(name.equals("archived"))v=false;
            else if(name.equals("role"))v="CUSTOMER";
            else if(name.equals("verification_status") || name.equals("status"))v="PENDING";
            else if(name.equals("email"))v="fixture-"+id+"@example.test";
            else if(name.equals("username"))v="fixture"+id.toString().replace("-", "").substring(0,12);
            else if(name.equals("password_hash"))v="!disabled-fixture!";
            else if(name.contains("fingerprint") || name.contains("hash"))v="fixture-"+id;
            else if(name.contains("masked"))v="****0000";
            else if(name.contains("encrypted"))v="fixture-ciphertext";
            else if(name.contains("ifsc"))v="ABCD0000000";
            else if(name.contains("account_holder"))v="Fixture Holder";
            else if(name.contains("upi"))v="fixture@bank";
            else if(name.contains("phone"))v="9000000000";
            else if(type.equals("boolean"))v=false;
            else if(type.equals("uuid"))v=UUID.randomUUID();
            else if(type.contains("timestamp"))v=java.sql.Timestamp.from(java.time.Instant.now());
            else if(type.equals("integer")||type.equals("bigint"))v=0;
            else if(type.equals("numeric"))v=java.math.BigDecimal.ZERO;
            else if(type.equals("character varying")||type.equals("text")||type.equals("character"))v="fixture";
            else throw new IllegalStateException("Unsupported required fixture column "+table+"."+name+" ("+udt+")");
            values.put(name,v);
        }
        String names=String.join(",",values.keySet());
        String marks=String.join(",",Collections.nCopies(values.size(),"?"));
        jdbc.update("INSERT INTO "+table+" ("+names+") VALUES ("+marks+")",values.values().toArray());
    }
    RequestPostProcessor host() { return user("fixture-host").roles("CUSTOMER"); }
    String url(String kind,UUID dest) {return base+kind+"/"+dest;}
    long attempts() {return jdbc.queryForObject("SELECT COUNT(*) FROM host_destination_verification_attempts WHERE host_id=?",Long.class,hostA);}

    @Test void anonymousCannotReadOrRequest() throws Exception {
        mvc.perform(get(url("upi",upi)+"/attempts")).andExpect(status().isUnauthorized());
        mvc.perform(post(url("upi",upi)+"/request").with(csrf())).andExpect(status().isUnauthorized());
        assertEquals(0,attempts());
    }
    @Test void csrfRequiredAndInvalidRejected() throws Exception {
        mvc.perform(post(url("upi",upi)+"/request").with(host())).andExpect(status().isForbidden());
        mvc.perform(post(url("upi",upi)+"/request").with(host()).with(csrf().useInvalidToken())).andExpect(status().isForbidden());
        assertEquals(0,attempts());
    }
    @Test void upiUnavailablePersistedAndRepeatIsIdempotent() throws Exception {
        String path=url("upi",upi)+"/request";
        MvcResult first=mvc.perform(post(path).with(host()).with(csrf())).andExpect(status().isCreated())
          .andExpect(jsonPath("$.state").value("UNAVAILABLE")).andReturn();
        String id=mapper.readTree(first.getResponse().getContentAsString()).get("attemptId").asText();
        mvc.perform(post(path).with(host()).with(csrf())).andExpect(status().isOk())
          .andExpect(jsonPath("$.attemptId").value(id));
        mvc.perform(get(url("upi",upi)+"/attempts").with(host())).andExpect(status().isOk())
          .andExpect(jsonPath("$[0].state").value("UNAVAILABLE"));
        assertEquals(1,attempts());
        assertEquals("PENDING",jdbc.queryForObject("SELECT verification_status FROM host_upi_accounts WHERE id=?",String.class,upi));
    }
    @Test void bankOwnershipAndHistoryIsolation() throws Exception {
        mvc.perform(post(url("bank",bank)+"/request").with(host()).with(csrf())).andExpect(status().isNotFound());
        mvc.perform(get(url("bank",bank)+"/attempts").with(host())).andExpect(status().isNotFound());
        when(actor.requireAuthenticatedUserId()).thenReturn(hostB);
        mvc.perform(post(url("bank",bank)+"/request").with(host()).with(csrf())).andExpect(status().isCreated());
        mvc.perform(get(url("bank",bank)+"/attempts").with(host())).andExpect(status().isOk())
          .andExpect(jsonPath("$[0].state").value("UNAVAILABLE"));
        assertEquals("PENDING",jdbc.queryForObject("SELECT verification_status FROM host_bank_accounts WHERE id=?",String.class,bank));
    }
    @Test void invalidKindPaginationAndUnknownDestination() throws Exception {
        mvc.perform(get(url("invalid",upi)+"/attempts").with(host())).andExpect(status().isBadRequest());
        mvc.perform(get(url("upi",upi)+"/attempts?limit=101").with(host())).andExpect(status().isBadRequest());
        mvc.perform(post(url("upi",UUID.randomUUID())+"/request").with(host()).with(csrf())).andExpect(status().isNotFound());
        assertEquals(0,attempts());
    }
}
