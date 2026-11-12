package com.giftplatform.eventops;

import com.giftplatform.admin.EffectiveModulePermissionService;
import com.giftplatform.cohost.EventAccess;
import com.giftplatform.cohost.EventAccess.Capability;
import com.giftplatform.event.entity.Event;
import com.giftplatform.event.entity.EventStatus;
import com.giftplatform.event.repository.EventRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Shared photo album: the host prints or shows a QR; guests scan it and add photos from their phone without an
 * account. Photos appear in one album the host can moderate and download. Separate from the guestbook memory album.
 */
@RestController
public class SharedAlbumController extends EventOpsSupport {
    public static final String MODULE = "SHARED_ALBUM";
    static final int MAX_PHOTOS = 3000, PAGE = 60;

    private final JdbcTemplate db;
    private final EventAccess access;
    private final EventRepository events;
    private final EffectiveModulePermissionService permissions;
    private final com.giftplatform.qr.QrRenderer qr;
    private final String guestBaseUrl;
    private final Path storage;

    public SharedAlbumController(JdbcTemplate db, EventAccess access, EventRepository events, EffectiveModulePermissionService permissions, com.giftplatform.qr.QrRenderer qr,
                                 @Value("${gift.guest-base-url:http://localhost:3000}") String guestBaseUrl, @Value("${gift.shared-album.dir:}") String dir,
                                 @Value("${gift.guestbook.photos.dir:}") String guestbookDir) {
        this.db = db; this.access = access; this.events = events; this.permissions = permissions; this.qr = qr;
        this.guestBaseUrl = guestBaseUrl.replaceAll("/+$", "");
        // Defaults to a folder inside the private guestbook media directory (required in production), else the home folder.
        String chosen = dir != null && !dir.isBlank() ? dir : guestbookDir != null && !guestbookDir.isBlank() ? guestbookDir + "/shared-album"
            : System.getProperty("user.home") + "/.gift-platform/shared-album";
        this.storage = Path.of(chosen).toAbsolutePath().normalize();
    }

    public record Photo(UUID id, String uploaderName, String caption, String status, OffsetDateTime createdAt) {}
    public record Settings(boolean uploadsOpen, String moderation, boolean guestsCanView) {}
    public record HostAlbum(UUID uploadToken, String uploadPath, Settings settings, List<Photo> photos, int approved, int pending, int hidden, long bytes, int limit) {}
    public record PublicAlbum(String eventTitle, java.time.LocalDate eventDate, String hostName, boolean uploadsOpen, boolean guestsCanView, boolean reviewed,
                              List<Photo> photos, int total, boolean hasMore, String invitationPath) {}
    public record StatusInput(String status) {}
    public record Uploaded(UUID id, String status, String message) {}

    // ---- Host ----

