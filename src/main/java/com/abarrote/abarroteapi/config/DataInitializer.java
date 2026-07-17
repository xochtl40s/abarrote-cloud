package com.abarrote.abarroteapi.config;

import com.abarrote.abarroteapi.entity.Usuario;
import com.abarrote.abarroteapi.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.findByUsernameIgnoreCase("admin").isEmpty()) {
            Usuario admin = new Usuario();
            admin.setNombre("Administrador");
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setRol("ADMIN");  // Sin prefijo ROLE_
            admin.setActivo(true);
            usuarioRepository.save(admin);
            System.out.println(">>> Usuario admin creado: admin / admin");
        }

        if (usuarioRepository.findByUsernameIgnoreCase("cajero").isEmpty()) {
            Usuario cajero = new Usuario();
            cajero.setNombre("Cajero Principal");
            cajero.setUsername("cajero");
            cajero.setPassword(passwordEncoder.encode("cajero"));
            cajero.setRol("CAJERO");  // Sin prefijo ROLE_
            cajero.setActivo(true);
            usuarioRepository.save(cajero);
            System.out.println(">>> Usuario cajero creado: cajero / cajero");
        }
    }
}
