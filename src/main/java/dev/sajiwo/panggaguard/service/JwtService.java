package dev.sajiwo.panggaguard.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import dev.sajiwo.panggaguard.dto.JsonWebToken;
import dev.sajiwo.panggaguard.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

  private final String secret = "supersecretkeythatsisverylongandsecureatleast256bits";
  private final SecretKey key;

  public JwtService() {
    this.key = Keys.hmacShaKeyFor(secret.getBytes());
  }

  public JsonWebToken generateToken(User user) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("email", user.getEmail());
    String jti = UUID.randomUUID().toString();

    String token = Jwts.builder()
        .id(jti)
        .subject(user.getFirstName())
        .claims(claims)
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24 hours
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
        .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24 hours
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

  public String extractUsername(String token) {
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .getSubject();
  }
}
