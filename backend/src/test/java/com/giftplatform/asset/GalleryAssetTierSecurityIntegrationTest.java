package com.giftplatform.asset;

import com.giftplatform.security.ActorContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class GalleryAssetTierSecurityIntegrationTest {

    @Container
    static PostgreSQLContainer postgres =
        new PostgreSQLContainer("postgres:17");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired MockMvc mvc;
    @MockitoBean ActorContext actor;
    @Autowired JdbcTemplate jdbc;
    @Autowired GalleryAssetRepository repo;

    private UUID createAsset(String tier, boolean published) {
        GalleryAsset asset = repo.saveAndFlush(
            new GalleryAsset(
                "DECORATION",
                "BIRTHDAY",
                "Security Test",
                new byte[]{(byte)137, 80, 78, 71},
                80,
                100
            )
        );

        jdbc.update(
            "UPDATE invitation_gallery_assets SET tier=?, published=? WHERE id=?",
            tier, published, asset.getId()
        );

        return asset.getId();
    }

    private String publicImage(UUID id) {
        return "/api/public/invitation-assets/" + id + "/image";
    }

    private String adminImage(UUID id) {
        return "/api/admin/invitation-assets/" + id + "/image";
    }

    @Test
    void publishedFreeImageIsPublic() throws Exception {
        UUID id = createAsset("FREE", true);

        mvc.perform(get(publicImage(id)))
            .andExpect(status().isOk())
            .andExpect(content().contentType("image/png"));
    }

    @Test
    void unpublishedFreeImageIsNotPublic() throws Exception {
        UUID id = createAsset("FREE", false);

        mvc.perform(get(publicImage(id)))
            .andExpect(status().isNotFound());
    }

    @Test
    void publishedPremiumImageIsNotPublic() throws Exception {
        UUID id = createAsset("PREMIUM", true);

        mvc.perform(get(publicImage(id)))
            .andExpect(status().isNotFound());
    }

    @Test
    void unpublishedPremiumImageIsNotPublic() throws Exception {
        UUID id = createAsset("PREMIUM", false);

        mvc.perform(get(publicImage(id)))
            .andExpect(status().isNotFound());
    }

    @Test
    void unknownAssetIsNotPublic() throws Exception {
        mvc.perform(get(publicImage(UUID.randomUUID())))
            .andExpect(status().isNotFound());
    }

    @Test
    void adminCanAccessPremiumImage() throws Exception {
        when(actor.isPlatformAdmin()).thenReturn(true);
        UUID id = createAsset("PREMIUM", true);

        mvc.perform(
            get(adminImage(id))
                .with(user("admin").roles("PLATFORM_ADMIN"))
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType("image/png"));
    }

    @Test
    void anonymousCannotAccessAdminImage() throws Exception {
        UUID id = createAsset("PREMIUM", true);

        mvc.perform(get(adminImage(id)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void hostCannotAccessAdminImage() throws Exception {
        UUID id = createAsset("PREMIUM", true);

        mvc.perform(
            get(adminImage(id))
                .with(user("host").roles("CUSTOMER"))
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void invalidTierRejectedByDatabase() {
        UUID id = createAsset("FREE", false);

        assertThrows(
            Exception.class,
            () -> jdbc.update(
                "UPDATE invitation_gallery_assets SET tier='INVALID' WHERE id=?",
                id
            )
        );
    }

    @Test
    void migrationV16Applied() {
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM flyway_schema_history " +
            "WHERE version='16' AND success=true",
            Integer.class
        );

        assertEquals(1, count);
    }
}
