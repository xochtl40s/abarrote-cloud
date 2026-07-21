package com.abarrote.abarroteapi.saas.service.impl;

import com.abarrote.abarroteapi.entity.Usuario;
import com.abarrote.abarroteapi.multitenant.domain.Tenant;
import com.abarrote.abarroteapi.multitenant.repository.TenantRepository;
import com.abarrote.abarroteapi.repository.UsuarioRepository;
import com.abarrote.abarroteapi.saas.entity.SaasProspecto;
import com.abarrote.abarroteapi.saas.repository.SaasProspectoRepository;
import com.abarrote.abarroteapi.saas.service.SaasNegocioEstadoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SaasNegocioEstadoServiceImpl
        implements SaasNegocioEstadoService {

    private final SaasProspectoRepository prospectoRepository;
    private final TenantRepository tenantRepository;
    private final UsuarioRepository usuarioRepository;

    public SaasNegocioEstadoServiceImpl(
            SaasProspectoRepository prospectoRepository,
            TenantRepository tenantRepository,
            UsuarioRepository usuarioRepository) {

        this.prospectoRepository = prospectoRepository;
        this.tenantRepository = tenantRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public String alternarEstado(
            String folio,
            Long superAdminUsuarioId) {

        SaasProspecto prospecto =
                prospectoRepository
                        .findByFolioIgnoreCase(folio)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "No existe el negocio con folio: "
                                                + folio
                                )
                        );

        if ("ELIMINADO".equalsIgnoreCase(
                prospecto.getEstado()
        )) {
            throw new IllegalStateException(
                    "No se puede cambiar el estado "
                            + "de un negocio eliminado."
            );
        }

        if (prospecto.getTenantId() == null) {
            throw new IllegalStateException(
                    "El prospecto todavía no tiene tenant."
            );
        }

        Tenant tenant =
                tenantRepository
                        .findById(
                                prospecto.getTenantId()
                        )
                        .orElseThrow(
                                () -> new IllegalStateException(
                                        "El tenant relacionado no existe."
                                )
                        );

        List<Usuario> usuarios =
                usuarioRepository
                        .findByTenantIdOrderByNombreAsc(
                                tenant.getId()
                        );

        boolean actualmenteActivo =
                "ACTIVADO".equalsIgnoreCase(
                        prospecto.getEstado()
                );

        if (actualmenteActivo) {

            usuarios.forEach(
                    usuario ->
                            usuario.setActivo(false)
            );

            prospecto.setEstado("EN_PROCESO");

            prospecto.setObservaciones(
                    agregarEvento(
                            prospecto.getObservaciones(),
                            "Negocio suspendido desde Super Admin."
                    )
            );

            usuarioRepository.saveAll(usuarios);
            prospectoRepository.saveAndFlush(prospecto);

            return "Negocio suspendido. "
                    + "Todos sus accesos fueron desactivados.";

        } else {

            usuarios.forEach(
                    usuario ->
                            usuario.setActivo(true)
            );

            prospecto.setEstado("ACTIVADO");

            prospecto.setObservaciones(
                    agregarEvento(
                            prospecto.getObservaciones(),
                            "Negocio reactivado desde Super Admin."
                    )
            );

            usuarioRepository.saveAll(usuarios);
            prospectoRepository.saveAndFlush(prospecto);

            return "Negocio reactivado correctamente.";
        }
    }

    private String agregarEvento(
            String observaciones,
            String evento) {

        if (observaciones == null
                || observaciones.isBlank()) {

            return evento;
        }

        if (observaciones.contains(evento)) {
            return observaciones;
        }

        return observaciones
                + System.lineSeparator()
                + evento;
    }
}
