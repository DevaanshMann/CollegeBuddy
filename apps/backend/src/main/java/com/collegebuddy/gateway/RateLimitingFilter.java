package com.collegebuddy.gateway;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Naive rate limiter placeholder.
 * Replace with real limiter or gateway rules later.
 */
@Component
public class RateLimitingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        // TODO: enforce per-IP or per-user thresholds
        HttpServletResponse httpResp = (HttpServletResponse) response;
        // For now just continue
        chain.doFilter(request, response);
    }
}
