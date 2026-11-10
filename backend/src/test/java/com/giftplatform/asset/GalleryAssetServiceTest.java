package com.giftplatform.asset;

import com.giftplatform.audit.service.AuditService;
import com.giftplatform.security.ActorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GalleryAssetServiceTest {
    GalleryAssetRepository repo;
    GalleryImageProcessor processor;
    AuditService audit;
    ActorContext actor;
    GalleryAssetService service;

    @BeforeEach void setUp() {
        repo = mock(GalleryAssetRepository.class);
        processor = mock(GalleryImageProcessor.class);
        audit = mock(AuditService.class);
        actor = mock(ActorContext.class);
        service = new GalleryAssetService(repo, processor, audit, actor);
    }

    @Test void previewRequiresAuthenticationBeforeReadingAsset() {
        when(actor.requireAuthenticatedUserId()).thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        assertThrows(ResponseStatusException.class,()->service.preview(UUID.randomUUID()));
        verifyNoInteractions(repo);
    }

    @Test void unpublishedPreviewIsNotAvailable() {
        GalleryAsset draft=asset();when(actor.requireAuthenticatedUserId()).thenReturn(UUID.randomUUID());
        when(repo.findById(draft.getId())).thenReturn(Optional.of(draft));
        assertEquals(HttpStatus.NOT_FOUND,assertThrows(ResponseStatusException.class,()->service.preview(draft.getId())).getStatusCode());
    }

    @Test void unpaidHostCanPreviewButCannotFetchCleanPremiumImage() throws Exception {
        var commerce=mock(com.giftplatform.monetization.CommercialAccessService.class);
        org.springframework.test.util.ReflectionTestUtils.setField(service,"commerce",commerce);
        UUID host=UUID.randomUUID();when(actor.requireAuthenticatedUserId()).thenReturn(host);
        GalleryAsset premium=new GalleryAsset("BACKGROUND","WEDDING","Premium",db.migration.V98__premium_invitation_collection.artwork(0),600,800);
        premium.setPublished(true);premium.setTier("PREMIUM");
        when(repo.findById(premium.getId())).thenReturn(Optional.of(premium));
        byte[] preview=service.preview(premium.getId());assertNotNull(javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(preview)));
        assertFalse(java.util.Arrays.equals(preview,premium.getImagePng()));
        assertEquals(HttpStatus.PAYMENT_REQUIRED,assertThrows(ResponseStatusException.class,()->service.purchasedImage(premium.getId())).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND,assertThrows(ResponseStatusException.class,()->service.publishedImage(premium.getId())).getStatusCode());
        when(commerce.template(host,premium.getId())).thenReturn(true);assertSame(premium,service.purchasedImage(premium.getId()));
    }

    @Test void anonymousCanListOnlyPublishedAssets() {
        GalleryAsset published = asset();
        published.setPublished(true);
        when(repo.findByPublishedTrueOrderByTitleAsc()).thenReturn(List.of(published));
        assertEquals(1, service.published().size());
        assertTrue(service.published().get(0).published());
        verify(repo, never()).findAll();
        verifyNoInteractions(actor);
    }

    @Test void draftImageReturnsNotFound() {
        GalleryAsset draft = asset();
        when(repo.findById(draft.getId())).thenReturn(Optional.of(draft));
        assertEquals(HttpStatus.NOT_FOUND, assertThrows(
            ResponseStatusException.class,
            () -> service.publishedImage(draft.getId())
        ).getStatusCode());
    }

    @Test void publishedImageIsAccessible() {
        GalleryAsset published = asset();
        published.setPublished(true);
        when(repo.findById(published.getId())).thenReturn(Optional.of(published));
        assertSame(published, service.publishedImage(published.getId()));
    }

    @Test void missingImageReturnsNotFound() {
        UUID id = UUID.randomUUID();
        assertEquals(HttpStatus.NOT_FOUND, assertThrows(
            ResponseStatusException.class, () -> service.publishedImage(id)
        ).getStatusCode());
    }

    @Test void nonAdminCannotListAll() {
        assertForbidden(() -> service.all());
        verifyNoInteractions(repo);
    }

    @Test void nonAdminCannotReadDraftImage() {
        assertForbidden(() -> service.adminImage(UUID.randomUUID()));
        verifyNoInteractions(repo);
    }

    @Test void nonAdminCannotUploadOrPublish() {
        assertForbidden(() -> service.upload("BACKGROUND", "BIRTHDAY", "Test", new byte[]{1}));
        assertForbidden(() -> service.publish(UUID.randomUUID(), true));
        verifyNoInteractions(repo, processor, audit);
    }

    @Test void adminUploadRejectsInvalidMetadataBeforeImageProcessing() {
        when(actor.isPlatformAdmin()).thenReturn(true);
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(
            ResponseStatusException.class,
            () -> service.upload("OTHER", "BIRTHDAY", "Test", new byte[]{1})
        ).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(
            ResponseStatusException.class,
            () -> service.upload("BACKGROUND", "../bad", "Test", new byte[]{1})
        ).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(
            ResponseStatusException.class,
            () -> service.upload("BACKGROUND", "BIRTHDAY", " ", new byte[]{1})
        ).getStatusCode());
        verifyNoInteractions(repo, processor, audit);
    }

    @Test void adminCanListAllAndReadDraft() {
        when(actor.isPlatformAdmin()).thenReturn(true);
        GalleryAsset draft = asset();
        when(repo.findAll()).thenReturn(List.of(draft));
        when(repo.findById(draft.getId())).thenReturn(Optional.of(draft));
        assertEquals(1, service.all().size());
        assertSame(draft, service.adminImage(draft.getId()));
    }

    private GalleryAsset asset() {
        return new GalleryAsset("BACKGROUND", "BIRTHDAY", "Test",
            new byte[]{(byte)137, 80, 78, 71}, 80, 100);
    }

    private void assertForbidden(org.junit.jupiter.api.function.Executable call) {
        assertEquals(HttpStatus.FORBIDDEN,
            assertThrows(ResponseStatusException.class, call).getStatusCode());
    }
}
