package com.abarrote.abarroteapi.controller;

import com.abarrote.abarroteapi.entity.Sucursal;
import com.abarrote.abarroteapi.service.SucursalComparativoService;
import com.abarrote.abarroteapi.service.SucursalDetalleService;
import com.abarrote.abarroteapi.service.SucursalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/sucursales")
public class SucursalController {

    private final SucursalService sucursalService;

    private final SucursalDetalleService
            sucursalDetalleService;

    private final SucursalComparativoService
            sucursalComparativoService;

    public SucursalController(
            SucursalService sucursalService,
            SucursalDetalleService sucursalDetalleService,
            SucursalComparativoService sucursalComparativoService) {

        this.sucursalService =
                sucursalService;

        this.sucursalDetalleService =
                sucursalDetalleService;

        this.sucursalComparativoService =
                sucursalComparativoService;
    }

    @GetMapping
    public String listar(
            Model model) {

        if (!model.containsAttribute(
                "sucursal")) {

            Sucursal sucursal =
                    new Sucursal();

            sucursal.setActiva(true);

            model.addAttribute(
                    "sucursal",
                    sucursal
            );
        }

        model.addAttribute(
                "sucursales",
                sucursalService.listarTodas()
        );

        model.addAttribute(
                "comparativoSucursales",
                sucursalComparativoService
                        .obtenerComparativoSucursalesActivas()
        );

        model.addAttribute(
                "modoEdicion",
                false
        );

        model.addAttribute(
                "activePage",
                "sucursales"
        );

        return "admin/sucursales";
    }

    @GetMapping("/{id}")
    public String detalle(
            @PathVariable Long id,
            Model model) {

        model.addAttribute(
                "detalle",
                sucursalDetalleService
                        .obtenerDetalle(id)
        );

        model.addAttribute(
                "activePage",
                "sucursales"
        );

        return "admin/sucursal-detalle";
    }

    @GetMapping("/editar/{id}")
    public String editar(
            @PathVariable Long id,
            Model model) {

        model.addAttribute(
                "sucursal",
                sucursalService.obtenerPorId(id)
        );

        model.addAttribute(
                "sucursales",
                sucursalService.listarTodas()
        );

        model.addAttribute(
                "comparativoSucursales",
                sucursalComparativoService
                        .obtenerComparativoSucursalesActivas()
        );

        model.addAttribute(
                "modoEdicion",
                true
        );

        model.addAttribute(
                "activePage",
                "sucursales"
        );

        return "admin/sucursales";
    }

    @PostMapping
    public String crear(
            @ModelAttribute
            Sucursal sucursal,

            RedirectAttributes redirectAttributes) {

        try {

            Sucursal guardada =
                    sucursalService.crear(
                            sucursal
                    );

            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    "Sucursal "
                            + guardada.getNombre()
                            + " creada correctamente. "
                            + "Su inventario inicial está vacío."
            );

        } catch (RuntimeException exception) {

            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    exception.getMessage()
            );

            redirectAttributes.addFlashAttribute(
                    "sucursal",
                    sucursal
            );
        }

        return "redirect:/admin/sucursales";
    }

    @PostMapping("/actualizar/{id}")
    public String actualizar(
            @PathVariable Long id,

            @ModelAttribute
            Sucursal sucursal,

            RedirectAttributes redirectAttributes) {

        try {

            Sucursal actualizada =
                    sucursalService.actualizar(
                            id,
                            sucursal
                    );

            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    "Sucursal "
                            + actualizada.getNombre()
                            + " actualizada correctamente"
            );

        } catch (RuntimeException exception) {

            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    exception.getMessage()
            );
        }

        return "redirect:/admin/sucursales";
    }

    @PostMapping("/cerrar/{id}")
    public String cerrarSucursal(
            @PathVariable Long id,

            Authentication authentication,

            RedirectAttributes redirectAttributes) {

        try {

            String usuarioResponsable =
                    authentication != null
                            ? authentication.getName()
                            : "usuario-desconocido";

            sucursalService.cerrarSucursal(
                    id,
                    usuarioResponsable
            );

            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    "Sucursal cerrada correctamente. "
                            + "El inventario fue trasladado a Matriz."
            );

        } catch (RuntimeException exception) {

            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    exception.getMessage()
            );
        }

        return "redirect:/admin/sucursales";
    }
}
