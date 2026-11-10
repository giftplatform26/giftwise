package com.giftplatform.payment.service;

import com.giftplatform.event.entity.Event;
import com.giftplatform.event.entity.EventStatus;
import com.giftplatform.event.repository.EventRepository;
import com.giftplatform.gift.entity.Gift;
import com.giftplatform.gift.entity.GiftStatus;
import com.giftplatform.gift.entity.GiftVisibility;
import com.giftplatform.gift.repository.GiftRepository;
import com.giftplatform.payment.entity.PaymentOrder;
import com.giftplatform.payment.repository.PaymentOrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class PaymentOrderService {

    @org.springframework.beans.factory.annotation.Autowired private com.giftplatform.payment.control.PaymentControlService controls;
    private final EventRepository events;
    private final GiftRepository gifts;
    private final PaymentOrderRepository orders;
    private final JdbcTemplate jdbc;
    private final com.giftplatform.monetization.MonetizationService monetization;

    public PaymentOrderService(
            EventRepository events,
            GiftRepository gifts,
            PaymentOrderRepository orders,
            JdbcTemplate jdbc, com.giftplatform.monetization.MonetizationService monetization) {
        this.events = events;
        this.gifts = gifts;
        this.orders = orders;
        this.jdbc = jdbc;
        this.monetization = monetization;
    }

    @Transactional
    public PaymentOrder create(
            UUID eventId,
            UUID giftId,
            String contributionType,
            long amountPaise,
            String idempotencyKey) {

        if (eventId == null || idempotencyKey == null ||
                idempotencyKey.isBlank() ||
                idempotencyKey.length() > 120 ||
                amountPaise <= 0 || amountPaise > 100_000_000L) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid payment request");
        }

        // Serialize requests sharing this key across application instances.
        // PostgreSQL releases this lock at transaction completion.
        jdbc.queryForObject(
                "SELECT pg_advisory_xact_lock(hashtextextended(?, 0))",
                Object.class,
                idempotencyKey
        );

        var existing = orders.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            PaymentOrder order = existing.get();

            if (!order.getEventId().equals(eventId) ||
                    !java.util.Objects.equals(order.getGiftId(), giftId) ||
                    !order.getContributionType().equals(contributionType) ||
                    order.getRequestedAmountPaise() != amountPaise) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Idempotency key belongs to another request");
            }
            return order;
        }

        Event event = events.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Event not found"));

        if (event.getStatus() != EventStatus.PUBLISHED || !event.isGiftingEnabled()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Event is not accepting contributions");
        }

        if ("EVENT_WALLET".equals(contributionType)) {
            if (giftId != null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Wallet contribution cannot reference a gift");
            }
        } else if ("GIFT_PARTIAL".equals(contributionType) ||
                   "GIFT_FULL".equals(contributionType)) {

            if (giftId == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Gift is required");
            }

            Gift gift = gifts.findById(giftId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Gift not found"));

            if (!gift.getEvent().getId().equals(eventId)) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Gift not found");
            }

            jdbc.queryForObject("SELECT id FROM gifts WHERE id=? FOR UPDATE",UUID.class,giftId);
            if(Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM gift_groups WHERE reserved_gift_id=?)",Boolean.class,giftId)))
                throw new ResponseStatusException(HttpStatus.CONFLICT,"A surprise is waiting for this gift");
            if (gift.getStatus() != GiftStatus.ACTIVE ||
                    gift.getVisibility() != GiftVisibility.PUBLIC) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Gift is not accepting contributions");
            }

        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid contribution type");
        }

        if(controls!=null) {
            controls.require(eventId,"RAZORPAY");
            if(controls.event(eventId).get("ROUTING"))throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Routing is selected but linked-account onboarding, provider fee allocation and settlement integration must be completed before collection");
        }
        PaymentOrder order = new PaymentOrder(
                eventId,
                giftId,
                contributionType,
                amountPaise,
                idempotencyKey
        );

        order.applyPricing(monetization.quote("EVENT_WALLET".equals(contributionType)?"WALLET":"CONTRIBUTION", amountPaise));
        return orders.saveAndFlush(order);
    }
}
