package com.abarrote.abarroteapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AuthenticatedNavigationWebConfig
        implements WebMvcConfigurer {

    private final AuthenticatedNavigationInterceptor interceptor;

    public AuthenticatedNavigationWebConfig(
            AuthenticatedNavigationInterceptor interceptor) {

        this.interceptor = interceptor;
    }

    @Override
    public void addInterceptors(
            InterceptorRegistry registry) {

        registry
                .addInterceptor(interceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/webjars/**",
                        "/favicon.ico"
                );
    }
}
