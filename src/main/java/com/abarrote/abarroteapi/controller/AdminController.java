package com.abarrote.abarroteapi.controller;

import com.abarrote.abarroteapi.entity.Usuario;
import com.abarrote.abarroteapi.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

import com.abarrote.abarroteapi.entity.Producto;
import com.abarrote.abarroteapi.repository.ProductoRepository;
import java.math.BigDecimal;

@Controller
public class AdminController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository; // <-- Inyectamos el nuevo repositorio

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/pos/inicio")
    public String mostrarDashboard(Model model, Authentication authentication) {
        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!esAdmin) {
            return "redirect:/pos/caja";
        }

        // Cargar cajeros activos
        List<Usuario> cajerosActivos = usuarioRepository.findAll().stream()
                .filter(u -> "CAJERO".equalsIgnoreCase(u.getRol()) && (u.getActivo() != null && u.getActivo()))
                .collect(Collectors.toList());
        model.addAttribute("cajeros", cajerosActivos);

        // Cargar TODOS los productos para el visor de inventario
        List<Producto> listaProductos = productoRepository.findAll();
        model.addAttribute("productos", listaProductos); // <-- Enviamos los productos a la vista

        return "inicio";
    }


    @PostMapping("/admin/registrar-producto")
    public String registrarProducto(@RequestParam String codigoBarras,
                                    @RequestParam String nombre,
                                    @RequestParam BigDecimal precioCompra,
                                    @RequestParam BigDecimal precioVenta,
                                    @RequestParam Integer stock,
                                    RedirectAttributes redirectAttributes) {
        try {
            if (productoRepository.findByCodigoBarras(codigoBarras.trim()).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "El código de barras '" + codigoBarras + "' ya pertenece a otro producto.");
                return "redirect:/pos/inicio";
            }

            Producto nuevoProducto = new Producto();
            nuevoProducto.setCodigoBarras(codigoBarras);
            nuevoProducto.setNombre(nombre);
            nuevoProducto.setPrecioCompra(precioCompra);
            nuevoProducto.setPrecioVenta(precioVenta);
            nuevoProducto.setStock(stock);

            productoRepository.save(nuevoProducto);
            redirectAttributes.addFlashAttribute("exito", "¡Producto '" + nombre + "' agregado al inventario!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error interno al registrar el producto.");
        }
        return "redirect:/pos/inicio";
    }    
	
@PostMapping("/admin/registrar-cajero")
    public String registrarCajero(@RequestParam String nombre,
                                  @RequestParam String username,
                                  @RequestParam String password,
                                  RedirectAttributes redirectAttributes) {
        try {
            String usernameLimpio = username.trim().toLowerCase();
            java.util.Optional<Usuario> usuarioExistente = usuarioRepository.findByUsernameIgnoreCase(usernameLimpio);

            if (usuarioExistente.isPresent()) {
                Usuario usuario = usuarioExistente.get();
                
                // CASO 1: Si ya está activo pero tiene rol ADMIN, protegemos el registro
                if (usuario.getActivo() != null && usuario.getActivo() && "ADMIN".equalsIgnoreCase(usuario.getRol())) {
                    redirectAttributes.addFlashAttribute("error", "El nombre de usuario '" + usernameLimpio + "' ya está registrado como ADMINISTRADOR.");
                    return "redirect:/pos/inicio";
                }
                
                // CASO 2: Si el usuario ya existe pero estaba INACTIVO (fue eliminado previamente), lo reactivamos
                if (usuario.getActivo() == null || !usuario.getActivo()) {
                    usuario.setNombre(nombre.trim());
                    usuario.setPassword(passwordEncoder.encode(password));
                    usuario.setRol("CAJERO"); // Nos aseguramos de forzar el rol correcto
                    usuario.setActivo(true);   // Lo volvemos a dar de alta
                    usuarioRepository.save(usuario);
                    redirectAttributes.addFlashAttribute("exito", "¡El usuario '" + usernameLimpio + "' ha sido reactivado como cajero con éxito!");
                    return "redirect:/pos/inicio";
                }

                // CASO 3: Ya existe y ya está activo como cajero
                redirectAttributes.addFlashAttribute("error", "El nombre de usuario '" + usernameLimpio + "' ya está registrado y activo.");
                return "redirect:/pos/inicio";
            }

            // CASO 4: Registro limpio desde cero si no existe ningún registro previo
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setNombre(nombre.trim());
            nuevoUsuario.setUsername(usernameLimpio);
            nuevoUsuario.setPassword(passwordEncoder.encode(password));
            nuevoUsuario.setRol("CAJERO");
            nuevoUsuario.setActivo(true);

            usuarioRepository.save(nuevoUsuario);
            redirectAttributes.addFlashAttribute("exito", "¡Cajero '" + usernameLimpio + "' registrado con éxito!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error interno al registrar el cajero.");
        }

        return "redirect:/pos/inicio";
    }
// Ruta para eliminar (dar de baja) un cajero por su username
    @PostMapping("/admin/eliminar-cajero")
    public String eliminarCajero(@RequestParam String username, RedirectAttributes redirectAttributes) {
        try {
            String usernameLimpio = username.trim().toLowerCase();
            
            // Buscamos si el usuario existe
            java.util.Optional<Usuario> usuarioOpt = usuarioRepository.findByUsernameIgnoreCase(usernameLimpio);
            
            if (usuarioOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El usuario '@" + usernameLimpio + "' no existe.");
                return "redirect:/pos/inicio";
            }

            Usuario usuario = usuarioOpt.get();

            // Validación de seguridad: No dejar que se borren cuentas ADMIN desde aquí
            if ("ADMIN".equalsIgnoreCase(usuario.getRol())) {
                redirectAttributes.addFlashAttribute("error", "No se puede eliminar un usuario con rol ADMINISTRADOR.");
                return "redirect:/pos/inicio";
            }

            // Opción segura: En lugar de borrar el registro físico (Hard Delete), lo marcamos inactivo (Soft Delete)
            usuario.setActivo(false);
            usuarioRepository.save(usuario);
            
            redirectAttributes.addFlashAttribute("exito", "¡El cajero @" + usernameLimpio + " ha sido dado de baja correctamente!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error interno al procesar la baja del cajero.");
        }

        return "redirect:/pos/inicio";
    }
}
