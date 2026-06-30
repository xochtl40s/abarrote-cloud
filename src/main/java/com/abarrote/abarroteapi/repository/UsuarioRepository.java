package com.abarrote.abarroteapi.repository;

import com.abarrote.abarroteapi.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // Método clave para el login: busca ignorando mayúsculas/minúsculas
    Optional<Usuario> findByUsernameIgnoreCase(String username);
}
