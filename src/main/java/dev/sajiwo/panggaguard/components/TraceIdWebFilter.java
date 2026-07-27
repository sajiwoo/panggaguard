package dev.sajiwo.panggaguard.components;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import dev.sajiwo.panggaguard.utilities.TraceContextAccessor;
import reactor.core.publisher.Mono;

@Component
@Order(0)
public class TraceIdWebFilter implements WebFilter {

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    exchange.getResponse().beforeCommit(() -> {
      String traceId = TraceContextAccessor.getCurrentTraceId();
      if (traceId != null) {
        exchange.getResponse().getHeaders().add("X-Trace-Id", traceId);
        exchange.getResponse().getHeaders().add("X-Request-Id", traceId);
      }
      return Mono.empty();
    });
    return chain.filter(exchange);
  }
}
