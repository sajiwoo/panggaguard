package dev.sajiwo.panggaguard.utilities;

import org.springframework.stereotype.Component;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TraceContextAccessor {

  private static Tracer tracer;
  private final Tracer injectedTracer;

  @PostConstruct
  void init() {
    TraceContextAccessor.tracer = injectedTracer;
  }

  public static String getCurrentTraceId() {
    if (tracer != null) {
      Span span = tracer.currentSpan();
      if (span != null && span.context() != null) {
        return span.context().traceId();
      }
    }
    return null;
  }
}
