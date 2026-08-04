package dev.sajiwo.panggaguard.components;

import java.util.Collections;
import java.util.Optional;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
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
@Component
@Order(-100)
@RequiredArgsConstructor
public class JwtReactiveFilter implements WebFilter {

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
      }
    }

    if (!StringUtils.hasText(token)) {
      return chain.filter(exchange);
    }

    try {
      Claims claims = jwtService.decode(token);
      Optional<UserActivity> activity = activityRepository.findById(claims.get("jti").toString());

      if (activity.isEmpty() || !activity.get().getIsValid()) {
        log.warn("Invalid JWT");
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
      }

      UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
          claims.getSubject(), null, Collections.emptyList());
      SecurityContext context = new SecurityContextImpl(auth);

      return chain.filter(exchange)
          .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)));

    } catch (Exception e) {
      exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
      return exchange.getResponse().setComplete();
    }
  }
}
