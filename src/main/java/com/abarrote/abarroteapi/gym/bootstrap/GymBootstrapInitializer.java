package com.abarrote.abarroteapi.gym.bootstrap;

import com.abarrote.abarroteapi.gym.domain.ClienteGym;
import com.abarrote.abarroteapi.gym.domain.EstadoClienteGym;
import com.abarrote.abarroteapi.gym.domain.EstadoMembresia;
import com.abarrote.abarroteapi.gym.domain.Membresia;
import com.abarrote.abarroteapi.gym.domain.MetodoPagoGym;
import com.abarrote.abarroteapi.gym.domain.PagoMembresia;
import com.abarrote.abarroteapi.gym.domain.PlanMembresia;
import com.abarrote.abarroteapi.gym.repository.ClienteGymRepository;
import com.abarrote.abarroteapi.gym.repository.MembresiaRepository;
import com.abarrote.abarroteapi.gym.repository.PagoMembresiaRepository;
import com.abarrote.abarroteapi.gym.repository.PlanMembresiaRepository;
import com.abarrote.abarroteapi.multitenant.domain.Tenant;
import com.abarrote.abarroteapi.multitenant.repository.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@ConditionalOnProperty(
    name = "commerce.bootstrap.gym.enabled",
    havingValue = "true"
)
public class GymBootstrapInitializer
    implements ApplicationRunner {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(
            GymBootstrapInitializer.class
        );

    private final TenantRepository tenantRepository;
    private final ClienteGymRepository clienteRepository;
    private final PlanMembresiaRepository planRepository;
    private final MembresiaRepository membresiaRepository;
    private final PagoMembresiaRepository pagoRepository;

    public GymBootstrapInitializer(
        TenantRepository tenantRepository,
        ClienteGymRepository clienteRepository,
        PlanMembresiaRepository planRepository,
        MembresiaRepository membresiaRepository,
        PagoMembresiaRepository pagoRepository
    ) {
        this.tenantRepository = tenantRepository;
        this.clienteRepository = clienteRepository;
        this.planRepository = planRepository;
        this.membresiaRepository = membresiaRepository;
        this.pagoRepository = pagoRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Tenant tenant = tenantRepository
            .findBySlugIgnoreCaseAndActivoTrue(
                "gym-titan"
            )
            .orElse(null);

        if (tenant == null) {
            LOGGER.warn(
                "No existe gym-titan. No se ejecuta bootstrap Gym."
            );
            return;
        }

        crearPlanSiNoExiste(
            tenant,
            "Mensual",
            "Acceso general durante 30 días",
            30,
            new BigDecimal("500.00")
        );

        crearPlanSiNoExiste(
            tenant,
            "Trimestral",
            "Acceso general durante 90 días",
            90,
            new BigDecimal("1350.00")
        );

        crearPlanSiNoExiste(
            tenant,
            "Anual",
            "Acceso general durante 365 días",
            365,
            new BigDecimal("4800.00")
        );

        ClienteGym cliente =
            crearClienteSiNoExiste(tenant);

        crearMembresiaSiNoExiste(
            tenant,
            cliente
        );

        LOGGER.info(
            "Bootstrap de Commerce Cloud Gym terminado"
        );
    }

    private void crearPlanSiNoExiste(
        Tenant tenant,
        String nombre,
        String descripcion,
        int duracionDias,
        BigDecimal precio
    ) {
        if (
            planRepository
                .existsByTenantIdAndNombreIgnoreCase(
                    tenant.getId(),
                    nombre
                )
        ) {
            return;
        }

        PlanMembresia plan = new PlanMembresia();
        plan.setTenant(tenant);
        plan.setNombre(nombre);
        plan.setDescripcion(descripcion);
        plan.setDuracionDias(duracionDias);
        plan.setPrecio(precio);
        plan.setActivo(true);

        planRepository.save(plan);
    }

    private ClienteGym crearClienteSiNoExiste(
        Tenant tenant
    ) {
        return clienteRepository
            .findByTenantIdAndNumeroClienteIgnoreCase(
                tenant.getId(),
                "GYM-000001"
            )
            .orElseGet(
                () -> {
                    ClienteGym cliente =
                        new ClienteGym();

                    cliente.setTenant(tenant);
                    cliente.setNumeroCliente(
                        "GYM-000001"
                    );
                    cliente.setNombre("Cliente");
                    cliente.setApellidoPaterno(
                        "Demostración"
                    );
                    cliente.setTelefono(
                        "5512345678"
                    );
                    cliente.setEmail(
                        "cliente.demo@gym.local"
                    );
                    cliente.setEstado(
                        EstadoClienteGym.ACTIVO
                    );

                    return clienteRepository.save(
                        cliente
                    );
                }
            );
    }

    private void crearMembresiaSiNoExiste(
        Tenant tenant,
        ClienteGym cliente
    ) {
        List<Membresia> existentes =
            membresiaRepository
                .findAllByTenantIdAndClienteIdOrderByFechaInicioDesc(
                    tenant.getId(),
                    cliente.getId()
                );

        if (!existentes.isEmpty()) {
            return;
        }

        PlanMembresia plan = planRepository
            .findAllByTenantIdAndActivoTrueOrderByPrecioAsc(
                tenant.getId()
            )
            .stream()
            .filter(
                item -> item
                    .getNombre()
                    .equalsIgnoreCase("Mensual")
            )
            .findFirst()
            .orElseThrow();

        LocalDate inicio = LocalDate.now();

        Membresia membresia = new Membresia();
        membresia.setTenant(tenant);
        membresia.setCliente(cliente);
        membresia.setPlan(plan);
        membresia.setFechaInicio(inicio);
        membresia.setFechaFin(
            inicio.plusDays(plan.getDuracionDias())
        );
        membresia.setPrecioContratado(
            plan.getPrecio()
        );
        membresia.setEstado(
            EstadoMembresia.ACTIVA
        );

        Membresia guardada =
            membresiaRepository.save(membresia);

        PagoMembresia pago =
            new PagoMembresia();

        pago.setTenant(tenant);
        pago.setMembresia(guardada);
        pago.setImporte(plan.getPrecio());
        pago.setMetodoPago(
            MetodoPagoGym.EFECTIVO
        );
        pago.setReferencia(
            "BOOTSTRAP-DEMO"
        );
        pago.setObservaciones(
            "Pago inicial de demostración"
        );

        pagoRepository.save(pago);
    }
}
