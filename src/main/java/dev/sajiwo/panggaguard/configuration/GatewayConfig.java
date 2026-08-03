package dev.sajiwo.panggaguard.configuration;

import java.util.List;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.AntPathMatcher;

import dev.sajiwo.panggaguard.entity.Route;
import dev.sajiwo.panggaguard.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class GatewayConfig {

  private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
  private static final String[] EXCLUDED_PATHS = { "/auth/**" };

  private final RouteRepository routeRepository;

  @Bean
  RouteLocator routeLocator(RouteLocatorBuilder routeLocatorBuilder) {
    List<Route> routes = routeRepository.fetchAllApi();

    RouteLocatorBuilder.Builder builder = routeLocatorBuilder.routes();

    routes.forEach(config -> {
      log.info("config {}", config);
      builder.route(config.getId().toString(), r -> r
          .header("x-target-domain", config.getDomain())
          .and()
          .predicate(exchange -> {
            String path = exchange.getRequest().getPath().value();
            for (String pattern : EXCLUDED_PATHS) {
              if (PATH_MATCHER.match(pattern, path)) {
                return false;
              }
            }
            return true;
          })
          .filters(f -> f.filter((exchange, chain) -> {
            String path = exchange.getRequest().getPath().value();
            if (!PATH_MATCHER.match("/public/**", path)) {
              boolean hasAuthHeader = exchange.getRequest().getHeaders().containsHeader(HttpHeaders.AUTHORIZATION);
              boolean hasAuthCookie = exchange.getRequest().getCookies().containsKey("accessToken");

              if (!hasAuthHeader && !hasAuthCookie) {
                log.warn("Rejected unauthenticated request to {}", exchange.getRequest().getURI());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
              }
            }

            return chain.filter(exchange);
          }))
          .uri(config.getUri()));
    });

    return builder.build();
  }
}
