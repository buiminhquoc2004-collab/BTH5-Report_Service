package com.example.bth5report.security;

import java.io.IOException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.example.bth5report.exception.UpstreamException;

@Component
public class AuthFilter extends OncePerRequestFilter {
    private final AuthClient auth;
    public AuthFilter(AuthClient auth) { this.auth = auth; }
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
            FilterChain chain) throws ServletException, IOException {
        String token = req.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ") || token.length() <= 7) {
            error(res, 401, "Bearer token required"); return;
        }
        try {
            if (!auth.valid(token)) { error(res, 401, "Invalid token"); return; }
        } catch (UpstreamException e) {
            error(res, 503, "Authentication Service unavailable"); return;
        }
        chain.doFilter(req, res);
    }
    private void error(HttpServletResponse res, int status, String message) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json");
        res.getWriter().write("{\"status\":" + status + ",\"message\":\"" + message + "\"}");
    }
}
