package dev.sajiwo.panggaguard.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import dev.sajiwo.panggaguard.dto.JsonWebToken;
import dev.sajiwo.panggaguard.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

  private final SecretKey key;
  private final long expiryTimeHours;

  public JwtService(
      @Value("${add-config.token.jwt.secret}") String secret,
      @Value("${add-config.token.jwt.expiry-time-h}") long expiryTimeHours) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes());
    this.expiryTimeHours = expiryTimeHours;
  }

  public JsonWebToken generateToken(User user) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", user.getRole());
    String jti = UUID.randomUUID().toString();

    String token = Jwts.builder()
        .id(jti)
        .subject(user.getFirstName())
        .claims(claims)
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * expiryTimeHours))
        .signWith(key)
        .compact();

    JsonWebToken jwt = new JsonWebToken();
    jwt.setJti(jti);
    jwt.setBearerToken(token);

    return jwt;
  }

  public String generateToken(OAuth2User oAuth2User) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("name", oAuth2User.getAttribute("name"));
    return Jwts.builder()
        .id(UUID.randomUUID().toString())
        .subject(oAuth2User.getName())
        .claims(claims)
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * expiryTimeHours))
        .signWith(key)
        .compact();
  }

  public Claims decode(String token) {
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public String extractTokenId(String token) {
    Claims claims = Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();
    return claims.get("jti").toString();
  }

  public String extractUsername(String token) {
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .getSubject();
  }

}
