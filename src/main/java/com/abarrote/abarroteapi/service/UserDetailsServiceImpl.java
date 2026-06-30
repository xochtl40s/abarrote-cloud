package com.abarrote.abarroteapi.service;

import com.abarrote.abarroteapi.entity.Usuario;
import com.abarrote.abarroteapi.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsernameIgnoreCase(username.trim())
                .orElseThrow(() -> new UsernameNotFoundException("El usuario no existe: " + username));

        // Limpiamos el rol de cualquier espacio en blanco incidental y lo forzamos a mayúsculas
        String rolLimpio = usuario.getRol().trim().toUpperCase();
        String rolConPrefijo = "ROLE_" + rolLimpio;
        // Imprime en tu consola para auditoría visual rápida
        System.out.println("👉 AUTENTICANDO USUARIO: " + usuario.getUsername() + " CON ROL: " + rolConPrefijo);
        return new User(
                usuario.getUsername(),
                usuario.getPassword(),
                usuario.getActivo() != null ? usuario.getActivo() : true,
                true, true, true,
                Collections.singletonList(new SimpleGrantedAuthority(rolConPrefijo))
        );
    }
}
