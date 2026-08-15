package dev.sajiwo.panggaguard.components;

import java.util.Collections;
import java.util.Optional;

import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import dev.sajiwo.panggaguard.entity.UserActivity;
import dev.sajiwo.panggaguard.repository.UserActivityRepository;
import dev.sajiwo.panggaguard.service.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class JwtReactiveSecurityFilter implements WebFilter {

  private final JwtService jwtService;
  private final UserActivityRepository activityRepository;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String token = null;
    String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

    if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
      token = authHeader.substring(7);
    } else {
      HttpCookie cookie = exchange.getRequest().getCookies().getFirst("accessToken");
      if (cookie != null) {
        token = cookie.getValue();
      } else {
        // Fallback for WebSockets since they cannot send headers
        token = exchange.getRequest().getQueryParams().getFirst("token");
      }
    }

    if (!StringUtils.hasText(token)) {
      return chain.filter(exchange);
    }

    try {
      Claims claims = jwtService.decode(token);
      Optional<UserActivity> activity = activityRepository.findById(claims.get("jti").toString());

      if (activity.isEmpty() || !activity.get().getIsValid()) {
        log.warn("Invalid JWT or Session");
        return chain.filter(exchange);
      }

      UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
          claims.getSubject(), null, Collections.emptyList());
      SecurityContext context = new SecurityContextImpl(auth);

      return chain.filter(exchange)
          .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)));

    } catch (Exception e) {
      log.warn("Failed to decode JWT: {}", e.getMessage());
      return chain.filter(exchange);
    }
  }
}
