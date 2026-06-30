package com.abarrote.abarroteapi.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        var authorities = authentication.getAuthorities();
        boolean isAdmin = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isCajero = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_CAJERO"));

        if (isAdmin) {
            response.sendRedirect("/pos/inicio");
        } else if (isCajero) {
            response.sendRedirect("/pos/caja");
        } else {
            response.sendRedirect("/login?error");
        }
    }
}
