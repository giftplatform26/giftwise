package com.giftplatform.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class QrRenderer {
    public byte[] png(String url) {
        if (url == null || url.isBlank() || url.length() > 2048) throw new IllegalArgumentException("Invalid QR URL");
        try {
            var matrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, 512, 512,
                    Map.of(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H,
                           EncodeHintType.MARGIN, 4));
            var out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return out.toByteArray();
        } catch (WriterException | IOException ex) {
            throw new IllegalStateException("Could not render QR", ex);
        }
    }
}
