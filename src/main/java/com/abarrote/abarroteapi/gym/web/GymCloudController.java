package com.abarrote.abarroteapi.gym.web;

import com.abarrote.abarroteapi.gym.domain.EstadoMembresia;
import com.abarrote.abarroteapi.gym.dto.ClienteGymRequest;
import com.abarrote.abarroteapi.gym.dto.GymVentaRequest;
import com.abarrote.abarroteapi.gym.dto.GymVentaResponse;
import com.abarrote.abarroteapi.gym.dto.MembresiaRequest;
import com.abarrote.abarroteapi.gym.dto.PagoMembresiaRequest;
import com.abarrote.abarroteapi.gym.repository.ClienteGymRepository;
import com.abarrote.abarroteapi.gym.repository.GymProductoRepository;
import com.abarrote.abarroteapi.gym.repository.GymVentaRepository;
import com.abarrote.abarroteapi.gym.repository.MembresiaRepository;
import com.abarrote.abarroteapi.gym.repository.PlanMembresiaRepository;
import com.abarrote.abarroteapi.gym.service.GymComercialService;
import com.abarrote.abarroteapi.gym.service.GymDashboardService;
import com.abarrote.abarroteapi.gym.service.GymService;
import com.abarrote.abarroteapi.security.CommerceUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
public class GymCloudController {

    private final GymComercialService comercialService;

    private final GymDashboardService dashboardService;

    private final GymService gymService;

    private final GymProductoRepository productoRepository;

    private final GymVentaRepository ventaRepository;

    private final ClienteGymRepository clienteRepository;

    private final PlanMembresiaRepository planRepository;

    private final MembresiaRepository membresiaRepository;

    public GymCloudController(
        GymComercialService comercialService,
        GymDashboardService dashboardService,
        GymService gymService,
        GymProductoRepository productoRepository,
        GymVentaRepository ventaRepository,
        ClienteGymRepository clienteRepository,
        PlanMembresiaRepository planRepository,
        MembresiaRepository membresiaRepository
    ) {
        this.comercialService = comercialService;
        this.dashboardService = dashboardService;
        this.gymService = gymService;
        this.productoRepository = productoRepository;
        this.ventaRepository = ventaRepository;
        this.clienteRepository = clienteRepository;
        this.planRepository = planRepository;
        this.membresiaRepository = membresiaRepository;
    }

    @GetMapping("/gym/dashboard")
    public String dashboard(
        @AuthenticationPrincipal
        CommerceUserPrincipal principal,
        Model model
    ) {
        validarGym(principal);

        Long tenantId = principal.getTenantId();

        model.addAttribute(
            "principal",
            principal
        );

        model.addAttribute(
            "totalProductos",
            productoRepository
                .countByTenantIdAndActivoTrue(
                    tenantId
                )
        );

        model.addAttribute(
            "totalVentas",
            ventaRepository
                .countByTenantId(tenantId)
        );

        model.addAttribute(
            "totalClientes",
            clienteRepository
                .findAllByTenantIdOrderByNombreAsc(
                    tenantId
                )
                .size()
        );

        model.addAttribute(
            "membresiasActivas",
            membresiaRepository
                .countByTenantIdAndEstado(
                    tenantId,
                    EstadoMembresia.ACTIVA
                )
        );

        model.addAttribute(
            "membresiasVencidas",
            membresiaRepository
                .countByTenantIdAndFechaFinBeforeAndEstado(
                    tenantId,
                    LocalDate.now(),
                    EstadoMembresia.ACTIVA
                )
        );

        model.addAttribute(
            "proximasAVencer",
            dashboardService.proximasAVencer(
                tenantId
            )
        );

        model.addAttribute(
            "clientesSinMembresia",
            dashboardService.clientesSinMembresia(
                tenantId
            )
        );

        model.addAttribute(
            "productosStockBajo",
            dashboardService.productosStockBajo(
                tenantId
            )
        );

        model.addAttribute(
            "vencimientosDatos",
            dashboardService.conteoVencimientosPorDia(
                tenantId
            )
        );

        model.addAttribute(
            "clientesConMembresia",
            dashboardService.membresiasActivas(
                tenantId
            )
        );

        model.addAttribute(
            "clientesSinMembresiaTotal",
            dashboardService.clientesSinMembresiaTotal(
                tenantId
            )
        );

        return "gym/dashboard";
    }

    @GetMapping("/gym/productos")
    public String productos(
        @AuthenticationPrincipal
        CommerceUserPrincipal principal,
        Model model
    ) {
        validarGym(principal);

        model.addAttribute(
            "productos",
            comercialService.listarProductos(
                principal.getTenantId()
            )
        );

        return "gym/productos";
    }

    @PostMapping("/gym/productos")
    public String crearProducto(
        @AuthenticationPrincipal
        CommerceUserPrincipal principal,

        @RequestParam String codigo,

        @RequestParam String nombre,

        @RequestParam(required = false)
        String descripcion,

        @RequestParam(required = false)
        String categoria,

        @RequestParam BigDecimal precio,

        @RequestParam(required = false)
        BigDecimal costo,

        @RequestParam(defaultValue = "0")
        Integer existencia,

        @RequestParam(defaultValue = "0")
        Integer stockMinimo
    ) {
        validarGym(principal);

        comercialService.crearProducto(
            principal.getTenantId(),
            codigo,
            nombre,
            descripcion,
            categoria,
            precio,
            costo,
            existencia,
            stockMinimo
        );

        return "redirect:/gym/productos?creado";
    }

