package dev.sajiwo.panggaguard.components;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.ServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;

import dev.sajiwo.panggaguard.repository.UserActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class LogoutHandler implements ServerLogoutHandler, ServerLogoutSuccessHandler {

  private final UserActivityRepository activityRepository;

  @Override
  public Mono<Void> logout(WebFilterExchange exchange, Authentication authentication) {
    if (authentication != null && authentication.getName() != null) {
      activityRepository.invalidatedTokenId(authentication.getName());
    }

    return Mono.empty();
  }

  @Override
  public Mono<Void> onLogoutSuccess(WebFilterExchange exchange, Authentication authentication) {
    ServerHttpResponse response = exchange.getExchange().getResponse();
    org.springframework.http.server.reactive.ServerHttpRequest request = exchange.getExchange().getRequest();

    request.getCookies().keySet().forEach(cookieName -> {
      ResponseCookie cookieToDelete = ResponseCookie
          .from(cookieName, "")
          .path("/")
          .maxAge(0)
          .build();

      response.addCookie(cookieToDelete);
    });

    response.setStatusCode(HttpStatus.OK);

    return Mono.empty();
  }
}
