package com.stockflow.security;

import java.util.Locale;

public enum RoleName {
    ADMIN,
    MANAGER,
    STAFF,
    VIEWER;

    public String authority() {
        return "ROLE_" + name();
    }

    public String label() {
        return name();
    }

    public static String normalizeAuthority(String roleName) {
        return from(roleName).authority();
    }

    public static String normalizeLabel(String roleName) {
        return from(roleName).label();
    }

    public static RoleName from(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            return VIEWER;
        }

        String normalized = roleName.trim().toUpperCase(Locale.ROOT);
        if (!normalized.startsWith("ROLE_")) {
            normalized = "ROLE_" + normalized;
        }

        if ("ROLE_USER".equals(normalized)) {
            return STAFF;
        }

        for (RoleName value : values()) {
            if (value.authority().equals(normalized)) {
                return value;
            }
        }

        return VIEWER;
    }
}
