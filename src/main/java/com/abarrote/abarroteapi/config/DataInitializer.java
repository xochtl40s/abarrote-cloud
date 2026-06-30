package com.abarrote.abarroteapi.config;

import com.abarrote.abarroteapi.entity.Usuario;
import com.abarrote.abarroteapi.entity.Producto;
import com.abarrote.abarroteapi.repository.UsuarioRepository;
import com.abarrote.abarroteapi.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository; // <-- Inyección corregida aquí

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        
        // 1. Asegurar o recrear a cajero1 de forma nativa
        if (usuarioRepository.findByUsernameIgnoreCase("cajero1").isEmpty()) {
            Usuario cajero1 = new Usuario();
            cajero1.setNombre("Cajero de Prueba Nativo");
            cajero1.setUsername("cajero1");
            cajero1.setPassword(passwordEncoder.encode("cajero123"));
            cajero1.setRol("CAJERO");
            cajero1.setActivo(true);
            usuarioRepository.save(cajero1);
            System.out.println("💾 [DataInitializer] Usuario 'cajero1' creado con éxito en PostgreSQL.");
        }

        // 2. Asegurar o recrear a lalo de forma nativa
        if (usuarioRepository.findByUsernameIgnoreCase("lalo").isEmpty()) {
            Usuario lalo = new Usuario();
            lalo.setNombre("Lalo Operador");
            lalo.setUsername("lalo");
            lalo.setPassword(passwordEncoder.encode("lalo123"));
            lalo.setRol("CAJERO");
            lalo.setActivo(true);
            usuarioRepository.save(lalo);
            System.out.println("💾 [DataInitializer] Usuario 'lalo' creado con éxito en PostgreSQL.");
        }

        // 3. Insertar productos de prueba si la tabla está vacía
        if (productoRepository.count() == 0) {
            Producto p1 = new Producto();
            p1.setCodigoBarras("7501055310883");
            p1.setNombre("Coca Cola Variable 600ml");
            p1.setPrecioCompra(new BigDecimal("14.50"));
            p1.setPrecioVenta(new BigDecimal("19.00"));
            p1.setStock(45);
            productoRepository.save(p1);

            Producto p2 = new Producto();
            p2.setCodigoBarras("7501000111206");
            p2.setNombre("Papas Sabritas Sal 42g");
            p2.setPrecioCompra(new BigDecimal("12.00"));
            p2.setPrecioVenta(new BigDecimal("16.50"));
            p2.setStock(30);
            productoRepository.save(p2);
            System.out.println("💾 [DataInitializer] Productos iniciales cargados en el inventario.");
        }
    }
}
