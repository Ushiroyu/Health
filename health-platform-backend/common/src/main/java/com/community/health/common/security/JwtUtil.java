package com.community.health.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public class JwtUtil {
    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(String secret, long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    // Convenience API used by services: create token with standard claims
    public String createToken(Long userId, String username, Role role) {
        Map<String, Object> claims = Map.of(
                "userId", userId,
                "role", role == null ? null : role.name(),
                "username", username
        );
        return generateToken(username, claims);
    }

    public String generateToken(String username, Map<String, Object> claims) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    // compatibility helper expected by some existing code
    public Claims parse(String token) {
        return parseToken(token).getBody();
    }

    public String getUsername(String token) {
        return parse(token).getSubject();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    // New helper: map claims to JwtUser
    public JwtUser parseUser(String token) {
        Claims c = parse(token);
        JwtUser u = new JwtUser();
        Object uid = c.get("userId");
        if (uid instanceof Number) {
            u.setUserId(((Number) uid).longValue());
        } else if (uid != null) {
            try {
                u.setUserId(Long.parseLong(uid.toString()));
            } catch (NumberFormatException ignore) {}
        }
        // username: prefer subject, fallback to claim 'username'
        String username = c.getSubject();
        if (username == null || username.isEmpty()) {
            Object un = c.get("username");
            username = un == null ? null : un.toString();
        }
        u.setUsername(username);
        Object role = c.get("role");
        u.setRole(role == null ? null : role.toString());
        return u;
    }
}
