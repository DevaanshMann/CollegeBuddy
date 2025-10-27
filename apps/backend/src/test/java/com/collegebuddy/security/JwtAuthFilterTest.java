package com.collegebuddy.security;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthFilterTest {

    JwtService jwt;
    JwtAuthFilter filter;

    @BeforeEach
    void setUp() {
        jwt = new JwtService();
        filter = new JwtAuthFilter(jwt);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void noAuthorizationHeader_leavesContextUnauthenticated() throws ServletException, IOException {
        var req = new MockHttpServletRequest("GET", "/anything");
        var res = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void noAuthorizationHeader_withUnrelatedHeader_stillUnauthenticated() throws ServletException, IOException {
        var req = new MockHttpServletRequest("GET", "/anything");
        req.addHeader("X-Api-Key", "abc123");
        var res = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void emptyAuthorizationHeader_stillUnauthenticated() throws ServletException, IOException {
        var req = new MockHttpServletRequest("GET", "/anything");
        req.addHeader("Authorization", "");
        var res = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void nonBearerAuthorizationHeader_leavesContextUnauthenticated() throws ServletException, IOException {
        var req = new MockHttpServletRequest("GET", "/anything");
        req.addHeader("Authorization", "Basic abcdef");
        var res = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void lowercaseBearerPrefix_isNotAccepted_leavesContextUnauthenticated() throws ServletException, IOException {
        var req = new MockHttpServletRequest("GET", "/anything");
        req.addHeader("Authorization", "bearer someToken");
        var res = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void bearerWithoutSpace_isNotAccepted_leavesContextUnauthenticated() throws ServletException, IOException {
        var req = new MockHttpServletRequest("GET", "/anything");
        // Missing space after 'Bearer'
        req.addHeader("Authorization", "BearerXYZ");
        var res = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void bearerValidToken_setsAuthentication_andSchoolIdAttribute() throws ServletException, IOException {
        String token = jwt.issueAccess(42L, 7L);

        var req = new MockHttpServletRequest("GET", "/secure");
        req.addHeader("Authorization", "Bearer " + token);
        req.setRemoteAddr("127.0.0.1");
        var res = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(auth.getPrincipal()).isEqualTo("42");
        assertThat(auth.getAuthorities()).isEmpty();
        assertThat(auth.getDetails()).isNotNull();

        assertThat(req.getAttribute("schoolId")).isEqualTo(7L);
    }

    @Test
    void bearerValidToken_credentialsAreNull_andDetailsContainRemoteAddr() throws ServletException, IOException {
        String token = jwt.issueAccess(100L, 3L);

        var req = new MockHttpServletRequest("GET", "/secure");
        req.setRemoteAddr("10.0.0.5");
        req.addHeader("Authorization", "Bearer " + token);
        var res = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        var auth = (UsernamePasswordAuthenticationToken)
                SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth.getCredentials()).isNull();
        assertThat(req.getAttribute("schoolId")).isEqualTo(3L);
        assertThat(auth.getDetails()).isNotNull(); // built from request (includes remote addr)
    }

    @Test
    void bearerValidToken_smallSid_stillSetsSchoolIdAttribute() throws ServletException, IOException {
        String token = jwt.issueAccess(77L, 1L); // small SID value

        var req = new MockHttpServletRequest("GET", "/secure");
        req.addHeader("Authorization", "Bearer " + token);
        var res = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(req.getAttribute("schoolId")).isEqualTo(1L);
    }

    @Test
    void bearerValidToken_withoutSid_setsAuth_butNoSchoolIdAttribute() throws ServletException, IOException {
        String token = jwt.issueAccess(99L, 0L);

        var req = new MockHttpServletRequest("GET", "/secure");
        req.addHeader("Authorization", "Bearer " + token);
        var res = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();

        assertThat(((UsernamePasswordAuthenticationToken) auth).getAuthorities()).isEmpty();
        assertThat(auth.getPrincipal()).isEqualTo("99");

        assertThat(req.getAttribute("schoolId")).isInstanceOf(Number.class);
    }

    @Test
    void bearerToken_withSidMissing_isNotSet_whenAuthPreservesOtherFields() throws ServletException, IOException {

        String token = jwt.issueAccess(123L, 5L);

        var req = new MockHttpServletRequest("GET", "/secure");
        req.addHeader("Authorization", "Bearer " + token);
        var res = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        var auth = (UsernamePasswordAuthenticationToken)
                SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth.getAuthorities()).isEmpty();
        assertThat(auth.getCredentials()).isNull();
    }

    @Test
    void bearerToken_setsPrincipalStringValue() throws ServletException, IOException {
        String token = jwt.issueAccess(555L, 6L);

        var req = new MockHttpServletRequest("GET", "/secure");
        req.addHeader("Authorization", "Bearer " + token);
        var res = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth.getPrincipal()).isEqualTo("555");
    }

    @Test
    void alreadyAuthenticated_skipsParsing_andPreservesAuth() throws ServletException, IOException {
        var existing = new UsernamePasswordAuthenticationToken("seed", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(existing);

        var req = new MockHttpServletRequest("GET", "/secure");
        req.addHeader("Authorization", "Bearer whatever");
        var res = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isSameAs(existing);

        assertThat(req.getAttribute("schoolId")).isNull();
    }

    @Test
    void alreadyAuthenticated_preservesAuthorities() throws ServletException, IOException {
        var existing = new UsernamePasswordAuthenticationToken("seed", null,
                List.of(() -> "ROLE_USER"));
        SecurityContextHolder.getContext().setAuthentication(existing);

        var req = new MockHttpServletRequest("GET", "/secure");
        req.addHeader("Authorization", "Bearer ignored");
        var res = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        var auth = (UsernamePasswordAuthenticationToken)
                SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth.getAuthorities()).hasSize(1);
        assertThat(auth.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    void alreadyAuthenticated_doesNotChangePrincipal() throws ServletException, IOException {
        var existing = new UsernamePasswordAuthenticationToken("original", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(existing);

        var req = new MockHttpServletRequest("GET", "/secure");
        req.addHeader("Authorization", "Bearer also-ignored");
        var res = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .isEqualTo("original");
    }

    @Test
    void invalidToken_parseThrows_leavesContextUnauthenticated() throws ServletException, IOException {
        var req = new MockHttpServletRequest("GET", "/secure");
        req.addHeader("Authorization", "Bearer not-a-jwt");
        var res = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(req.getAttribute("schoolId")).isNull();
    }

    @Test
    void invalidToken_emptyAfterBearer_leavesContextUnauthenticated() throws ServletException, IOException {
        var req = new MockHttpServletRequest("GET", "/secure");

        req.addHeader("Authorization", "Bearer ");
        var res = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void invalidToken_tamperedSignature_leavesContextUnauthenticated() throws ServletException, IOException {
        String good = jwt.issueAccess(1L, 1L);
        String tampered = good.substring(0, good.length() - 1) + "x";

        var req = new MockHttpServletRequest("GET", "/secure");
        req.addHeader("Authorization", "Bearer " + tampered);
        var res = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(req.getAttribute("schoolId")).isNull();
    }
}
