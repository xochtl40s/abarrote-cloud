package com.abarrote.abarroteapi.saas.service.impl;

import com.abarrote.abarroteapi.entity.Usuario;
import com.abarrote.abarroteapi.multitenant.domain.EstadoTenant;
import com.abarrote.abarroteapi.multitenant.domain.Tenant;
import com.abarrote.abarroteapi.multitenant.domain.TipoNegocio;
import com.abarrote.abarroteapi.multitenant.repository.TenantRepository;
import com.abarrote.abarroteapi.repository.UsuarioRepository;
import com.abarrote.abarroteapi.saas.entity.SaasProspecto;
import com.abarrote.abarroteapi.saas.repository.SaasProspectoRepository;
import com.abarrote.abarroteapi.saas.service.SaasNegocioEliminacionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SaasNegocioEliminacionServiceImpl
        implements SaasNegocioEliminacionService {

    private final SaasProspectoRepository prospectoRepository;
    private final TenantRepository tenantRepository;
    private final UsuarioRepository usuarioRepository;

    public SaasNegocioEliminacionServiceImpl(
            SaasProspectoRepository prospectoRepository,
            TenantRepository tenantRepository,
            UsuarioRepository usuarioRepository) {

        this.prospectoRepository = prospectoRepository;
        this.tenantRepository = tenantRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Eliminación lógica, atómica e idempotente.
     *
     * No se borran registros históricos ni relaciones físicas.
     * Se cancela el tenant y se bloquean todos sus accesos.
     */
    @Override
    @Transactional
    public void eliminarNegocio(
            String folio,
            Long superAdminUsuarioId) {

        if (folio == null || folio.isBlank()) {
            throw new IllegalArgumentException(
                    "El folio es obligatorio."
            );
        }

        SaasProspecto prospecto =
                prospectoRepository
                        .findByFolioIgnoreCase(
                                folio.trim()
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "No existe el negocio con folio: "
                                                + folio
                                )
                        );

        if ("ELIMINADO".equalsIgnoreCase(
                prospecto.getEstado()
        )) {
            return;
        }

        Long tenantId =
                prospecto.getTenantId();

        if (tenantId != null) {

            Tenant tenant =
                    tenantRepository
                            .findById(tenantId)
                            .orElseThrow(
                                    () -> new IllegalStateException(
                                            "El prospecto tiene tenant_id, "
                                                    + "pero el tenant no existe."
                                    )
                            );

            if (TipoNegocio.PLATFORM.equals(
                    tenant.getTipoNegocio()
            )) {
                throw new IllegalStateException(
                        "No está permitido eliminar "
                                + "el tenant PLATFORM."
                );
            }

            tenant.setEstado(
                    EstadoTenant.CANCELADO
            );

            /*
             * Tenant.preUpdate() dejará activo=false
             * automáticamente al detectar CANCELADO.
             */
            tenantRepository.saveAndFlush(tenant);

            List<Usuario> usuarios =
                    usuarioRepository
                            .findByTenantIdOrderByNombreAsc(
                                    tenantId
                            );

            usuarios.forEach(
                    usuario ->
                            usuario.setActivo(false)
            );

            usuarioRepository.saveAll(usuarios);
            usuarioRepository.flush();
        }

        prospecto.setEstado("ELIMINADO");

        prospecto.setAtendidoPorUsuarioId(
                superAdminUsuarioId
        );

        String observacionAnterior =
                prospecto.getObservaciones();

        String marca =
                "Negocio eliminado lógicamente "
                        + "desde Super Admin.";

        if (observacionAnterior == null
                || observacionAnterior.isBlank()) {

            prospecto.setObservaciones(marca);

        } else if (!observacionAnterior.contains(marca)) {

            prospecto.setObservaciones(
                    observacionAnterior
                            + "\n"
                            + marca
            );
        }

        prospectoRepository.saveAndFlush(prospecto);
    }
}
