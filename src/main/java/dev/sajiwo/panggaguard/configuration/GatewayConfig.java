package dev.sajiwo.panggaguard.configuration;

import java.util.List;

import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

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

  @RefreshScope
  @Bean
  RouteLocator routeLocator(RouteLocatorBuilder routeLocatorBuilder) {
    List<Route> routes = routeRepository.fetchAllApi();

    RouteLocatorBuilder.Builder builder = routeLocatorBuilder.routes();

    routes.forEach(config -> {
      log.info("config {}", config);
      builder.route(config.getId().toString(), r -> r
          .predicate(exchange -> {
            String headerDomain = exchange.getRequest().getHeaders().getFirst("x-target-domain");
            String queryDomain = exchange.getRequest().getQueryParams().getFirst("x-target-domain");
            String targetDomain = StringUtils.hasText(headerDomain) ? headerDomain : queryDomain;
            return config.getDomain().equals(targetDomain);
          })
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
            boolean hasAuthHeader = exchange.getRequest().getHeaders().containsHeader(HttpHeaders.AUTHORIZATION);
            HttpCookie cookie = exchange.getRequest().getCookies().getFirst("accessToken");
            boolean hasAuthCookie = cookie != null;
            String tokenQuery = exchange.getRequest().getQueryParams().getFirst("token");
            boolean hasAuthQuery = StringUtils.hasText(tokenQuery);

            String path = exchange.getRequest().getPath().value();
            if (!PATH_MATCHER.match("/public/**", path)) {
              if (!hasAuthHeader && !hasAuthCookie && !hasAuthQuery) {
                log.warn("Rejected unauthenticated request to {}", exchange.getRequest().getURI());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
              }
            }

            ServerWebExchange mutatedExchange = exchange;
            if (!hasAuthHeader && (hasAuthCookie || hasAuthQuery)) {
              String token = hasAuthCookie ? cookie.getValue() : tokenQuery;
              mutatedExchange = exchange.mutate().request(
                  exchange.getRequest().mutate().header(HttpHeaders.AUTHORIZATION, "Bearer " + token).build()).build();
            }

            return chain.filter(mutatedExchange);
          }))
          .uri(config.getUri()));
    });

    return builder.build();
  }
}
