package com.abarrote.abarroteapi.gym.repository;

import com.abarrote.abarroteapi.gym.domain.ClienteGym;
import com.abarrote.abarroteapi.gym.domain.EstadoMembresia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ClienteGymRepository
        extends JpaRepository<ClienteGym, Long> {

    List<ClienteGym> findAllByTenantIdOrderByNombreAsc(
            Long tenantId
    );

    Optional<ClienteGym> findByIdAndTenantId(
            Long id,
            Long tenantId
    );

    Optional<ClienteGym>
    findByTenantIdAndNumeroClienteIgnoreCase(
            Long tenantId,
            String numeroCliente
    );

    boolean existsByTenantIdAndNumeroClienteIgnoreCase(
            Long tenantId,
            String numeroCliente
    );

    long countByTenantId(
            Long tenantId
    );

    /*
     * Busca clientes activos que no tienen ninguna membresía
     * activa y vigente.
     *
     * La consulta siempre filtra por tenant_id para evitar
     * mezclar clientes de diferentes gimnasios.
     */
    @Query("""
        SELECT c
        FROM ClienteGym c
        WHERE c.tenant.id = :tenantId
          AND c.estado =
              com.abarrote.abarroteapi.gym.domain.EstadoClienteGym.ACTIVO
          AND NOT EXISTS (
              SELECT m.id
              FROM Membresia m
              WHERE m.tenant.id = :tenantId
                AND m.cliente.id = c.id
                AND m.estado = :estado
                AND m.fechaFin >= :hoy
          )
        ORDER BY
            c.nombre ASC,
            c.apellidoPaterno ASC,
            c.apellidoMaterno ASC
        """)
    List<ClienteGym> buscarSinMembresiaActiva(
            @Param("tenantId")
            Long tenantId,

            @Param("estado")
            EstadoMembresia estado,

            @Param("hoy")
            LocalDate hoy
    );
}
