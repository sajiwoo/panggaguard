package dev.sajiwo.panggaguard.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

import dev.sajiwo.panggaguard.dto.response.DataResponse;
import dev.sajiwo.panggaguard.utilities.DataResponses;
import dev.sajiwo.panggaguard.utilities.TraceContextAccessor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalReactiveExceptionHandler {

  @ExceptionHandler(NoResourceFoundException.class)
  public Mono<ResponseEntity<DataResponse<?>>> handleErrorResponseException(NoResourceFoundException exception,
      ServerWebExchange exchange) {
    DataResponse<?> response = DataResponses.badRequest("Invalid Request");
    response.setTraceId(TraceContextAccessor.getCurrentTraceId());
    return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(response));
  }

  @ExceptionHandler(ErrorResponseException.class)
  public Mono<ResponseEntity<DataResponse<?>>> handleErrorResponseException(ErrorResponseException exception,
      ServerWebExchange exchange) {
    DataResponse<?> response = DataResponses.error(HttpStatus.valueOf(exception.getStatusCode().value()), "Failed",
        exception.getMessage());
    response.setTraceId(TraceContextAccessor.getCurrentTraceId());
    return Mono.just(ResponseEntity.status(exception.getStatusCode()).body(response));
  }

  @ExceptionHandler(ServerWebInputException.class)
  public Mono<ResponseEntity<?>> handleServerWebInputException(ServerWebInputException ex, ServerWebExchange exchange) {
    return Mono.just(ResponseEntity.badRequest().build());
  }

  @ExceptionHandler(Exception.class)
  public Mono<ResponseEntity<DataResponse<?>>> handleAllExceptions(Exception ex, ServerWebExchange exchange) {
    log.error("Unhandled exception: ", ex);
    DataResponse<?> response = DataResponses.internalServerError();
    response.setTraceId(TraceContextAccessor.getCurrentTraceId());
    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
  }
}
