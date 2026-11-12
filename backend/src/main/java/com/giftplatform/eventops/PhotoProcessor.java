package com.giftplatform.eventops;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Turns a guest's photo (JPEG, PNG, WebP, HEIC/HEIF) into a clean JPEG: rotated upright, location and camera data
 * stripped, resized. Uses ImageMagick with memory/time limits, like guestbook photos.
 */
final class PhotoProcessor {
    private PhotoProcessor() {}

    static String signature(byte[] b) {
        if (b.length >= 3 && (b[0] & 255) == 255 && (b[1] & 255) == 216 && (b[2] & 255) == 255) return "JPEG";
        if (b.length >= 8 && (b[0] & 255) == 137 && b[1] == 80 && b[2] == 78 && b[3] == 71) return "PNG";
        if (b.length >= 12 && new String(b, 0, 4, StandardCharsets.US_ASCII).equals("RIFF") && new String(b, 8, 4, StandardCharsets.US_ASCII).equals("WEBP")) return "WEBP";
        if (b.length >= 12 && new String(b, 4, 4, StandardCharsets.US_ASCII).equals("ftyp")
            && Set.of("heic", "heix", "hevc", "hevx", "heim", "heis", "mif1", "msf1").contains(new String(b, 8, 4, StandardCharsets.US_ASCII))) return "HEIF";
        return "UNKNOWN";
    }

    /** Returns {full, thumbnail} JPEG bytes. */
    static byte[][] process(byte[] bytes, int fullEdge, int thumbEdge) {
        if (bytes == null || bytes.length == 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a photo");
        if (signature(bytes).equals("UNKNOWN")) throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Use JPG, PNG, WebP, HEIC or HEIF");
        Path in = null, full = null, thumb = null;
        try {
            in = Files.createTempFile("album-in-", ".image");
            full = Files.createTempFile("album-full-", ".jpg");
            thumb = Files.createTempFile("album-thumb-", ".jpg");
            Files.write(in, bytes);
            magick(in + "[0]", fullEdge, 80, full);
            magick(full.toString(), thumbEdge, 72, thumb);
            byte[] f = Files.readAllBytes(full), t = Files.readAllBytes(thumb);
            if (f.length == 0 || t.length == 0 || !signature(f).equals("JPEG") || f.length > 4L * 1024 * 1024)
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "This photo could not be prepared. Try a smaller one.");
            return new byte[][]{f, t};
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Photo processing is unavailable right now; try a JPEG", e);
        } finally {
            for (Path p : new Path[]{in, full, thumb}) try { if (p != null) Files.deleteIfExists(p); } catch (Exception ignored) {}
        }
    }

    private static void magick(String input, int edge, int quality, Path out) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(List.of("magick", "-limit", "memory", "128MiB", "-limit", "map", "256MiB", "-limit", "disk", "512MiB",
            input, "-auto-orient", "-strip", "-resize", edge + "x" + edge + ">", "-quality", String.valueOf(quality), "jpeg:" + out));
        pb.redirectErrorStream(true);
        pb.environment().put("MAGICK_TEMPORARY_PATH", System.getProperty("java.io.tmpdir"));
        Process proc = pb.start();
        proc.getInputStream().transferTo(java.io.OutputStream.nullOutputStream());
        if (!proc.waitFor(25, TimeUnit.SECONDS)) { proc.destroyForcibly(); throw new IllegalStateException("Image processing timed out"); }
        if (proc.exitValue() != 0) throw new IllegalStateException("Image conversion failed");
    }
}
