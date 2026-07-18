package com.abarrote.abarroteapi.service.impl;

import com.abarrote.abarroteapi.entity.Usuario;
import com.abarrote.abarroteapi.multitenant.domain.Tenant;
import com.abarrote.abarroteapi.repository.UsuarioRepository;
import com.abarrote.abarroteapi.security.CommerceUserPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserDetailsServiceImpl
    implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UserDetailsServiceImpl(
        UsuarioRepository usuarioRepository
    ) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(
        String username
    ) throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository
            .findByUsernameIgnoreCase(username)
            .orElseThrow(
                () -> new UsernameNotFoundException(
                    "Usuario no encontrado: " + username
                )
            );

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new UsernameNotFoundException(
                "Usuario desactivado: " + username
            );
        }

        Tenant tenant = usuario.getTenant();

        if (tenant == null) {
            throw new UsernameNotFoundException(
                "El usuario no tiene un tenant asignado: "
                    + username
            );
        }

        String role = usuario.getRol();

        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }

        List<SimpleGrantedAuthority> authorities =
            new ArrayList<>();

        authorities.add(
            new SimpleGrantedAuthority(role)
        );

        Long sucursalId =
            usuario.getSucursal() != null
                ? usuario.getSucursal().getId()
                : null;

        return new CommerceUserPrincipal(
            usuario.getId(),
            usuario.getNombre(),
            usuario.getUsername(),
            usuario.getPassword(),
            authorities,
            tenant.getId(),
            tenant.getSlug(),
            tenant.getNombre(),
            tenant.getTipoNegocio().name(),
            sucursalId
        );
    }
}