    @GetMapping("/api/v1/events/{id}/shared-album")
    public ResponseEntity<HostAlbum> host(@PathVariable UUID id) {
        access.require(id, Capability.GUESTS, MODULE);
        var album = ensure(id);
        List<Photo> photos = db.query("SELECT * FROM event_shared_photos WHERE event_id=? ORDER BY created_at DESC LIMIT 3000", (rs, i) -> photo(rs), id);
        Long bytes = db.queryForObject("SELECT COALESCE(SUM(bytes),0) FROM event_shared_photos WHERE event_id=?", Long.class, id);
        UUID token = (UUID) album.get("upload_token");
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new HostAlbum(token, "/a/" + token, settings(album), photos,
            count(photos, "APPROVED"), count(photos, "PENDING"), count(photos, "HIDDEN"), bytes == null ? 0 : bytes, MAX_PHOTOS));
    }

    @PutMapping("/api/v1/events/{id}/shared-album/settings")
    public Settings saveSettings(@PathVariable UUID id, @RequestBody Settings in) {
        access.require(id, Capability.GUESTS, MODULE);
        if (in == null) throw bad("Settings are required");
        String mod = in.moderation() == null ? "AUTO" : in.moderation().strip().toUpperCase(Locale.ROOT);
        if (!mod.equals("AUTO") && !mod.equals("REVIEW")) throw bad("Choose whether photos appear straight away or after you approve them");
        ensure(id);
        db.update("UPDATE event_shared_albums SET uploads_open=?, moderation=?, guests_can_view=? WHERE event_id=?", in.uploadsOpen(), mod, in.guestsCanView(), id);
        return settings(ensure(id));
    }

    /** New QR link: old printed or shared QRs stop working for uploads and viewing. */
    @PostMapping("/api/v1/events/{id}/shared-album/new-link")
    public Map<String, Object> newLink(@PathVariable UUID id) {
        access.require(id, Capability.GUESTS, MODULE);
        ensure(id);
        UUID token = db.queryForObject("UPDATE event_shared_albums SET upload_token=gen_random_uuid() WHERE event_id=? RETURNING upload_token", UUID.class, id);
        return Map.of("uploadToken", token, "uploadPath", "/a/" + token);
    }

    @PostMapping("/api/v1/events/{id}/shared-album/photos/{photoId}/status")
    public Photo setStatus(@PathVariable UUID id, @PathVariable UUID photoId, @RequestBody StatusInput in) {
        access.require(id, Capability.GUESTS, MODULE);
        String s = in == null || in.status() == null ? "" : in.status().strip().toUpperCase(Locale.ROOT);
        if (!Set.of("APPROVED", "HIDDEN", "PENDING").contains(s)) throw bad("Choose show or hide");
        if (db.update("UPDATE event_shared_photos SET status=? WHERE id=? AND event_id=?", s, photoId, id) == 0) throw notFound();
        return db.query("SELECT * FROM event_shared_photos WHERE id=?", (rs, i) -> photo(rs), photoId).getFirst();
    }

    @PostMapping("/api/v1/events/{id}/shared-album/photos/{photoId}/delete")
    @Transactional
    public Map<String, Object> delete(@PathVariable UUID id, @PathVariable UUID photoId) {
        access.require(id, Capability.GUESTS, MODULE);
        var rows = db.queryForList("DELETE FROM event_shared_photos WHERE id=? AND event_id=? RETURNING file_name, thumb_name", photoId, id);
        if (rows.isEmpty()) throw notFound();
        afterCommit(() -> { remove((String) rows.getFirst().get("file_name")); remove((String) rows.getFirst().get("thumb_name")); });
        return Map.of("deleted", true);
    }

    @GetMapping("/api/v1/events/{id}/shared-album/photos/{photoId}")
    public ResponseEntity<byte[]> hostPhoto(@PathVariable UUID id, @PathVariable UUID photoId, @RequestParam(defaultValue = "false") boolean thumb) {
        access.require(id, Capability.GUESTS, MODULE);
        return file(id, photoId, thumb, false);
    }

    @GetMapping(value = "/api/v1/events/{id}/shared-album/qr.png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> hostQr(@PathVariable UUID id) {
        access.require(id, Capability.GUESTS, MODULE);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(qr.png(guestBaseUrl + "/a/" + ensure(id).get("upload_token"), 640));
    }

    /** Every photo that is shown in the album (approved), as one zip download. */
    @GetMapping("/api/v1/events/{id}/shared-album/download.zip")
    public void download(@PathVariable UUID id, HttpServletResponse response) throws java.io.IOException {
        var a = access.require(id, Capability.GUESTS, MODULE);
        var rows = db.queryForList("SELECT id, uploader_name, file_name FROM event_shared_photos WHERE event_id=? AND status='APPROVED' ORDER BY created_at", id);
        String slug = a.event().getTitle().replaceAll("[^A-Za-z0-9]+", "-").replaceAll("(^-|-$)", "").toLowerCase(Locale.ROOT);
        response.setContentType("application/zip");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + (slug.isEmpty() ? "album" : slug) + "-photos.zip\"");
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        try (ZipOutputStream zip = new ZipOutputStream(response.getOutputStream())) {
            int n = 0;
            for (var r : rows) {
                Path p = resolve((String) r.get("file_name"));
                if (p == null || !Files.exists(p)) continue;
                String who = r.get("uploader_name") == null ? "" : "-" + ((String) r.get("uploader_name")).replaceAll("[^A-Za-z0-9]+", "-");
                zip.putNextEntry(new ZipEntry(String.format("%04d%s.jpg", ++n, who.length() > 30 ? who.substring(0, 30) : who)));
                Files.copy(p, zip);
                zip.closeEntry();
            }
        }
    }

    // ---- Guests (QR link, no account) ----

    @GetMapping("/api/public/albums/{token}")
    public ResponseEntity<PublicAlbum> view(@PathVariable UUID token, @RequestParam(defaultValue = "0") int offset) {
        var c = context(token);
        if (offset < 0 || offset > MAX_PHOTOS) throw bad("Invalid page");
        boolean canView = Boolean.TRUE.equals(c.album().get("guests_can_view"));
        List<Photo> photos = canView ? db.query("SELECT * FROM event_shared_photos WHERE event_id=? AND status='APPROVED' ORDER BY created_at DESC LIMIT ? OFFSET ?",
            (rs, i) -> photo(rs), c.event().getId(), PAGE, offset) : List.of();
        Integer total = canView ? db.queryForObject("SELECT count(*) FROM event_shared_photos WHERE event_id=? AND status='APPROVED'", Integer.class, c.event().getId()) : 0;
        Event e = c.event();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).header("X-Robots-Tag", "noindex").body(new PublicAlbum(e.getTitle(), e.getEventDate(), e.getHostDisplayName(),
            Boolean.TRUE.equals(c.album().get("uploads_open")), canView, "REVIEW".equals(c.album().get("moderation")), photos.stream().map(p -> new Photo(p.id(), p.uploaderName(), p.caption(), null, p.createdAt())).toList(),
            total == null ? 0 : total, offset + photos.size() < (total == null ? 0 : total), "/e/" + e.getPublicToken()));
    }

    @PostMapping(path = "/api/public/albums/{token}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Uploaded upload(@PathVariable UUID token, @RequestPart("photo") MultipartFile photo, @RequestParam(required = false) String name,
                           @RequestParam(required = false) String caption, HttpServletRequest http) throws java.io.IOException {
        var c = context(token);
        if (!Boolean.TRUE.equals(c.album().get("uploads_open"))) throw conflict("The host has closed this album to new photos");
        String who = text(name, 80, "Name"), cap = text(caption, 200, "Caption");
        if (photo == null || photo.isEmpty()) throw bad("Choose a photo");
        if (photo.getSize() > 15L * 1024 * 1024) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Photo must be 15 MB or less");
        UUID eventId = c.event().getId();
        db.queryForList("SELECT event_id FROM event_shared_albums WHERE event_id=? FOR UPDATE", eventId);
        Integer n = db.queryForObject("SELECT count(*) FROM event_shared_photos WHERE event_id=?", Integer.class, eventId);
        if (n != null && n >= MAX_PHOTOS) throw conflict("This album is full");
        byte[][] jpg = PhotoProcessor.process(photo.getBytes(), 2000, 480);
        limit(db, eventId, http, "ALBUM_UPLOAD", 80);
        String status = "REVIEW".equals(c.album().get("moderation")) ? "PENDING" : "APPROVED";
        UUID id = UUID.randomUUID();
        String full = UUID.randomUUID() + ".jpg", thumb = UUID.randomUUID() + ".jpg";
        Files.createDirectories(storage);
        Path fp = resolve(full), tp = resolve(thumb);
        if (fp == null || tp == null) throw new IllegalStateException("Invalid photo path");
        Files.write(fp, jpg[0], StandardOpenOption.CREATE_NEW);
        Files.write(tp, jpg[1], StandardOpenOption.CREATE_NEW);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCompletion(int s) { if (s != STATUS_COMMITTED) { remove(full); remove(thumb); } }
        });
        db.update("INSERT INTO event_shared_photos(id,event_id,uploader_name,caption,file_name,thumb_name,bytes,status) VALUES(?,?,?,?,?,?,?,?)",
            id, eventId, who, cap, full, thumb, jpg[0].length + jpg[1].length, status);
        return new Uploaded(id, status, status.equals("PENDING") ? "Thank you! Your photo will appear once the host approves it." : "Thank you! Your photo is in the album.");
    }

    @GetMapping("/api/public/albums/{token}/photos/{photoId}")
    public ResponseEntity<byte[]> publicPhoto(@PathVariable UUID token, @PathVariable UUID photoId) {
        var c = context(token);
        if (!Boolean.TRUE.equals(c.album().get("guests_can_view"))) throw notFound();
        return file(c.event().getId(), photoId, false, true);
    }

    @GetMapping("/api/public/albums/{token}/photos/{photoId}/thumb")
    public ResponseEntity<byte[]> publicThumb(@PathVariable UUID token, @PathVariable UUID photoId) {
        var c = context(token);
        if (!Boolean.TRUE.equals(c.album().get("guests_can_view"))) throw notFound();
        return file(c.event().getId(), photoId, true, true);
    }

    @GetMapping(value = "/api/public/albums/{token}/qr.png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> publicQr(@PathVariable UUID token) {
        context(token);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(qr.png(guestBaseUrl + "/a/" + token, 640));
    }

    /** For the invitation page: where guests can add photos, if the album is on and open or viewable. 404 otherwise. */
    @GetMapping("/api/public/celebrations/{token}/shared-album")
    public ResponseEntity<Map<String, Object>> fromInvitation(@PathVariable UUID token) {
        Event e = events.findByPublicToken(token).filter(x -> x.getStatus() == EventStatus.PUBLISHED).orElseThrow(EventOpsSupport::notFound);
        if (!permissions.eventEnabled(e.getOwnerId(), e.getId(), MODULE)) throw notFound();
        var rows = db.queryForList("SELECT upload_token, uploads_open, guests_can_view FROM event_shared_albums WHERE event_id=?", e.getId());
        if (rows.isEmpty() || !(Boolean.TRUE.equals(rows.getFirst().get("uploads_open")) || Boolean.TRUE.equals(rows.getFirst().get("guests_can_view")))) throw notFound();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(Map.of("path", "/a/" + rows.getFirst().get("upload_token"),
            "uploadsOpen", rows.getFirst().get("uploads_open")));
    }

    // ---- Helpers ----

    private record Context(Event event, Map<String, Object> album) {}
    private Context context(UUID token) {
        var rows = db.queryForList("SELECT * FROM event_shared_albums WHERE upload_token=?", token);
        if (rows.isEmpty()) throw notFound();
        Event e = events.findById((UUID) rows.getFirst().get("event_id")).filter(x -> x.getStatus() == EventStatus.PUBLISHED).orElseThrow(EventOpsSupport::notFound);
        if (!permissions.eventEnabled(e.getOwnerId(), e.getId(), MODULE)) throw notFound();
        return new Context(e, rows.getFirst());
    }
    private Map<String, Object> ensure(UUID eventId) {
        db.update("INSERT INTO event_shared_albums(event_id) VALUES(?) ON CONFLICT DO NOTHING", eventId);
        return db.queryForMap("SELECT * FROM event_shared_albums WHERE event_id=?", eventId);
    }
    private static Settings settings(Map<String, Object> a) {
        return new Settings(Boolean.TRUE.equals(a.get("uploads_open")), (String) a.get("moderation"), Boolean.TRUE.equals(a.get("guests_can_view")));
    }
    private ResponseEntity<byte[]> file(UUID eventId, UUID photoId, boolean thumb, boolean approvedOnly) {
        var names = db.queryForList("SELECT " + (thumb ? "thumb_name" : "file_name") + " FROM event_shared_photos WHERE id=? AND event_id=?" + (approvedOnly ? " AND status='APPROVED'" : ""),
            String.class, photoId, eventId);
        if (names.isEmpty()) throw notFound();
        Path p = resolve(names.getFirst());
        try {
            if (p == null) throw notFound();
            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).cacheControl(CacheControl.noStore()).header("X-Content-Type-Options", "nosniff").body(Files.readAllBytes(p));
        } catch (java.io.IOException ex) { throw notFound(); }
    }
    private Path resolve(String name) {
        if (name == null || !name.matches("[0-9a-f-]{36}\\.jpg")) return null;
        Path p = storage.resolve(name).normalize();
        return p.getParent().equals(storage) ? p : null;
    }
    private void remove(String name) { try { Path p = resolve(name); if (p != null) Files.deleteIfExists(p); } catch (Exception ignored) {} }
    private static void afterCommit(Runnable r) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) { r.run(); return; }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() { r.run(); }
        });
    }
    private static int count(List<Photo> photos, String status) { return (int) photos.stream().filter(p -> status.equals(p.status())).count(); }
    private static Photo photo(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new Photo(rs.getObject("id", UUID.class), rs.getString("uploader_name"), rs.getString("caption"), rs.getString("status"), rs.getObject("created_at", OffsetDateTime.class));
    }
}
