package com.giftplatform.hostpayment.qr;

import com.giftplatform.security.ActorContext;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HostUpiQrControllerTest {
    private final UpiQrImageService service=mock(UpiQrImageService.class);
    private final ActorContext actors=mock(ActorContext.class);
    private final HostUpiQrController controller=new HostUpiQrController(service,actors);
    private final UUID host=UUID.randomUUID(),account=UUID.randomUUID();
    @Test void uploadUsesAuthenticatedHost() {
        when(actors.requireAuthenticatedUserId()).thenReturn(host);
        var file=new MockMultipartFile("file","upi.png","image/png",new byte[]{1,2,3});
        controller.upload(account,file);
        verify(service).upload(host,account,new byte[]{1,2,3});
    }
    @Test void rejectsEmptyUpload() {
        var file=new MockMultipartFile("file",new byte[0]);
        assertEquals(HttpStatus.BAD_REQUEST,assertThrows(ResponseStatusException.class,()->controller.upload(account,file)).getStatusCode());
        verifyNoInteractions(service);
    }
    @Test void rejectsOversizeUpload() {
        var file=new MockMultipartFile("file",new byte[UpiQrImageCodec.MAX_UPLOAD_BYTES+1]);
        assertEquals(HttpStatus.BAD_REQUEST,assertThrows(ResponseStatusException.class,()->controller.upload(account,file)).getStatusCode());
        verifyNoInteractions(service);
    }
    @Test void generatedPngIsNoStore() {
        when(actors.requireAuthenticatedUserId()).thenReturn(host);
        when(service.generate(host,account)).thenReturn(new byte[]{1,2});
        var response=controller.generate(account);
        assertEquals("no-store",response.getHeaders().getFirst("Cache-Control"));
        assertArrayEquals(new byte[]{1,2},response.getBody());
    }
    @Test void generatedQrRejectsUnverifiedAccount() {
        when(actors.requireAuthenticatedUserId()).thenReturn(host);
        when(service.generate(host,account)).thenThrow(new IllegalStateException("not verified"));
        assertEquals(HttpStatus.CONFLICT,assertThrows(ResponseStatusException.class,()->controller.generate(account)).getStatusCode());
    }
    @Test void uploadedQrIsHostScoped() {
        when(actors.requireAuthenticatedUserId()).thenReturn(host);
        when(service.uploaded(host,account)).thenReturn(new byte[]{3});
        assertArrayEquals(new byte[]{3},controller.uploaded(account).getBody());
        verify(service).uploaded(host,account);
    }
}
