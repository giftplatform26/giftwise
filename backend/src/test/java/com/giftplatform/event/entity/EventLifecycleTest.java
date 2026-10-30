package com.giftplatform.event.entity;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

class EventLifecycleTest {
    @Test void newEventStartsAsDraft() {
        assertThat(new Event("Birthday", OccasionType.BIRTHDAY).getStatus()).isEqualTo(EventStatus.DRAFT);
    }
    @Test void ownerCanBeAssignedOnce() {
        Event e = new Event("Birthday", OccasionType.BIRTHDAY);
        UUID owner = UUID.randomUUID(); e.assignOwner(owner);
        assertThat(e.getOwnerId()).isEqualTo(owner);
        assertThatThrownBy(() -> e.assignOwner(UUID.randomUUID())).isInstanceOf(IllegalStateException.class);
    }
    @Test void nullOwnerRejected() {
        Event e = new Event("Birthday", OccasionType.BIRTHDAY);
        assertThatThrownBy(() -> e.assignOwner(null)).isInstanceOf(IllegalStateException.class);
    }
    @Test void draftCanBePublishedThenCompleted() {
        Event e = new Event("Birthday", OccasionType.BIRTHDAY);
        e.transition(EventStatus.PUBLISHED); e.transition(EventStatus.COMPLETED);
        assertThat(e.getStatus()).isEqualTo(EventStatus.COMPLETED);
    }
    @Test void draftCanBeCancelled() {
        Event e = new Event("Birthday", OccasionType.BIRTHDAY);
        e.transition(EventStatus.CANCELLED);
        assertThat(e.getStatus()).isEqualTo(EventStatus.CANCELLED);
    }
    @Test void completedEventCannotTransition() {
        Event e = new Event("Birthday", OccasionType.BIRTHDAY);
        e.transition(EventStatus.PUBLISHED); e.transition(EventStatus.COMPLETED);
        assertThatThrownBy(() -> e.transition(EventStatus.CANCELLED)).isInstanceOf(IllegalStateException.class);
    }
    @Test void cancelledEventCannotBePublished() {
        Event e = new Event("Birthday", OccasionType.BIRTHDAY);
        e.transition(EventStatus.CANCELLED);
        assertThatThrownBy(() -> e.transition(EventStatus.PUBLISHED)).isInstanceOf(IllegalStateException.class);
    }
    @Test void draftCannotCompleteDirectly() {
        Event e = new Event("Birthday", OccasionType.BIRTHDAY);
        assertThatThrownBy(() -> e.transition(EventStatus.COMPLETED)).isInstanceOf(IllegalStateException.class);
    }
    @Test
    void publishedEventCanBeUnpublishedAndRepublished() {
        Event event = new Event("Birthday", OccasionType.BIRTHDAY);
        UUID originalToken = event.getPublicToken();

        event.transition(EventStatus.PUBLISHED);
        event.transition(EventStatus.DRAFT);

        assertThat(event.getStatus()).isEqualTo(EventStatus.DRAFT);
        assertThat(event.getPublicToken()).isEqualTo(originalToken);

        event.transition(EventStatus.PUBLISHED);

        assertThat(event.getStatus()).isEqualTo(EventStatus.PUBLISHED);
        assertThat(event.getPublicToken()).isEqualTo(originalToken);
    }

    @Test
    void draftCannotBeUnpublishedAgain() {
        Event event = new Event("Birthday", OccasionType.BIRTHDAY);

        assertThatThrownBy(
                () -> event.transition(EventStatus.DRAFT)
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void completedEventCannotBeUnpublished() {
        Event event = new Event("Birthday", OccasionType.BIRTHDAY);

        event.transition(EventStatus.PUBLISHED);
        event.transition(EventStatus.COMPLETED);

        assertThatThrownBy(
                () -> event.transition(EventStatus.DRAFT)
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cancelledEventCannotBeRepublished() {
        Event event = new Event("Birthday", OccasionType.BIRTHDAY);

        event.transition(EventStatus.CANCELLED);

        assertThatThrownBy(
                () -> event.transition(EventStatus.PUBLISHED)
        ).isInstanceOf(IllegalStateException.class);
    }
}
