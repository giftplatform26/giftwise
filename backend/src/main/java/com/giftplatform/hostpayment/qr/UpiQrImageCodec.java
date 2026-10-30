package com.giftplatform.hostpayment.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * QR image codec, deliberately independent of HTTP and storage.
 * A matching QR does not prove UPI ownership or successful payment.
 */
public final class UpiQrImageCodec {
    public static final int MAX_UPLOAD_BYTES = 2_000_000;
    public static final int MAX_IMAGE_SIDE = 3000;
    public static final long MAX_IMAGE_PIXELS = 9_000_000L;
    private static final Set<String> FORMATS = Set.of("png", "jpeg");
    private UpiQrImageCodec() {}

    /** Generate an amount-free UPI QR. Caller MUST authorize verified account ownership. */
    public static byte[] generatePng(String verifiedUpiId, String payeeName) {
        try {
            String payload = UpiQrPayload.generate(verifiedUpiId, payeeName);
            BitMatrix bits = new MultiFormatWriter().encode(
                payload, BarcodeFormat.QR_CODE, 420, 420,
                Map.of(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
                       EncodeHintType.MARGIN, 3));
            BufferedImage image = new BufferedImage(bits.getWidth(), bits.getHeight(), BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < bits.getHeight(); y++)
                for (int x = 0; x < bits.getWidth(); x++)
                    image.setRGB(x, y, bits.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (!ImageIO.write(image, "png", out)) throw new IllegalStateException("PNG writer unavailable");
            return out.toByteArray();
        } catch (com.google.zxing.WriterException | IOException e) {
            throw new IllegalStateException("Unable to generate QR image", e);
        }
    }

    /**
     * Validate uploaded image bytes, decode exactly one UPI QR, and match its payee.
     * Does not persist or publish the image.
     */
    public static UpiQrPayload.Parsed validateUpload(byte[] imageBytes, String registeredUpiId) {
        if (imageBytes == null || imageBytes.length == 0 || imageBytes.length > MAX_UPLOAD_BYTES)
            throw new IllegalArgumentException("Invalid QR image size");
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(imageBytes))) {
            if (input == null) throw new IllegalArgumentException("Invalid image");
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new IllegalArgumentException("Unsupported image format");
            ImageReader reader = readers.next();
            try {
                if (!FORMATS.contains(reader.getFormatName().toLowerCase(java.util.Locale.ROOT)))
                    throw new IllegalArgumentException("Unsupported image format");
                reader.setInput(input, true, true);
                int width = reader.getWidth(0), height = reader.getHeight(0);
                if (width < 80 || height < 80 || width > MAX_IMAGE_SIDE || height > MAX_IMAGE_SIDE ||
                    (long)width * height > MAX_IMAGE_PIXELS)
                    throw new IllegalArgumentException("Invalid QR image dimensions");
                BufferedImage image = reader.read(0);
                if (image == null) throw new IllegalArgumentException("Unreadable image");
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
                Result result = new MultiFormatReader().decode(bitmap,
                    Map.of(DecodeHintType.POSSIBLE_FORMATS, java.util.List.of(BarcodeFormat.QR_CODE),
                           DecodeHintType.TRY_HARDER, Boolean.TRUE));
                String text = result.getText();
                UpiQrPayload.requireMatchingPayee(text, registeredUpiId);
                return UpiQrPayload.parse(text);
            } finally { reader.dispose(); }
        } catch (NotFoundException | IOException | RuntimeException e) {
            if (e instanceof IllegalArgumentException iae) throw iae;
            throw new IllegalArgumentException("Invalid or unreadable UPI QR image");
        }
    }
}
