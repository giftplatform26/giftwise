package com.giftplatform.qr;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class QrRendererTest {
    private final QrRenderer renderer = new QrRenderer();
    @Test void qrRoundTripsLongPublicLink() throws Exception {
        String url = "https://example.test/e/" + java.util.UUID.randomUUID();
        byte[] png = renderer.png(url);
        assertTrue(png.length > 100);
        var image = ImageIO.read(new ByteArrayInputStream(png));
        assertEquals(512, image.getWidth());
        var bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
        assertEquals(url, new MultiFormatReader().decode(bitmap).getText());
    }
    @Test void rejectsBlankUrl() { assertThrows(IllegalArgumentException.class, () -> renderer.png(" ")); }
    @Test void rejectsOverlongUrl() { assertThrows(IllegalArgumentException.class, () -> renderer.png("x".repeat(2049))); }
}
