package com.abarrote.abarroteapi.gym.service;

import com.abarrote.abarroteapi.gym.domain.ClienteGym;
import com.abarrote.abarroteapi.gym.domain.EstadoMembresia;
import com.abarrote.abarroteapi.gym.domain.GymProducto;
import com.abarrote.abarroteapi.gym.domain.Membresia;
import com.abarrote.abarroteapi.gym.repository.ClienteGymRepository;
import com.abarrote.abarroteapi.gym.repository.GymProductoRepository;
import com.abarrote.abarroteapi.gym.repository.MembresiaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class GymDashboardService {

    private final ClienteGymRepository clienteRepository;

    private final MembresiaRepository membresiaRepository;

    private final GymProductoRepository productoRepository;

    public GymDashboardService(
        ClienteGymRepository clienteRepository,
        MembresiaRepository membresiaRepository,
        GymProductoRepository productoRepository
    ) {
        this.clienteRepository = clienteRepository;
        this.membresiaRepository = membresiaRepository;
        this.productoRepository = productoRepository;
    }

    public List<Membresia> proximasAVencer(
        Long tenantId
    ) {
        LocalDate hoy = LocalDate.now();

        return membresiaRepository
            .findAllByTenantIdAndEstadoAndFechaFinBetweenOrderByFechaFinAsc(
                tenantId,
                EstadoMembresia.ACTIVA,
                hoy,
                hoy.plusDays(5)
            );
    }

    public List<ClienteGym> clientesSinMembresia(
        Long tenantId
    ) {
        return clienteRepository
            .buscarSinMembresiaActiva(
                tenantId,
                EstadoMembresia.ACTIVA,
                LocalDate.now()
            );
    }

    public List<GymProducto> productosStockBajo(
        Long tenantId
    ) {
        return productoRepository
            .buscarProductosStockBajo(tenantId);
    }

    /*
     * Conteo de membresías que vencen:
     *
     * Hoy, mañana, en 2, 3, 4 y 5 días.
     */
    public List<Long> conteoVencimientosPorDia(
        Long tenantId
    ) {
        LocalDate hoy = LocalDate.now();

        List<Membresia> membresias =
            proximasAVencer(tenantId);

        List<Long> resultado =
            new ArrayList<>();

        for (int dia = 0; dia <= 5; dia++) {
            LocalDate fecha = hoy.plusDays(dia);

            long total = membresias
                .stream()
                .filter(
                    membresia ->
                        fecha.equals(
                            membresia.getFechaFin()
                        )
                )
                .count();

            resultado.add(total);
        }

        return resultado;
    }

    public long totalClientes(
        Long tenantId
    ) {
        return clienteRepository.countByTenantId(
            tenantId
        );
    }

    public long membresiasActivas(
        Long tenantId
    ) {
        return membresiaRepository
            .countByTenantIdAndEstado(
                tenantId,
                EstadoMembresia.ACTIVA
            );
    }

    public long clientesSinMembresiaTotal(
        Long tenantId
    ) {
        return clientesSinMembresia(
            tenantId
        ).size();
    }
}
