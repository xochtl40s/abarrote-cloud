package com.abarrote.abarroteapi.repository;

import com.abarrote.abarroteapi.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsernameIgnoreCase(
            String username
    );

    List<Usuario> findBySucursalIdOrderByNombreAsc(
            Long sucursalId
    );

    List<Usuario>
    findBySucursalIdAndActivoTrueOrderByNombreAsc(
            Long sucursalId
    );
}
