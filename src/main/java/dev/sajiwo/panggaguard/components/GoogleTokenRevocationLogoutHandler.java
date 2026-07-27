package dev.sajiwo.panggaguard.components;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.ServerLogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleTokenRevocationLogoutHandler implements ServerLogoutHandler {

  private final ReactiveOAuth2AuthorizedClientService authorizedClientService;
  private final WebClient webClient = WebClient.create();

  @Override
  public Mono<Void> logout(WebFilterExchange exchange, Authentication authentication) {
    if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
      String clientRegistrationId = oauthToken.getAuthorizedClientRegistrationId();
      String principalName = oauthToken.getName();

      return authorizedClientService.loadAuthorizedClient(clientRegistrationId, principalName)
          .map(client -> client.getAccessToken().getTokenValue())
          .flatMap(token -> {
            log.info("Revoking Google OAuth2 token for user: {}", principalName);
            return webClient.post()
                .uri("https://oauth2.googleapis.com/revoke?token=" + token)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(e -> {
                  log.error("Failed to revoke Google token", e);
                  return Mono.empty();
                });
          })
          .then(authorizedClientService.removeAuthorizedClient(clientRegistrationId, principalName))
          .then();
    }
    
    return Mono.empty();
  }
}
