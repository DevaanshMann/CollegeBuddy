package com.collegebuddy.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Reads the Authorization: Bearer <JWT> header, validates it via JwtService,
 * and places an Authentication into the SecurityContext.
 *
 * Principal:   userId (String)
 * Authorities: none (empty list)
 * Request attr: "schoolId" (Long) — convenient to access campus scoping.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwt;

    public JwtAuthFilter(JwtService jwt) {
        this.jwt = jwt;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            Jws<Claims> jws = jwt.parse(token);
            Claims claims = jws.getBody();

            String sub = claims.getSubject();
            Object sidRaw = claims.get("sid"); // could be Integer, Long, or null

            if (sub != null && !sub.isBlank()) {
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(sub, null, List.of());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                if (sidRaw instanceof Number n) {
                    Long schoolId = n.longValue();
                    request.setAttribute("schoolId", schoolId);
                }

                SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                SecurityContextHolder.clearContext();
            }
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }
}
