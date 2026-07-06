package com.centria.cabbooking.common.enums;

import java.util.EnumSet;
import java.util.Set;

/**
 * Trip lifecycle state machine:
 *
 *   PENDING -> ACCEPTED -> IN_PROGRESS -> COMPLETED
 *   PENDING -> CANCELLED
 *   ACCEPTED -> CANCELLED
 *
 * Any other transition (e.g. completing a trip that was never accepted)
 * is rejected by {@link #canTransitionTo(TripStatus)}. This mirrors the
 * validation gate described in the project report, which was introduced
 * after functional testing found that trips could be force-completed
 * without ever being accepted or started.
 */
public enum TripStatus {
    PENDING,
    ACCEPTED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    public boolean canTransitionTo(TripStatus target) {
        Set<TripStatus> allowed = switch (this) {
            case PENDING -> EnumSet.of(ACCEPTED, CANCELLED);
            case ACCEPTED -> EnumSet.of(IN_PROGRESS, CANCELLED);
            case IN_PROGRESS -> EnumSet.of(COMPLETED);
            case COMPLETED, CANCELLED -> EnumSet.noneOf(TripStatus.class);
        };
        return allowed.contains(target);
    }
}
