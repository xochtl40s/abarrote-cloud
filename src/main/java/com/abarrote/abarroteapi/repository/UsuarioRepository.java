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

    @EntityGraph(
        attributePaths = {
            "sucursal",
            "tenant"
        }
    )
    Optional<Usuario> findByUsernameIgnoreCase(
            String username
    );

    @EntityGraph(
        attributePaths = {
            "sucursal",
            "tenant"
        }
    )
    Optional<Usuario> findConSucursalById(
            Long id
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
            "tenant"
        }
    )
    List<Usuario> findByTenantIdOrderByNombreAsc(
            Long tenantId
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
}
