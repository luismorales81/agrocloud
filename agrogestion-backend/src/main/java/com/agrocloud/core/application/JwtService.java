package com.agrocloud.core.application;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service("jwtServiceCore")
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    @Value("${jwt.refresh-token.expiration:604800000}")
    private long refreshExpiration;

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, refreshExpiration);
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        Claims claims = extractAllClaims(token);
        if (claims.get("alcance") != null) {
            return false;
        }
        final String username = claims.getSubject();
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Ticket de un solo propósito (por ejemplo aceptar el EULA). No abre sesión.
     */
    public String generarTicketAlcance(String email, String alcance, long validezMs) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("alcance", alcance);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + validezMs))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String validarTicketAlcance(String token, String alcanceEsperado) {
        Claims claims = extractAllClaims(token);
        if (!alcanceEsperado.equals(claims.get("alcance", String.class))) {
            throw new JwtException("Ticket de alcance incorrecto");
        }
        if (claims.getExpiration() == null || claims.getExpiration().before(new Date())) {
            throw new JwtException("Ticket vencido");
        }
        if (claims.getSubject() == null || claims.getSubject().isBlank()) {
            throw new JwtException("Ticket sin sujeto");
        }
        return claims.getSubject();
    }

    public Date fechaEmision(String token) {
        return extractClaim(token, Claims::getIssuedAt);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            logger.warn("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    public long getExpirationTimeInSeconds() {
        return jwtExpiration / 1000;
    }

    public long getRefreshExpirationTimeInSeconds() {
        return refreshExpiration / 1000;
    }
}
