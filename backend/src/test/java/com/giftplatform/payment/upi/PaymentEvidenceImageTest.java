package com.giftplatform.payment.upi;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class PaymentEvidenceImageTest {
    static byte[] image(String format, int w, int h) throws Exception {
        var out = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB), format, out));
        return out.toByteArray();
    }

    @Test void pngAndJpegScreenshotsAreReEncodedAsBoundedJpeg() throws Exception {
        for (String format : new String[]{"png", "jpeg"}) {
            byte[] clean = PaymentEvidenceImage.sanitize(image(format, 1080, 2400));
            assertEquals((byte) 0xFF, clean[0]); assertEquals((byte) 0xD8, clean[1]);
            BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(clean));
            assertEquals(1600, decoded.getHeight()); assertEquals(720, decoded.getWidth());
        }
    }

    @Test void appendedPayloadsAndMetadataDoNotSurviveReEncoding() throws Exception {
        byte[] original = image("png", 400, 800);
        byte[] marker = "<script>alert(1)</script>".getBytes(StandardCharsets.UTF_8);
        byte[] polyglot = new byte[original.length + marker.length];
        System.arraycopy(original, 0, polyglot, 0, original.length);
        System.arraycopy(marker, 0, polyglot, original.length, marker.length);
        String clean = new String(PaymentEvidenceImage.sanitize(polyglot), StandardCharsets.ISO_8859_1);
        assertFalse(clean.contains("<script>"));
    }

    @Test void rejectsNonImagesOtherFormatsTinyAndOversizedFiles() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> PaymentEvidenceImage.sanitize(null));
        assertThrows(IllegalArgumentException.class, () -> PaymentEvidenceImage.sanitize(new byte[0]));
        assertThrows(IllegalArgumentException.class, () -> PaymentEvidenceImage.sanitize("<svg onload=alert(1)>".getBytes(StandardCharsets.UTF_8)));
        assertThrows(IllegalArgumentException.class, () -> PaymentEvidenceImage.sanitize(image("gif", 300, 300)));
        assertThrows(IllegalArgumentException.class, () -> PaymentEvidenceImage.sanitize(image("bmp", 300, 300)));
        assertThrows(IllegalArgumentException.class, () -> PaymentEvidenceImage.sanitize(image("png", 50, 50)));
        assertThrows(IllegalArgumentException.class, () -> PaymentEvidenceImage.sanitize(new byte[PaymentEvidenceImage.MAX_UPLOAD_BYTES + 1]));
        byte[] fakePng = new byte[64]; fakePng[0] = (byte) 0x89; fakePng[1] = 'P'; fakePng[2] = 'N'; fakePng[3] = 'G';
        assertThrows(IllegalArgumentException.class, () -> PaymentEvidenceImage.sanitize(fakePng));
    }
}
