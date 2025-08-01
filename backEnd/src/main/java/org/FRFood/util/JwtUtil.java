package org.FRFood.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.FRFood.entity.*;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

public class JwtUtil {
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24 hours
    private static final Key SECRET_KEY = Keys.hmacShaKeyFor("ay baba farsi benvisam momkene error bede".getBytes(StandardCharsets.UTF_8));

    public static String generateToken(User user) {
        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .claim("confirmed", user.isConfirmed()) // optional
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public static Jws<Claims> validateToken(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token);
    }

    public static int getUserIdFromToken(String token) {
        Claims claims = validateToken(token).getBody();
        return Integer.parseInt(claims.getSubject());
    }

}