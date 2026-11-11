package com.giftplatform.register;

import java.time.LocalDate;

/** Test bridge to the package-private date helper. */
public final class OccasionRemindersControllerAccess {
    private OccasionRemindersControllerAccess() {}
    public static LocalDate next(LocalDate today, int month, int day) { return OccasionRemindersController.next(today, month, day); }
}
