package com.abarrote.abarroteapi.repository;

import com.abarrote.abarroteapi.entity.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {

    /*
     * Se conserva para autenticación, donde el username
     * actualmente es único globalmente.
     */
    @EntityGraph(
        attributePaths = {
            "sucursal",
            "tenant"
        }
    )
    Optional<Usuario> findByUsernameIgnoreCase(
            String username
    );

    /*
     * Consultas administrativas seguras por tenant.
     */

    @EntityGraph(
        attributePaths = {
            "sucursal",
            "tenant"
        }
    )
    Optional<Usuario> findByIdAndTenantId(
            Long id,
            Long tenantId
    );

    @EntityGraph(
        attributePaths = {
            "sucursal",
            "tenant"
        }
    )
    Optional<Usuario>
        findByUsernameIgnoreCaseAndTenantId(
            String username,
            Long tenantId
        );

    @EntityGraph(
        attributePaths = {
            "sucursal",
            "tenant"
        }
    )
    List<Usuario> findByTenantIdOrderByNombreAsc(
            Long tenantId
    );

    @EntityGraph(
        attributePaths = {
            "sucursal",
            "tenant"
        }
    )
    List<Usuario>
        findByTenantIdAndActivoTrueOrderByNombreAsc(
            Long tenantId
        );

    @EntityGraph(
        attributePaths = {
            "sucursal",
            "tenant"
        }
    )
    List<Usuario> findBySucursalIdOrderByNombreAsc(
            Long sucursalId
    );

    @EntityGraph(
        attributePaths = {
            "sucursal",
            "tenant"
        }
    )
    List<Usuario>
        findBySucursalIdAndActivoTrueOrderByNombreAsc(
            Long sucursalId
        );

    @EntityGraph(
        attributePaths = {
            "tenant",
            "sucursal"
        }
    )
    Optional<Usuario>
        findFirstByTenantIdAndRolIgnoreCaseOrderByIdAsc(
            Long tenantId,
            String rol
        );

    /*
     * Compatibilidad con código heredado.
     * No debe utilizarse en pantallas administrativas SaaS.
     */
    @EntityGraph(
        attributePaths = {
            "sucursal",
            "tenant"
        }
    )
    Optional<Usuario> findConSucursalById(
            Long id
    );
}
