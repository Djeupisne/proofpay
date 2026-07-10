package com.proofpay.common.idempotency;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Empêche le double traitement des requêtes sensibles (paiement, confirmation)
 * via l'en-tête "Idempotency-Key" (règle métier #16 : les paiements doivent
 * être idempotents). Implémentation MVP en mémoire ; à remplacer par Redis
 * en production multi-instance.
 */
@Component
public class IdempotencyKeyFilter extends OncePerRequestFilter {

    private static final String HEADER = "Idempotency-Key";
    private final ConcurrentHashMap<String, Long> seenKeys = new ConcurrentHashMap<>();
    private static final long TTL_MS = TimeUnit.HOURS.toMillis(24);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String key = request.getHeader(HEADER);
        if (key != null && !key.isBlank()) {
            Long previous = seenKeys.putIfAbsent(key, System.currentTimeMillis());
            if (previous != null && System.currentTimeMillis() - previous < TTL_MS) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                response.getWriter().write("{\"message\":\"Requête déjà traitée (idempotency key réutilisée)\"}");
                response.setContentType("application/json");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
