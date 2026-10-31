package com.giftplatform.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import org.springframework.stereotype.Component;

@Component
public class QrRenderer {
    public byte[] png(String url) {
        return png(url, 512);
    }

    public byte[] png(String url, int size) {
        if (url == null || url.isBlank() || url.length() > 2048) throw new IllegalArgumentException("Invalid QR URL");
        try {
            var hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 4);
            // Some payloads make the default mask unreadable to the detector.
            // Keep the default when it works, then try the eight standard masks.
            for (int mask = -1; mask < 8; mask++) {
                if (mask >= 0) hints.put(EncodeHintType.QR_MASK_PATTERN, mask);
                var matrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, size, size, hints);
                var image = MatrixToImageWriter.toBufferedImage(matrix);
                try {
                    var decoded = new MultiFormatReader().decode(new BinaryBitmap(
                            new HybridBinarizer(new BufferedImageLuminanceSource(image))));
                    if (!url.equals(decoded.getText())) continue;
                } catch (ReaderException ex) {
                    continue;
                }
                var out = new ByteArrayOutputStream();
                javax.imageio.ImageIO.write(image, "PNG", out);
                return out.toByteArray();
            }
            throw new IllegalStateException("Could not produce scannable QR");
        } catch (WriterException | IOException ex) {
            throw new IllegalStateException("Could not render QR", ex);
        }
    }
}
