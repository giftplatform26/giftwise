package com.giftplatform.event.entity;

/** How guests can give at an event. Existing events default to {@link #CONTRIBUTION_AND_GIFTS}. */
public enum GiftMode {
    /** Wishlist gifts plus "give toward the celebration" (the original experience). */
    CONTRIBUTION_AND_GIFTS,
    /** Only "give toward the celebration"; no wishlist or gift items. */
    CONTRIBUTION_ONLY;

    public static GiftMode orDefault(GiftMode mode) { return mode == null ? CONTRIBUTION_AND_GIFTS : mode; }
}
