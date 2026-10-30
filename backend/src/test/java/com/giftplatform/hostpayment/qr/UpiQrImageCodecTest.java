package com.giftplatform.hostpayment.qr;

import org.junit.jupiter.api.Test;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

class UpiQrImageCodecTest {
    @Test void generatesReadablePng() throws Exception {
        byte[] png=UpiQrImageCodec.generatePng("host@okbank","Example Host");
        assertTrue(png.length>500);
        assertEquals("host@okbank",UpiQrImageCodec.validateUpload(png,"host@okbank").payeeUpiId());
    }
    @Test void generatedPngDoesNotSetAmount() {
        byte[] png=UpiQrImageCodec.generatePng("host@okbank","Example Host");
        assertFalse(UpiQrImageCodec.validateUpload(png,"host@okbank").hasAmount());
    }
    @Test void rejectsMismatchedPayee() {
        byte[] png=UpiQrImageCodec.generatePng("other@okbank","Other");
        assertThrows(IllegalArgumentException.class,()->UpiQrImageCodec.validateUpload(png,"host@okbank"));
    }
    @Test void rejectsNull() {
        assertThrows(IllegalArgumentException.class,()->UpiQrImageCodec.validateUpload(null,"host@okbank"));
    }
    @Test void rejectsEmpty() {
        assertThrows(IllegalArgumentException.class,()->UpiQrImageCodec.validateUpload(new byte[0],"host@okbank"));
    }
    @Test void rejectsOversized() {
        assertThrows(IllegalArgumentException.class,()->UpiQrImageCodec.validateUpload(new byte[UpiQrImageCodec.MAX_UPLOAD_BYTES+1],"host@okbank"));
    }
    @Test void rejectsArbitraryBytes() {
        assertThrows(IllegalArgumentException.class,()->UpiQrImageCodec.validateUpload("not a QR".getBytes(),"host@okbank"));
    }
    @Test void rejectsPlainImageWithoutQr() throws Exception {
        BufferedImage img=new BufferedImage(200,200,BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        ImageIO.write(img,"png",out);
        assertThrows(IllegalArgumentException.class,()->UpiQrImageCodec.validateUpload(out.toByteArray(),"host@okbank"));
    }
    @Test void rejectsInvalidPayeeOnGeneration() {
        assertThrows(IllegalArgumentException.class,()->UpiQrImageCodec.generatePng("invalid","Example Host"));
    }
    @Test void rejectsInvalidNameOnGeneration() {
        assertThrows(IllegalArgumentException.class,()->UpiQrImageCodec.generatePng("host@okbank",""));
    }
    @Test void rejectsTruncatedPng() {
        byte[] png=UpiQrImageCodec.generatePng("host@okbank","Host");
        assertThrows(IllegalArgumentException.class,()->UpiQrImageCodec.validateUpload(Arrays.copyOf(png,30),"host@okbank"));
    }
}
