package com.abarrote.abarroteapi.saas.service.impl;

import com.abarrote.abarroteapi.saas.dto.SaasProspectoRequest;
import com.abarrote.abarroteapi.saas.dto.SaasProspectoResponse;
import com.abarrote.abarroteapi.saas.entity.SaasPlan;
import com.abarrote.abarroteapi.saas.entity.SaasProspecto;
import com.abarrote.abarroteapi.saas.repository.SaasPlanRepository;
import com.abarrote.abarroteapi.saas.repository.SaasProspectoRepository;
import com.abarrote.abarroteapi.saas.service.SaasProspectoService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@Transactional
public class SaasProspectoServiceImpl
        implements SaasProspectoService {

    private static final Pattern CORREO_PATTERN = Pattern.compile(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private final SaasProspectoRepository prospectoRepository;
    private final SaasPlanRepository planRepository;

    public SaasProspectoServiceImpl(
        SaasProspectoRepository prospectoRepository,
        SaasPlanRepository planRepository
    ) {
        this.prospectoRepository = prospectoRepository;
        this.planRepository = planRepository;
    }

    @Override
    public SaasProspectoResponse registrar(
        SaasProspectoRequest request
    ) {
        validarRequest(request);

        String codigoPlan = request
            .getPlanCodigo()
            .trim()
            .toUpperCase(Locale.ROOT);

        SaasPlan plan = planRepository
            .findByCodigoIgnoreCase(codigoPlan)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El plan solicitado no existe: " + codigoPlan
            ));

        if (!Boolean.TRUE.equals(plan.getActivo())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El plan solicitado no está disponible"
            );
        }

        SaasProspecto prospecto = new SaasProspecto();

        prospecto.setFolio(generarFolio());
        prospecto.setNombreNegocio(request.getNombreNegocio());
        prospecto.setPropietario(request.getPropietario());
        prospecto.setCorreo(request.getCorreo());
        prospecto.setWhatsapp(
            normalizarTelefono(request.getWhatsapp())
        );
        prospecto.setCiudad(request.getCiudad());
        prospecto.setTipoNegocio(request.getTipoNegocio());
        prospecto.setNumeroMesas(request.getNumeroMesas());
        prospecto.setPlanSolicitadoId(plan.getId());
        prospecto.setEstado("NUEVO");
        prospecto.setOrigen("LANDING");
        prospecto.setObservaciones(request.getObservaciones());

        SaasProspecto guardado =
            prospectoRepository.save(prospecto);

        return SaasProspectoResponse.desdeEntidad(
            guardado,
            plan
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaasProspectoResponse> listarTodos() {
        return prospectoRepository
            .findVisiblesOrderByFechaCreacionDesc()
            .stream()
            .map(this::convertirRespuesta)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SaasProspectoResponse obtenerPorFolio(String folio) {
        if (folio == null || folio.isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El folio es obligatorio"
            );
        }

        SaasProspecto prospecto = prospectoRepository
            .findByFolioIgnoreCase(folio.trim())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No existe el prospecto con folio: " + folio
            ));

        return convertirRespuesta(prospecto);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarNuevos() {
        return prospectoRepository.countByEstadoIgnoreCase(
            "NUEVO"
        );
    }

    private SaasProspectoResponse convertirRespuesta(
        SaasProspecto prospecto
    ) {
        SaasPlan plan = planRepository
            .findById(prospecto.getPlanSolicitadoId())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "El prospecto tiene un plan inexistente"
            ));

        return SaasProspectoResponse.desdeEntidad(
            prospecto,
            plan
        );
    }

    private void validarRequest(SaasProspectoRequest request) {
        if (request == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La solicitud es obligatoria"
            );
        }

        validarObligatorio(
            request.getNombreNegocio(),
            "El nombre del negocio es obligatorio"
        );

        validarObligatorio(
            request.getPropietario(),
            "El propietario es obligatorio"
        );

        validarObligatorio(
            request.getCorreo(),
            "El correo es obligatorio"
        );

        validarObligatorio(
            request.getWhatsapp(),
            "El WhatsApp es obligatorio"
        );

        validarObligatorio(
            request.getCiudad(),
            "La ciudad es obligatoria"
        );

        validarObligatorio(
            request.getTipoNegocio(),
            "El tipo de negocio es obligatorio"
        );

        validarObligatorio(
            request.getPlanCodigo(),
            "El plan es obligatorio"
        );

        String correo = request.getCorreo().trim();

        if (!CORREO_PATTERN.matcher(correo).matches()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El correo electrónico no tiene un formato válido"
            );
        }

        if (request.getNumeroMesas() != null
                && request.getNumeroMesas() < 0) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El número de mesas no puede ser negativo"
            );
        }

        String telefono = normalizarTelefono(
            request.getWhatsapp()
        );

        if (telefono.length() < 10 || telefono.length() > 15) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El WhatsApp debe contener entre 10 y 15 dígitos"
            );
        }
    }

    private void validarObligatorio(
        String valor,
        String mensaje
    ) {
        if (valor == null || valor.isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                mensaje
            );
        }
    }

    private String normalizarTelefono(String telefono) {
        return telefono == null
            ? ""
            : telefono.replaceAll("[^0-9]", "");
    }

    private String generarFolio() {
        String fecha = LocalDate.now().format(
            DateTimeFormatter.BASIC_ISO_DATE
        );

        String folio;

        do {
            String aleatorio = UUID
                .randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 6)
                .toUpperCase(Locale.ROOT);

            folio = "CC-" + fecha + "-" + aleatorio;

        } while (
            prospectoRepository.existsByFolioIgnoreCase(folio)
        );

        return folio;
    }
}
