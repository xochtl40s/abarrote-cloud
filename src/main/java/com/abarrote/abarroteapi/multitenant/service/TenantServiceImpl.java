package com.abarrote.abarroteapi.multitenant.service;

import com.abarrote.abarroteapi.multitenant.domain.EstadoTenant;
import com.abarrote.abarroteapi.multitenant.domain.Tenant;
import com.abarrote.abarroteapi.multitenant.dto.TenantRequest;
import com.abarrote.abarroteapi.multitenant.dto.TenantResponse;
import com.abarrote.abarroteapi.multitenant.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;

    public TenantServiceImpl(
        TenantRepository tenantRepository
    ) {
        this.tenantRepository = tenantRepository;
    }

    @Override
    public TenantResponse crear(TenantRequest request) {
        String slug = normalizarSlug(request.getSlug());

        if (tenantRepository.existsBySlugIgnoreCase(slug)) {
            throw new IllegalArgumentException(
                "Ya existe una empresa con el slug: " + slug
            );
        }

        Tenant tenant = new Tenant();
        tenant.setNombre(request.getNombre().trim());
        tenant.setSlug(slug);
        tenant.setTipoNegocio(request.getTipoNegocio());
        tenant.setPlan(
            request.getPlan()
                .trim()
                .toUpperCase(Locale.ROOT)
        );
        tenant.setFechaVencimiento(
            request.getFechaVencimiento()
        );
        tenant.setEstado(EstadoTenant.ACTIVO);
        tenant.setActivo(true);

        return convertirRespuesta(
            tenantRepository.save(tenant)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TenantResponse obtenerPorId(Long id) {
        return convertirRespuesta(
            obtenerEntidad(id)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TenantResponse obtenerPorSlug(String slug) {
        Tenant tenant = tenantRepository
            .findBySlugIgnoreCase(
                normalizarSlug(slug)
            )
            .orElseThrow(
                () -> new IllegalArgumentException(
                    "No existe el tenant con slug: " + slug
                )
            );

        return convertirRespuesta(tenant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TenantResponse> listar() {
        return tenantRepository
            .findAllByOrderByNombreAsc()
            .stream()
            .map(this::convertirRespuesta)
            .toList();
    }

    @Override
    public TenantResponse suspender(Long id) {
        Tenant tenant = obtenerEntidad(id);

        tenant.setEstado(EstadoTenant.SUSPENDIDO);
        tenant.setActivo(false);

        return convertirRespuesta(
            tenantRepository.save(tenant)
        );
    }

    @Override
    public TenantResponse reactivar(Long id) {
        Tenant tenant = obtenerEntidad(id);

        tenant.setEstado(EstadoTenant.ACTIVO);
        tenant.setActivo(true);

        return convertirRespuesta(
            tenantRepository.save(tenant)
        );
    }

    private Tenant obtenerEntidad(Long id) {
        return tenantRepository
            .findById(id)
            .orElseThrow(
                () -> new IllegalArgumentException(
                    "No existe el tenant con id: " + id
                )
            );
    }

    private TenantResponse convertirRespuesta(
        Tenant tenant
    ) {
        return new TenantResponse(
            tenant.getId(),
            tenant.getNombre(),
            tenant.getSlug(),
            tenant.getTipoNegocio(),
            tenant.getEstado(),
            tenant.getPlan(),
            tenant.getFechaAlta(),
            tenant.getFechaVencimiento(),
            tenant.isActivo()
        );
    }

    private String normalizarSlug(String valor) {
        if (valor == null) {
            return "";
        }

        return valor
            .trim()
            .toLowerCase(Locale.ROOT);
    }
}
