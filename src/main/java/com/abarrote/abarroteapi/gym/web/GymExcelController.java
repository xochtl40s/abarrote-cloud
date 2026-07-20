package com.abarrote.abarroteapi.gym.web;

import com.abarrote.abarroteapi.gym.service.GymExcelImportResult;
import com.abarrote.abarroteapi.gym.service.GymExcelService;
import com.abarrote.abarroteapi.security.CommerceUserPrincipal;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class GymExcelController {

    private static final String XLSX =
        "application/vnd.openxmlformats-officedocument"
            + ".spreadsheetml.sheet";

    private final GymExcelService excelService;

    public GymExcelController(
        GymExcelService excelService
    ) {
        this.excelService = excelService;
    }

    @GetMapping("/gym/plantillas/productos")
    public ResponseEntity<byte[]>
        descargarProductos(
            @AuthenticationPrincipal
            CommerceUserPrincipal principal
        ) {

        validarGym(principal);

        return descargar(
            excelService.plantillaProductos(),
            "GymCloud_Productos_Muestra.xlsx"
        );
    }

    @GetMapping("/gym/plantillas/clientes")
    public ResponseEntity<byte[]>
        descargarClientes(
            @AuthenticationPrincipal
            CommerceUserPrincipal principal
        ) {

        validarGym(principal);

        return descargar(
            excelService.plantillaClientes(),
            "GymCloud_Clientes_Muestra.xlsx"
        );
    }

    @PostMapping("/gym/productos/importar")
    public String importarProductos(
        @AuthenticationPrincipal
        CommerceUserPrincipal principal,

        @RequestParam("archivo")
        MultipartFile archivo,

        RedirectAttributes redirect
    ) {
        validarGym(principal);

        try {
            GymExcelImportResult result =
                excelService.importarProductos(
                    archivo,
                    principal.getTenantId()
                );

            redirect.addFlashAttribute(
                "excelExito",
                result.getResumen()
            );

            redirect.addFlashAttribute(
                "excelErrores",
                result.getErrores()
            );

        } catch (Exception exception) {
            redirect.addFlashAttribute(
                "excelError",
                exception.getMessage()
            );
        }

        return "redirect:/gym/productos";
    }

    @PostMapping("/gym/clientes/importar")
    public String importarClientes(
        @AuthenticationPrincipal
        CommerceUserPrincipal principal,

        @RequestParam("archivo")
        MultipartFile archivo,

        RedirectAttributes redirect
    ) {
        validarGym(principal);

        try {
            GymExcelImportResult result =
                excelService.importarClientes(
                    archivo,
                    principal.getTenantId(),
                    principal.getTenantSlug()
                );

            redirect.addFlashAttribute(
                "excelExito",
                result.getResumen()
            );

            redirect.addFlashAttribute(
                "excelErrores",
                result.getErrores()
            );

        } catch (Exception exception) {
            redirect.addFlashAttribute(
                "excelError",
                exception.getMessage()
            );
        }

        return "redirect:/gym/clientes";
    }

    private ResponseEntity<byte[]> descargar(
        byte[] contenido,
        String filename
    ) {
        return ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\""
                    + filename
                    + "\""
            )
            .contentType(
                MediaType.parseMediaType(XLSX)
            )
            .body(contenido);
    }

    private void validarGym(
        CommerceUserPrincipal principal
    ) {
        if (
            principal == null
                || !principal.esGym()
        ) {
            throw new IllegalArgumentException(
                "Acceso exclusivo de Gym Cloud"
            );
        }
    }
}
