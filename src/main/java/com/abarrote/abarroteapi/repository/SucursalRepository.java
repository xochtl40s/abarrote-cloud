package com.abarrote.abarroteapi.repository;

import com.abarrote.abarroteapi.entity.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SucursalRepository
        extends JpaRepository<Sucursal, Long> {

    List<Sucursal> findByActivaTrueOrderByNombreAsc();

    Optional<Sucursal> findByCodigoIgnoreCase(
            String codigo
    );

    boolean existsByCodigoIgnoreCase(
            String codigo
    );
}
