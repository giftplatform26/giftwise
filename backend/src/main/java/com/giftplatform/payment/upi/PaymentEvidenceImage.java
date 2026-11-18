package com.giftplatform.payment.upi;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Validates a guest's payment screenshot and re-encodes it as a fresh JPEG: decodes only PNG/JPEG, bounds
 * dimensions before allocating pixels, and drops all metadata (EXIF location, comments, embedded content).
 * A screenshot is supporting evidence for the host, never verification.
 */
public final class PaymentEvidenceImage {
    public static final int MAX_UPLOAD_BYTES = 5_000_000;
    private static final int MAX_SIDE = 6000;
    private static final long MAX_PIXELS = 24_000_000L;
    private static final int OUTPUT_MAX_SIDE = 1600;

    private PaymentEvidenceImage() {}

    public static byte[] sanitize(byte[] bytes) {
        if (bytes == null || bytes.length == 0 || bytes.length > MAX_UPLOAD_BYTES)
            throw new IllegalArgumentException("Choose a PNG or JPEG screenshot up to 5 MB.");
        boolean png = bytes.length > 8 && (bytes[0] & 0xFF) == 0x89 && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G';
        boolean jpeg = bytes.length > 3 && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF;
        if (!png && !jpeg) throw new IllegalArgumentException("Only PNG and JPEG screenshots are supported.");
        try (var stream = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            var readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) throw new IllegalArgumentException("Choose a valid PNG or JPEG screenshot.");
            var reader = readers.next();
            try {
                String format = reader.getFormatName();
                if (!format.equalsIgnoreCase("png") && !format.equalsIgnoreCase("jpeg"))
                    throw new IllegalArgumentException("Only PNG and JPEG screenshots are supported.");
                reader.setInput(stream, true, true);
                int w = reader.getWidth(0), h = reader.getHeight(0);
                if (w < 100 || h < 100 || w > MAX_SIDE || h > MAX_SIDE || (long) w * h > MAX_PIXELS)
                    throw new IllegalArgumentException("Screenshot dimensions are not supported.");
                BufferedImage source = reader.read(0);
                double scale = Math.min(1d, (double) OUTPUT_MAX_SIDE / Math.max(w, h));
                int ow = Math.max(1, (int) Math.round(w * scale)), oh = Math.max(1, (int) Math.round(h * scale));
                BufferedImage output = new BufferedImage(ow, oh, BufferedImage.TYPE_INT_RGB);
                var g = output.createGraphics();
                try {
                    g.setColor(java.awt.Color.WHITE);
                    g.fillRect(0, 0, ow, oh);
                    g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g.drawImage(source, 0, 0, ow, oh, null);
                } finally { g.dispose(); }
                var out = new ByteArrayOutputStream();
                if (!ImageIO.write(output, "jpeg", out)) throw new IllegalStateException("JPEG writer unavailable");
                return out.toByteArray();
            } finally { reader.dispose(); }
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to read this screenshot. Choose a valid PNG or JPEG.");
        }
    }
}
