package dev.sajiwo.panggaguard.configuration;

import java.util.List;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.sajiwo.panggaguard.entity.Route;
import dev.sajiwo.panggaguard.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class GatewayConfig {

  private final RouteRepository routeRepository;

  @Bean
  RouteLocator routeLocator(RouteLocatorBuilder routeLocatorBuilder) {
    List<Route> routes = routeRepository.findAll();

    RouteLocatorBuilder.Builder builder = routeLocatorBuilder.routes();

    routes.forEach(config -> {
      log.info("config {}", config);
      builder.route(r -> r.header("x-target-domain", config.getDomain()).uri(config.getUri()));
    });

    return builder.build();
  }
}
