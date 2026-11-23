package com.daon.daonpay.global.util;

import org.hibernate.exception.ConstraintViolationException;

public final class DatabaseConstraints {
    private DatabaseConstraints() {
    }

    public static boolean matches(Throwable failure, String expectedName) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException violation && violation.getConstraintName() != null) {
                String qualifiedName = violation.getConstraintName();
                String name = qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1);
                if (expectedName.equalsIgnoreCase(name)) {
                    return true;
                }
            }
        }
        return false;
    }
}
