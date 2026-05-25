package com.spagnuolo.flashify_app.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    // One bucket per IP address
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket createLoginBucket() {
        // Allow 5 attempts per minute
        Bandwidth limit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket createGeneralBucket() {
        // Allow 100 requests per minute for general endpoints
        Bandwidth limit = Bandwidth.classic(100, Refill.greedy(100, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    private String getClientIP(HttpServletRequest request) {
    // Cloudflare passes the real IP in this header
    String cfIP = request.getHeader("CF-Connecting-IP");
    if (cfIP != null && !cfIP.isEmpty()) {
        return cfIP;
    }
    String realIP = request.getHeader("X-Real-IP");
    if (realIP != null && !realIP.isEmpty()) {
        return realIP;
    }
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isEmpty()) {
        return forwarded.split(",")[0].trim();
    }
    return request.getRemoteAddr();
}

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        String ip = getClientIP(request);

        // Apply strict rate limiting to auth endpoints
        if (path.equals("/api/teachers/login") || path.equals("/api/teachers/register")) {
            String key = "login:" + ip;
            Bucket bucket = buckets.computeIfAbsent(key, k -> createLoginBucket());

            if (!bucket.tryConsume(1)) {
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Too many attempts. Please wait a minute before trying again.\"}");
                return;
            }
        } else {
            // General rate limiting for all other endpoints
            String key = "general:" + ip;
            Bucket bucket = buckets.computeIfAbsent(key, k -> createGeneralBucket());

            if (!bucket.tryConsume(1)) {
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Too many requests. Please slow down.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}