package com.abarrote.abarroteapi.saas.repository;

import com.abarrote.abarroteapi.saas.entity.SaasPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SaasPlanRepository extends JpaRepository<SaasPlan, Long> {

    Optional<SaasPlan> findByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCase(String codigo);

    List<SaasPlan> findAllByOrderByOrdenVisualAscNombreAsc();

    List<SaasPlan> findByActivoTrueOrderByOrdenVisualAscNombreAsc();

    long countByActivoTrue();
}
