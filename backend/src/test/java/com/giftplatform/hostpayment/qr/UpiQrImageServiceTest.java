package com.giftplatform.hostpayment.qr;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UpiQrImageServiceTest {
    private final UUID host = UUID.randomUUID(), accountId = UUID.randomUUID();
    private final RegisteredUpiResolver resolver = mock(RegisteredUpiResolver.class);
    private final UpiQrImageStorage storage = mock(UpiQrImageStorage.class);
    private final UpiQrImageService service = new UpiQrImageService(resolver, storage);

    private void account(boolean verified) {
        when(resolver.resolve(host, accountId))
            .thenReturn(new RegisteredUpiResolver.ResolvedUpi("host@okbank", "Example Host", verified));
    }
    @Test void verifiedAccountGeneratesReadableQr() {
        account(true);
        byte[] png = service.generate(host, accountId);
        assertEquals("host@okbank", UpiQrImageCodec.validateUpload(png, "host@okbank").payeeUpiId());
        verify(storage).saveGenerated(eq(host), eq(accountId), matches("[0-9a-f]{64}"));
    }
    @Test void pendingAccountCanGenerateMatchingQr() {
        account(false);
        byte[] png = service.generate(host, accountId);

        assertNotNull(png);
        assertEquals(
            "host@okbank",
            UpiQrImageCodec.validateUpload(png, "host@okbank").payeeUpiId()
        );

        verify(storage).saveGenerated(
            eq(host),
            eq(accountId),
            matches("[0-9a-f]{64}")
        );
    }
    @Test void validUploadedQrIsCanonicalized() {
        account(false);
        byte[] uploaded = UpiQrImageCodec.generatePng("host@okbank", "Test Host");
        service.upload(host, accountId, uploaded);
        verify(storage).saveUploaded(eq(host), eq(accountId), any(byte[].class), matches("[0-9a-f]{64}"));
    }
    @Test void mismatchedUploadedQrIsRejected() {
        account(true);
        byte[] uploaded = UpiQrImageCodec.generatePng("other@okbank", "Other Host");
        assertThrows(IllegalArgumentException.class, () -> service.upload(host, accountId, uploaded));
        verify(storage).hasUploaded(host, accountId);
        verify(storage, never()).saveUploaded(
            any(), any(), any(byte[].class), anyString());
        verify(storage, never()).saveGenerated(
            any(), any(), anyString());
        verifyNoMoreInteractions(storage);
    }
    @Test void malformedImageIsRejected() {
        account(true);
        assertThrows(IllegalArgumentException.class, () -> service.upload(host, accountId, new byte[] {1,2,3}));
        verify(storage).hasUploaded(host, accountId);
        verify(storage, never()).saveUploaded(
            any(), any(), any(byte[].class), anyString());
        verify(storage, never()).saveGenerated(
            any(), any(), anyString());
        verifyNoMoreInteractions(storage);
    }
    @Test void unknownAccountIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.generate(host, accountId));
        verifyNoInteractions(storage);
    }
    @Test void nullHostRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.generate(null, accountId));
        verifyNoInteractions(resolver, storage);
    }
    @Test void retrievalIsHostScoped() {
        account(true);
        byte[] image = {1,2,3};
        when(storage.getUploaded(host, accountId)).thenReturn(image);
        assertArrayEquals(image, service.uploaded(host, accountId));
        verify(storage).getUploaded(host, accountId);
    }
}
