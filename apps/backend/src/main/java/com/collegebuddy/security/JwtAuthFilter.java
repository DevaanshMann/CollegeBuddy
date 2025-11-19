package com.collegebuddy.security;

import com.collegebuddy.common.exceptions.ForbiddenCampusAccessException;
import com.collegebuddy.common.exceptions.UnauthorizedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        // Skip JWT validation for public endpoints
        String path = request.getRequestURI();
        if (path.equals("/auth/signup") ||
            path.equals("/auth/login") ||
            path.equals("/auth/verify") ||
            path.equals("/auth/resend")) {
            chain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring("Bearer ".length()).trim();

            if (!jwtService.validateToken(token)) {
                throw new UnauthorizedException("Invalid or expired JWT");
            }

            Long userId = jwtService.extractUserId(token);
            String campusDomain = jwtService.extractCampusDomain(token);

            if (campusDomain == null || campusDomain.isBlank()) {
                throw new ForbiddenCampusAccessException("Campus domain missing or invalid");
            }

            AuthenticatedUser principal = new AuthenticatedUser(userId, campusDomain);

            AbstractAuthenticationToken auth =
                    new AbstractAuthenticationToken(List.of(new SimpleGrantedAuthority("ROLE_USER"))) {
                        @Override
                        public Object getCredentials() {
                            return token;
                        }

                        @Override
                        public Object getPrincipal() {
                            return principal;
                        }
                    };
            auth.setAuthenticated(true);

            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        chain.doFilter(request, response);
    }
}
