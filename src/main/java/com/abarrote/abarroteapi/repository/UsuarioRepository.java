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
     * Carga el usuario y su sucursal en la misma consulta.
     *
     * Esto evita:
     *
     * could not initialize proxy [Sucursal] - no Session
     *
     * cuando open-in-view está deshabilitado en Render.
     */
    @EntityGraph(
            attributePaths = {
                    "sucursal"
            }
    )
    Optional<Usuario> findByUsernameIgnoreCase(
            String username
    );

    @EntityGraph(
            attributePaths = {
                    "sucursal"
            }
    )
    Optional<Usuario> findConSucursalById(
            Long id
    );

    @EntityGraph(
            attributePaths = {
                    "sucursal"
            }
    )
    List<Usuario> findBySucursalIdOrderByNombreAsc(
            Long sucursalId
    );

    @EntityGraph(
            attributePaths = {
                    "sucursal"
            }
    )
    List<Usuario>
    findBySucursalIdAndActivoTrueOrderByNombreAsc(
            Long sucursalId
    );
}
