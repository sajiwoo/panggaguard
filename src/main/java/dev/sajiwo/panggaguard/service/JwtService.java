package dev.sajiwo.panggaguard.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
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
import io.jsonwebtoken.JwtBuilder;
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

    String token = build(jti, claims, user.getEmail()).compact();
    JsonWebToken jwt = new JsonWebToken();
    jwt.setJti(jti);
    jwt.setBearerToken(token);

    return jwt;
  }

  public String generateToken(OAuth2User oAuth2User) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("name", oAuth2User.getAttribute("name"));
    String jti = UUID.randomUUID().toString();

    return build(jti, claims, oAuth2User.getName()).compact();
  }

  public String extractTokenId(String token) {
    Claims claims = decode(token);
    return claims.get("jti").toString();
  }

  public String extractUsername(String token) {
    return decode(token).getSubject();
  }

  public JwtBuilder build(String jti, Map<String, Object> claims, String subject) {
    LocalDateTime expiration = LocalDateTime.now().plusHours(expiryTimeHours);
    return Jwts.builder()
        .id(jti)
        .subject(subject)
        .claims(claims)
        .issuedAt(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()))
        .expiration(Date.from(expiration.atZone(ZoneId.systemDefault()).toInstant()))
        .signWith(key);

  }

  public Claims decode(String token) {
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

}
