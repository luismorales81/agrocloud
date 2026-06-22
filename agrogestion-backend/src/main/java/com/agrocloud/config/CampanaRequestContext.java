package com.agrocloud.config;

/**
 * Contexto de campaña por request (header X-Campaign-Id).
 */
public final class CampanaRequestContext {

    private static final ThreadLocal<Long> CAMPANA_ID = new ThreadLocal<>();

    private CampanaRequestContext() {
    }

    public static void setCampanaId(Long campanaId) {
        CAMPANA_ID.set(campanaId);
    }

    public static Long getCampanaId() {
        return CAMPANA_ID.get();
    }

    public static void clear() {
        CAMPANA_ID.remove();
    }
}
