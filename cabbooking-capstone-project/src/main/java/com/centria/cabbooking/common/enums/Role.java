package com.centria.cabbooking.common.enums;

/**
 * The three user roles supported by the platform.
 * Stored as a plain enum column on the single `users` table rather than
 * three separate tables, per the schema decision documented in the
 * project report.
 */
public enum Role {
    RIDER,
    DRIVER,
    ADMIN
}
