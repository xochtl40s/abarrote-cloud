package com.abarrote.abarroteapi.gym.service;

import com.abarrote.abarroteapi.gym.domain.ClienteGym;
import com.abarrote.abarroteapi.gym.domain.EstadoClienteGym;
import com.abarrote.abarroteapi.gym.domain.EstadoMembresia;
import com.abarrote.abarroteapi.gym.domain.Membresia;
import com.abarrote.abarroteapi.gym.domain.PagoMembresia;
import com.abarrote.abarroteapi.gym.domain.PlanMembresia;
import com.abarrote.abarroteapi.gym.dto.ClienteGymRequest;
import com.abarrote.abarroteapi.gym.dto.ClienteGymResponse;
import com.abarrote.abarroteapi.gym.dto.MembresiaRequest;
import com.abarrote.abarroteapi.gym.dto.MembresiaResponse;
import com.abarrote.abarroteapi.gym.dto.PagoMembresiaRequest;
import com.abarrote.abarroteapi.gym.dto.PagoMembresiaResponse;
import com.abarrote.abarroteapi.gym.dto.PlanMembresiaRequest;
import com.abarrote.abarroteapi.gym.dto.PlanMembresiaResponse;
import com.abarrote.abarroteapi.gym.repository.ClienteGymRepository;
import com.abarrote.abarroteapi.gym.repository.MembresiaRepository;
import com.abarrote.abarroteapi.gym.repository.PagoMembresiaRepository;
import com.abarrote.abarroteapi.gym.repository.PlanMembresiaRepository;
import com.abarrote.abarroteapi.multitenant.domain.Tenant;
import com.abarrote.abarroteapi.multitenant.domain.TipoNegocio;
import com.abarrote.abarroteapi.multitenant.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class GymService {

    private final TenantRepository tenantRepository;
    private final ClienteGymRepository clienteRepository;
    private final PlanMembresiaRepository planRepository;
    private final MembresiaRepository membresiaRepository;
    private final PagoMembresiaRepository pagoRepository;

    public GymService(
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

    public ClienteGymResponse crearCliente(
        String tenantSlug,
        ClienteGymRequest request
    ) {
        Tenant tenant = obtenerGym(tenantSlug);

        if (
            clienteRepository
                .existsByTenantIdAndNumeroClienteIgnoreCase(
                    tenant.getId(),
                    request.numeroCliente()
                )
        ) {
            throw new IllegalArgumentException(
                "El número de cliente ya existe"
            );
        }

        ClienteGym cliente = new ClienteGym();
        cliente.setTenant(tenant);
        cliente.setNumeroCliente(
            request.numeroCliente().trim()
        );
        cliente.setNombre(request.nombre().trim());
        cliente.setApellidoPaterno(
            request.apellidoPaterno()
        );
        cliente.setApellidoMaterno(
            request.apellidoMaterno()
        );
        cliente.setTelefono(request.telefono());
        cliente.setEmail(request.email());
        cliente.setFechaNacimiento(
            request.fechaNacimiento()
        );
        cliente.setContactoEmergencia(
            request.contactoEmergencia()
        );
        cliente.setTelefonoEmergencia(
            request.telefonoEmergencia()
        );
        cliente.setEstado(EstadoClienteGym.ACTIVO);

        return clienteResponse(
            clienteRepository.save(cliente)
        );
    }

    @Transactional(readOnly = true)
    public List<ClienteGymResponse> listarClientes(
        String tenantSlug
    ) {
        Tenant tenant = obtenerGym(tenantSlug);

        return clienteRepository
            .findAllByTenantIdOrderByNombreAsc(
                tenant.getId()
            )
            .stream()
            .map(this::clienteResponse)
            .toList();
    }

    public PlanMembresiaResponse crearPlan(
        String tenantSlug,
        PlanMembresiaRequest request
    ) {
        Tenant tenant = obtenerGym(tenantSlug);

        if (
            planRepository
                .existsByTenantIdAndNombreIgnoreCase(
                    tenant.getId(),
                    request.nombre()
                )
        ) {
            throw new IllegalArgumentException(
                "Ya existe un plan con ese nombre"
            );
        }

        PlanMembresia plan = new PlanMembresia();
        plan.setTenant(tenant);
        plan.setNombre(request.nombre().trim());
        plan.setDescripcion(request.descripcion());
        plan.setDuracionDias(request.duracionDias());
        plan.setPrecio(request.precio());
        plan.setActivo(true);

        return planResponse(
            planRepository.save(plan)
        );
    }

    @Transactional(readOnly = true)
    public List<PlanMembresiaResponse> listarPlanes(
        String tenantSlug
    ) {
        Tenant tenant = obtenerGym(tenantSlug);

        return planRepository
            .findAllByTenantIdAndActivoTrueOrderByPrecioAsc(
                tenant.getId()
            )
            .stream()
            .map(this::planResponse)
            .toList();
    }

    public MembresiaResponse crearMembresia(
        String tenantSlug,
        MembresiaRequest request
    ) {
        Tenant tenant = obtenerGym(tenantSlug);

        ClienteGym cliente = clienteRepository
            .findByIdAndTenantId(
                request.clienteId(),
                tenant.getId()
            )
            .orElseThrow(
                () -> new IllegalArgumentException(
                    "Cliente no encontrado"
                )
            );

        PlanMembresia plan = planRepository
            .findByIdAndTenantId(
                request.planId(),
                tenant.getId()
            )
            .orElseThrow(
                () -> new IllegalArgumentException(
                    "Plan no encontrado"
                )
            );

        LocalDate fechaInicio =
            request.fechaInicio() != null
                ? request.fechaInicio()
                : LocalDate.now();

        Membresia membresia = new Membresia();
        membresia.setTenant(tenant);
        membresia.setCliente(cliente);
        membresia.setPlan(plan);
        membresia.setFechaInicio(fechaInicio);
        membresia.setFechaFin(
            fechaInicio.plusDays(
                plan.getDuracionDias()
            )
        );
        membresia.setPrecioContratado(
            plan.getPrecio()
        );
        membresia.setEstado(
            EstadoMembresia.PENDIENTE
        );

        return membresiaResponse(
            membresiaRepository.save(membresia)
        );
    }

    public PagoMembresiaResponse registrarPago(
        String tenantSlug,
        PagoMembresiaRequest request
    ) {
        Tenant tenant = obtenerGym(tenantSlug);

        Membresia membresia = membresiaRepository
            .findByIdAndTenantId(
                request.membresiaId(),
                tenant.getId()
            )
            .orElseThrow(
                () -> new IllegalArgumentException(
                    "Membresía no encontrada"
                )
            );

        PagoMembresia pago = new PagoMembresia();
        pago.setTenant(tenant);
        pago.setMembresia(membresia);
        pago.setImporte(request.importe());
        pago.setMetodoPago(request.metodoPago());
        pago.setReferencia(request.referencia());
        pago.setObservaciones(
            request.observaciones()
        );

        PagoMembresia guardado =
            pagoRepository.save(pago);

        membresia.setEstado(EstadoMembresia.ACTIVA);
        membresiaRepository.save(membresia);

        return pagoResponse(guardado);
    }

    private Tenant obtenerGym(String tenantSlug) {
        Tenant tenant = tenantRepository
            .findBySlugIgnoreCaseAndActivoTrue(
                tenantSlug
            )
            .orElseThrow(
                () -> new IllegalArgumentException(
                    "Tenant no encontrado o inactivo"
                )
            );

        if (tenant.getTipoNegocio() != TipoNegocio.GYM) {
            throw new IllegalArgumentException(
                "El tenant no pertenece al vertical GYM"
            );
        }

        return tenant;
    }

    private ClienteGymResponse clienteResponse(
        ClienteGym cliente
    ) {
        String nombreCompleto = String.join(
            " ",
            texto(cliente.getNombre()),
            texto(cliente.getApellidoPaterno()),
            texto(cliente.getApellidoMaterno())
        ).trim();

        return new ClienteGymResponse(
            cliente.getId(),
            cliente.getNumeroCliente(),
            nombreCompleto,
            cliente.getTelefono(),
            cliente.getEmail(),
            cliente.getFechaNacimiento(),
            cliente.getEstado()
        );
    }

    private PlanMembresiaResponse planResponse(
        PlanMembresia plan
    ) {
        return new PlanMembresiaResponse(
            plan.getId(),
            plan.getNombre(),
            plan.getDescripcion(),
            plan.getDuracionDias(),
            plan.getPrecio(),
            plan.isActivo()
        );
    }

    private MembresiaResponse membresiaResponse(
        Membresia membresia
    ) {
        return new MembresiaResponse(
            membresia.getId(),
            membresia.getCliente().getId(),
            membresia.getCliente().getNombre(),
            membresia.getPlan().getId(),
            membresia.getPlan().getNombre(),
            membresia.getFechaInicio(),
            membresia.getFechaFin(),
            membresia.getPrecioContratado(),
            membresia.getEstado()
        );
    }

    private PagoMembresiaResponse pagoResponse(
        PagoMembresia pago
    ) {
        return new PagoMembresiaResponse(
            pago.getId(),
            pago.getMembresia().getId(),
            pago.getImporte(),
            pago.getMetodoPago(),
            pago.getReferencia(),
            pago.getFechaPago()
        );
    }

    private String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
