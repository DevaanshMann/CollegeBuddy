package com.collegebuddy.config;

import com.collegebuddy.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.SecurityFilterChain;

/*
Security configuration: HTTP security, filter chain, session, etc.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
//             TODO: configure scrf, authorizeHttpRequests, sessionCreationpolicy, etc.
        return http.build();
    }
}
