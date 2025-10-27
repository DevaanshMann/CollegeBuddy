//package com.collegebuddy.config;
//
//import com.collegebuddy.security.JwtAuthFilter;
//import com.collegebuddy.security.JwtService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.annotation.Import;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.web.FilterChainProxy;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.web.bind.annotation.*;
//
//import jakarta.servlet.Filter;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(controllers = SecurityConfigTest.TestController.class)
//@Import(SecurityConfig.class) // import your security configuration into the MVC slice
//class SecurityConfigTest {
//
//    @Autowired MockMvc mvc;
//    @Autowired ApplicationContext ctx;
//
//    // SecurityConfig defines JwtAuthFilter bean via a JwtService dependency — provide a mock
//    @MockBean JwtService jwtService;
//
//    // --- Simple controller just for exercising routes in tests ---
//    @RestController
//    static class TestController {
//        @GetMapping("/auth/ping")
//        public String authPing() { return "ok"; }
//
//        @PostMapping("/auth/ping")
//        public String authPingPost() { return "ok"; }
//
//        @GetMapping("/secured/ping")
//        public String securedPing() { return "ok"; }
//    }
//
//    @Test
//    void passwordEncoder_isBCrypt() {
//        PasswordEncoder encoder = ctx.getBean(PasswordEncoder.class);
//        assertThat(encoder).isInstanceOf(BCryptPasswordEncoder.class);
//        // sanity check: encoder actually encodes & matches
//        String hash = encoder.encode("secret");
//        assertThat(encoder.matches("secret", hash)).isTrue();
//    }
//
//    @Test
//    void jwtAuthFilter_beanExists() {
//        JwtAuthFilter bean = ctx.getBean(JwtAuthFilter.class);
//        assertThat(bean).isNotNull();
//    }
//
//    @Test
//    void authEndpoints_arePermitAll() throws Exception {
//        // Should not require authentication; since controller exists, expect 200 OK
//        mvc.perform(get("/auth/ping"))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    void nonAuthEndpoints_requireAuthentication() throws Exception {
//        // No authentication -> should be 401 Unauthorized
//        mvc.perform(get("/secured/ping"))
//                .andExpect(status().isUnauthorized());
//    }
//
//    @Test
//    void csrf_isDisabled_allowsPostWithoutCsrfToken() throws Exception {
//        // With CSRF disabled in your config, POST to an endpoint should succeed without CSRF token
//        mvc.perform(post("/auth/ping"))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    void session_isStateless_noSessionCreated() throws Exception {
//        var result = mvc.perform(get("/auth/ping")).andReturn();
//        assertThat(result.getRequest().getSession(false)).as("no session should be created").isNull();
//    }
//
//    @Test
//    void jwtAuthFilter_isBefore_UsernamePasswordAuthenticationFilter() {
//        FilterChainProxy chain = ctx.getBean(FilterChainProxy.class);
//        // pick a secured path so the chain is fully assembled
//        List<Filter> filters = chain.getFilters("/secured/ping");
//
//        int jwtIdx = indexOf(filters, JwtAuthFilter.class);
//        int upaIdx = indexOf(filters, UsernamePasswordAuthenticationFilter.class);
//
//        assertThat(jwtIdx).as("JwtAuthFilter present in chain").isNotEqualTo(-1);
//        assertThat(upaIdx).as("UsernamePasswordAuthenticationFilter present in chain").isNotEqualTo(-1);
//        assertThat(jwtIdx).as("JwtAuthFilter is added before UsernamePasswordAuthenticationFilter")
//                .isLessThan(upaIdx);
//    }
//
//    private static int indexOf(List<Filter> filters, Class<? extends Filter> type) {
//        for (int i = 0; i < filters.size(); i++) {
//            if (type.isInstance(filters.get(i))) return i;
//        }
//        return -1;
//    }
//}

package com.collegebuddy.config;

import com.collegebuddy.security.JwtAuthFilter;
import com.collegebuddy.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.Filter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SecurityConfigTest.TestController.class)
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired MockMvc mvc;
    @Autowired ApplicationContext ctx;

    // SecurityConfig creates JwtAuthFilter via this dependency => provide a mock
    @MockBean JwtService jwtService;

    // Minimal controller to exercise routes
    @RestController
    static class TestController {
        @GetMapping("/auth/ping") public String authGet() { return "ok"; }
        @PostMapping(value="/auth/ping", consumes = "application/json") public String authPost(@RequestBody(required=false) String body){ return "ok"; }
        @GetMapping("/secured/ping") public String securedGet(){ return "ok"; }
        @PostMapping(value="/secured/ping", consumes = "application/json") public String securedPost(@RequestBody String body){ return "ok"; }
    }

    @Test
    void passwordEncoder_isBCrypt() {
        PasswordEncoder enc = ctx.getBean(PasswordEncoder.class);
        assertThat(enc).isInstanceOf(BCryptPasswordEncoder.class);
        String hash = enc.encode("secret");
        assertThat(enc.matches("secret", hash)).isTrue();
    }

    @Test
    void jwtAuthFilter_beanExists() {
        JwtAuthFilter f = ctx.getBean(JwtAuthFilter.class);
        assertThat(f).isNotNull();
    }

    @Test
    void authEndpoints_arePermitAll_getAndPost() throws Exception {
        mvc.perform(get("/auth/ping")).andExpect(status().isOk());
        mvc.perform(post("/auth/ping").contentType(APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void nonAuthEndpoints_requireAuthentication() throws Exception {
        mvc.perform(get("/secured/ping")).andExpect(status().isUnauthorized());
        mvc.perform(post("/secured/ping").contentType(APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void csrf_isDisabled_postWithoutCsrfIsAllowedOnPermitAll() throws Exception {
        // Because CSRF is disabled in SecurityConfig, POST to /auth/** should succeed without CSRF token
        mvc.perform(post("/auth/ping").contentType(APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void session_isStateless_noSessionCreatedOnPermitAll() throws Exception {
        var result = mvc.perform(get("/auth/ping")).andReturn();
        assertThat(result.getRequest().getSession(false)).as("no HTTP session should be created").isNull();
    }

    @Test
    void jwtAuthFilter_isBefore_UsernamePasswordAuthenticationFilter() {
        FilterChainProxy chain = ctx.getBean(FilterChainProxy.class);
        List<Filter> filters = chain.getFilters("/secured/ping");

        int jwtIdx = indexOf(filters, JwtAuthFilter.class);
        int upaIdx = indexOf(filters, UsernamePasswordAuthenticationFilter.class);

        assertThat(jwtIdx).as("JwtAuthFilter present").isNotEqualTo(-1);
        assertThat(upaIdx).as("UsernamePasswordAuthenticationFilter present").isNotEqualTo(-1);
        assertThat(jwtIdx).as("JwtAuthFilter is before UsernamePasswordAuthenticationFilter").isLessThan(upaIdx);
    }

    private static int indexOf(List<Filter> filters, Class<? extends Filter> type) {
        for (int i = 0; i < filters.size(); i++) {
            if (type.isInstance(filters.get(i))) return i;
        }
        return -1;
    }
}
