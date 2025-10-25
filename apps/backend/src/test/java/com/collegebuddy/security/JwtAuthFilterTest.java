package com.collegebuddy.security;

import jakarta.servlet.FilterChain; import jakarta.servlet.ServletException;
import org.junit.jupiter.api.*; import org.mockito.Mockito; import org.springframework.mock.web.*; import org.springframework.security.core.context.SecurityContextHolder;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthFilterTest {
    JwtService jwt = new JwtService();
    JwtAuthFilter filter = new JwtAuthFilter(jwt);

    @AfterEach void clear(){ SecurityContextHolder.clearContext(); }

    @Test void setsAuthentication_whenValidBearerToken() throws ServletException, IOException {
        String token = jwt.issueAccess(10L, 1L);
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer "+token);
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);

        filter.doFilter(req, res, chain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo("10");
    }

    @Test void leavesContextEmpty_whenNoHeader() throws Exception {
        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), Mockito.mock(FilterChain.class));
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test void invalidToken_leavesContextEmpty() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer not-a-jwt");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(req, res, Mockito.mock(FilterChain.class));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test void validToken_setsSchoolIdRequestAttr() throws Exception {
        String token = jwt.issueAccess(10L, 77L);
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer "+token);
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(req, res, Mockito.mock(FilterChain.class));

        assertThat(req.getAttribute("schoolId")).isEqualTo(77L);
    }

    @Test
    void malformedBearer_headerIgnored() throws Exception {
        var req = new org.springframework.mock.web.MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer "); // missing token
        var res = new org.springframework.mock.web.MockHttpServletResponse();

        filter.doFilter(req, res, Mockito.mock(FilterChain.class));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }


}