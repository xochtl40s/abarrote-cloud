package com.abarrote.abarroteapi.multitenant.context;

public final class TenantContext {

    private static final ThreadLocal<Long> TENANT_ID =
        new ThreadLocal<>();

    private static final ThreadLocal<String> TENANT_SLUG =
        new ThreadLocal<>();

    private TenantContext() {
    }

    public static void establecer(
        Long tenantId,
        String tenantSlug
    ) {
        if (tenantId == null) {
            throw new IllegalArgumentException(
                "El tenantId no puede ser nulo"
            );
        }

        if (
            tenantSlug == null
                || tenantSlug.isBlank()
        ) {
            throw new IllegalArgumentException(
                "El tenantSlug no puede estar vacío"
            );
        }

        TENANT_ID.set(tenantId);
        TENANT_SLUG.set(tenantSlug);
    }

    public static Long obtenerTenantId() {
        Long tenantId = TENANT_ID.get();

        if (tenantId == null) {
            throw new IllegalStateException(
                "No existe tenant en la petición actual"
            );
        }

        return tenantId;
    }

    public static String obtenerTenantSlug() {
        String tenantSlug = TENANT_SLUG.get();

        if (
            tenantSlug == null
                || tenantSlug.isBlank()
        ) {
            throw new IllegalStateException(
                "No existe tenant en la petición actual"
            );
        }

        return tenantSlug;
    }

    public static Long obtenerTenantIdOpcional() {
        return TENANT_ID.get();
    }

    public static boolean existeTenant() {
        return TENANT_ID.get() != null;
    }

    public static void limpiar() {
        TENANT_ID.remove();
        TENANT_SLUG.remove();
    }
}
