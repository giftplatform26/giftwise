package com.giftplatform.qr;

import com.giftplatform.audit.entity.AuditAction;
import com.giftplatform.audit.entity.AuditActorType;
import com.giftplatform.audit.service.AuditService;
import com.giftplatform.event.entity.Event;
import com.giftplatform.event.repository.EventRepository;
import com.giftplatform.security.ActorContext;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EventQrService {
    private final EventRepository events;
    private final ActorContext actors;
    private final AuditService audit;
    private final QrRenderer renderer;
    private final String guestBaseUrl;

    public EventQrService(EventRepository events, ActorContext actors, AuditService audit,
                          QrRenderer renderer, @Value("${gift.guest-base-url:http://localhost:3000}") String guestBaseUrl) {
        this.events = events; this.actors = actors; this.audit = audit; this.renderer = renderer;
        if (!guestBaseUrl.matches("https?://[^/\\s]+(?::[0-9]+)?(?:/[^?\\s]*)?"))
            throw new IllegalArgumentException("Invalid guest base URL");
        this.guestBaseUrl = guestBaseUrl.replaceAll("/+$", "");
    }

    @Transactional(readOnly = true)
    public QrDetails details(UUID eventId) { return detailsFor(ownedEvent(eventId)); }

    @Transactional(readOnly = true)
    public byte[] png(UUID eventId) { return renderer.png(detailsFor(ownedEvent(eventId)).url()); }

    @Transactional
    public QrDetails changeTheme(UUID eventId, QrTheme theme) {
        if (theme == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "QR theme required");
        Event event = ownedEvent(eventId);
        if (event.getStatus() == com.giftplatform.event.entity.EventStatus.COMPLETED ||
            event.getStatus() == com.giftplatform.event.entity.EventStatus.CANCELLED)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Event cannot be edited");
        QrTheme previous = event.getQrTheme();
        if (previous != theme) {
            event.changeQrTheme(theme);
            events.saveAndFlush(event);
            audit.recordChange(actors.requireAuthenticatedUserId(), AuditActorType.CUSTOMER,
                "EVENT", event.getId(), AuditAction.UPDATE,
                Map.of("qrTheme", previous.name()), Map.of("qrTheme", theme.name()), null);
        }
        return detailsFor(event);
    }

    private Event ownedEvent(UUID eventId) {
        UUID actor = actors.requireAuthenticatedUserId();
        Event event = events.findById(eventId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!actor.equals(event.getOwnerId())) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return event;
    }

    private QrDetails detailsFor(Event event) {
        return new QrDetails(event.getId(), event.getPublicToken(), event.getQrTheme(),
                guestBaseUrl + "/e/" + event.getPublicToken());
    }



    public record QrDetails(
            UUID eventId,
            UUID publicToken,
            QrTheme theme,
            String url
    ) {}
}
