package com.shopsphere.auth_service.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    // =========================
    // Password Encoder
    // =========================
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // =========================
    // Authentication Provider
    // =========================
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // =========================
    // Security Filter Chain
    // =========================
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {

        try {
            http
                    // Disable CSRF
                    .csrf(AbstractHttpConfigurer::disable)

                    // Stateless Session
                    .sessionManagement(session ->
                            session.sessionCreationPolicy(
                                    SessionCreationPolicy.STATELESS
                            )
                    )

                    // Route Authorization
                    .authorizeHttpRequests(auth -> auth
                            // FIX: These now correctly match your /api/auth controller endpoints!
                            .requestMatchers("/api/auth/**").permitAll()
                            .requestMatchers("/api/public/**").permitAll()
                            .anyRequest().authenticated()
                    )

                    // Authentication Provider
                    .authenticationProvider(authenticationProvider())

                    // Add JWT Filter
                    .addFilterBefore(
                            jwtAuthenticationFilter,
                            UsernamePasswordAuthenticationFilter.class
                    );

            return http.build();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}