    @PostMapping("/gym/productos/existencia")
    public String actualizarExistencia(
        @AuthenticationPrincipal
        CommerceUserPrincipal principal,

        @RequestParam Long productoId,

        @RequestParam Integer existencia
    ) {
        validarGym(principal);

        comercialService.actualizarExistencia(
            principal.getTenantId(),
            productoId,
            existencia
        );

        return "redirect:/gym/productos?existenciaActualizada";
    }

    @GetMapping("/gym/pos")
    public String pos(
        @AuthenticationPrincipal
        CommerceUserPrincipal principal,
        Model model
    ) {
        validarGym(principal);

        model.addAttribute(
            "productos",
            comercialService.listarProductos(
                principal.getTenantId()
            )
        );

        return "gym/pos";
    }

    @PostMapping("/gym/api/ventas")
    @ResponseBody
    public ResponseEntity<GymVentaResponse>
        registrarVenta(
            @AuthenticationPrincipal
            CommerceUserPrincipal principal,

            @Valid
            @RequestBody GymVentaRequest request
        ) {

        validarGym(principal);

        GymVentaResponse response =
            comercialService.registrarVenta(
                principal.getTenantId(),
                principal.getUsername(),
                request
            );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }

    @GetMapping("/gym/ventas")
    public String ventas(
        @AuthenticationPrincipal
        CommerceUserPrincipal principal,
        Model model
    ) {
        validarGym(principal);

        model.addAttribute(
            "ventas",
            comercialService.listarVentas(
                principal.getTenantId()
            )
        );

        return "gym/ventas";
    }

    @GetMapping("/gym/clientes")
    public String clientes(
        @AuthenticationPrincipal
        CommerceUserPrincipal principal,
        Model model
    ) {
        validarGym(principal);

        model.addAttribute(
            "clientes",
            clienteRepository
                .findAllByTenantIdOrderByNombreAsc(
                    principal.getTenantId()
                )
        );

        return "gym/clientes";
    }

    @PostMapping("/gym/clientes")
    public String crearCliente(
        @AuthenticationPrincipal
        CommerceUserPrincipal principal,

        @RequestParam String numeroCliente,

        @RequestParam String nombre,

        @RequestParam(required = false)
        String apellidoPaterno,

        @RequestParam(required = false)
        String apellidoMaterno,

        @RequestParam(required = false)
        String telefono,

        @RequestParam(required = false)
        String email,

        @RequestParam(required = false)
        LocalDate fechaNacimiento,

        @RequestParam(required = false)
        String contactoEmergencia,

        @RequestParam(required = false)
        String telefonoEmergencia
    ) {
        validarGym(principal);

        gymService.crearCliente(
            principal.getTenantSlug(),
            new ClienteGymRequest(
                numeroCliente,
                nombre,
                apellidoPaterno,
                apellidoMaterno,
                telefono,
                email,
                fechaNacimiento,
                contactoEmergencia,
                telefonoEmergencia
            )
        );

        return "redirect:/gym/clientes?creado";
    }

    @GetMapping("/gym/membresias")
    public String membresias(
        @AuthenticationPrincipal
        CommerceUserPrincipal principal,
        Model model
    ) {
        validarGym(principal);

        Long tenantId = principal.getTenantId();

        model.addAttribute(
            "clientes",
            clienteRepository
                .findAllByTenantIdOrderByNombreAsc(
                    tenantId
                )
        );

        model.addAttribute(
            "planes",
            planRepository
                .findAllByTenantIdAndActivoTrueOrderByPrecioAsc(
                    tenantId
                )
        );

        model.addAttribute(
            "membresias",
            membresiaRepository
                .findAllByTenantIdOrderByFechaCreacionDesc(
                    tenantId
                )
        );

        return "gym/membresias";
    }

    @PostMapping("/gym/membresias")
    public String crearMembresia(
        @AuthenticationPrincipal
        CommerceUserPrincipal principal,

        @RequestParam Long clienteId,

        @RequestParam Long planId,

        @RequestParam(required = false)
        LocalDate fechaInicio
    ) {
        validarGym(principal);

        gymService.crearMembresia(
            principal.getTenantSlug(),
            new MembresiaRequest(
                clienteId,
                planId,
                fechaInicio
            )
        );

        return "redirect:/gym/membresias?creada";
    }

    @PostMapping("/gym/membresias/pagar")
    public String pagarMembresia(
        @AuthenticationPrincipal
        CommerceUserPrincipal principal,

        @RequestParam Long membresiaId,

        @RequestParam BigDecimal importe,

        @RequestParam
        com.abarrote.abarroteapi.gym.domain.MetodoPagoGym
            metodoPago,

        @RequestParam(required = false)
        String referencia
    ) {
        validarGym(principal);

        gymService.registrarPago(
            principal.getTenantSlug(),
            new PagoMembresiaRequest(
                membresiaId,
                importe,
                metodoPago,
                referencia,
                "Pago registrado desde Gym Cloud"
            )
        );

        return "redirect:/gym/membresias?pagoRegistrado";
    }

    private void validarGym(
        CommerceUserPrincipal principal
    ) {
        if (principal == null) {
            throw new IllegalStateException(
                "Sesión no disponible"
            );
        }

        if (!principal.esGym()) {
            throw new IllegalArgumentException(
                "El usuario no pertenece a Gym Cloud"
            );
        }
    }
}
