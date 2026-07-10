package com.proofpay.security.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Limite le nombre de tentatives sur /api/auth/** par adresse IP, pour se
 * prémunir contre le brute-force d'OTP (§9 spécifications techniques :
 * "Journalisation de sécurité : tentatives OTP..." implique aussi de les
 * limiter). Implémentation MVP en mémoire (fenêtre fixe de 15 minutes) ; à
 * remplacer par Redis en production multi-instance.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_ATTEMPTS_PER_WINDOW = 10;
    private static final long WINDOW_MILLIS = 15 * 60 * 1000L;

    private final ConcurrentHashMap<String, Window> attemptsByIp = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/api/auth/")) {
            String ip = clientIp(request);
            Window window = attemptsByIp.compute(ip, (key, existing) -> {
                long now = System.currentTimeMillis();
                if (existing == null || now - existing.windowStart > WINDOW_MILLIS) {
                    return new Window(now);
                }
                return existing;
            });

            if (window.count.incrementAndGet() > MAX_ATTEMPTS_PER_WINDOW) {
                response.setStatus(429); // Too Many Requests
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"message\":\"Trop de tentatives. Merci de réessayer dans quelques minutes.\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded != null ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }

    private static final class Window {
        final long windowStart;
        final AtomicInteger count = new AtomicInteger(0);

        Window(long windowStart) {
            this.windowStart = windowStart;
        }
    }
}
