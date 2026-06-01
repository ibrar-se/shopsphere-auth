package com.shopsphere.auth_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // =========================
        // 1. Get Authorization Header
        // =========================
        final String authHeader = request.getHeader("Authorization");

        final String jwt;
        final String userEmail;

        // =========================
        // 2. Check Header Exists
        // =========================
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);

            return;
        }

        // =========================
        // 3. Extract JWT Token
        // =========================
        jwt = authHeader.substring(7);

        // =========================
        // 4. Extract Username
        // =========================
        userEmail = jwtUtil.extractUsername(jwt);

        // =========================
        // 5. Check User Not Already Authenticated
        // =========================
        if (userEmail != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            // =========================
            // 6. Load User From Database
            // =========================
            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(userEmail);

            // =========================
            // 7. Validate Token
            // =========================
            if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {

                // =========================
                // 8. Create Authentication Object
                // =========================
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                // =========================
                // 9. Add Request Details
                // =========================
                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                // =========================
                // 10. Set Authentication
                // =========================
                SecurityContextHolder.getContext()
                        .setAuthentication(authToken);
            }
        }

        // =========================
        // 11. Continue Filter Chain
        // =========================
        filterChain.doFilter(request, response);
    }
}