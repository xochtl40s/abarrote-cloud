package com.abarrote.abarroteapi.gym.web;

import com.abarrote.abarroteapi.gym.dto.ClienteGymRequest;
import com.abarrote.abarroteapi.gym.dto.ClienteGymResponse;
import com.abarrote.abarroteapi.gym.dto.MembresiaRequest;
import com.abarrote.abarroteapi.gym.dto.MembresiaResponse;
import com.abarrote.abarroteapi.gym.dto.PagoMembresiaRequest;
import com.abarrote.abarroteapi.gym.dto.PagoMembresiaResponse;
import com.abarrote.abarroteapi.gym.dto.PlanMembresiaRequest;
import com.abarrote.abarroteapi.gym.dto.PlanMembresiaResponse;
import com.abarrote.abarroteapi.gym.service.GymService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/gym/{tenantSlug}")
public class GymController {

    private final GymService gymService;

    public GymController(GymService gymService) {
        this.gymService = gymService;
    }

    @PostMapping("/clientes")
    public ResponseEntity<ClienteGymResponse>
        crearCliente(
            @PathVariable String tenantSlug,
            @Valid
            @RequestBody ClienteGymRequest request
        ) {

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                gymService.crearCliente(
                    tenantSlug,
                    request
                )
            );
    }

    @GetMapping("/clientes")
    public ResponseEntity<List<ClienteGymResponse>>
        listarClientes(
            @PathVariable String tenantSlug
        ) {

        return ResponseEntity.ok(
            gymService.listarClientes(tenantSlug)
        );
    }

    @PostMapping("/planes")
    public ResponseEntity<PlanMembresiaResponse>
        crearPlan(
            @PathVariable String tenantSlug,
            @Valid
            @RequestBody PlanMembresiaRequest request
        ) {

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                gymService.crearPlan(
                    tenantSlug,
                    request
                )
            );
    }

    @GetMapping("/planes")
    public ResponseEntity<List<PlanMembresiaResponse>>
        listarPlanes(
            @PathVariable String tenantSlug
        ) {

        return ResponseEntity.ok(
            gymService.listarPlanes(tenantSlug)
        );
    }

    @PostMapping("/membresias")
    public ResponseEntity<MembresiaResponse>
        crearMembresia(
            @PathVariable String tenantSlug,
            @Valid
            @RequestBody MembresiaRequest request
        ) {

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                gymService.crearMembresia(
                    tenantSlug,
                    request
                )
            );
    }

    @PostMapping("/pagos")
    public ResponseEntity<PagoMembresiaResponse>
        registrarPago(
            @PathVariable String tenantSlug,
            @Valid
            @RequestBody PagoMembresiaRequest request
        ) {

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                gymService.registrarPago(
                    tenantSlug,
                    request
                )
            );
    }
}
