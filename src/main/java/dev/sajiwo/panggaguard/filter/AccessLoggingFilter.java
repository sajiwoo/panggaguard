package dev.sajiwo.panggaguard.filter;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.reactivestreams.Publisher;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
public class AccessLoggingFilter implements WebFilter {

  public static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SSSSS");

  private static final ObjectMapper objectMapper = JsonMapper.builder()
      .addModule(new JavaTimeModule())
      .build();

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    final LocalDateTime requestTimestamp = LocalDateTime.now();
    ServerHttpRequest request = exchange.getRequest();

    Map<String, Object> commonRequestFields = commonRequestLoggingFields(request, requestTimestamp);

    boolean hasBody = (request.getHeaders().getContentLength() > 0 ||
        request.getHeaders().getContentType() != null ||
        "chunked".equalsIgnoreCase(request.getHeaders().getFirst("Transfer-Encoding")));

    ByteArrayOutputStream requestBodyStream = new ByteArrayOutputStream();
    ServerHttpRequestDecorator decoratedRequest = new ServerHttpRequestDecorator(request) {
      @Override
      public Flux<DataBuffer> getBody() {
        return super.getBody()
            .doOnNext(dataBuffer -> {
              try {
                ByteBuffer byteBuffer = dataBuffer.toByteBuffer(dataBuffer.readPosition(),
                    dataBuffer.readableByteCount());
                byte[] content = new byte[byteBuffer.remaining()];
                byteBuffer.get(content);
                requestBodyStream.write(content);
              } catch (Exception e) {
                log.error("Failed reading request data buffer: {}", e.getMessage());
              }
            })
            .doOnComplete(() -> {
              try {
                Map<String, Object> logData = new HashMap<>(commonRequestFields);
                String bodyStr = requestBodyStream.toString(StandardCharsets.UTF_8);
                if (StringUtils.hasText(bodyStr)) {
                  logData.put("request_body", bodyStr);
                }
                log.info("{}", objectMapper.writeValueAsString(logData));
              } catch (JsonProcessingException e) {
                log.error("Failed writing JSON for request: {}", e.getMessage());
              }
            })
            .doOnError(e -> {
              try {
                Map<String, Object> logData = new HashMap<>(commonRequestFields);
                logData.put("error", e.getMessage());
                log.info("{}", objectMapper.writeValueAsString(logData));
              } catch (JsonProcessingException ex) {
                log.error("Failed writing JSON onError request: {}", ex.getMessage());
              }
            });
      }
    };

    if (!hasBody) {
      try {
        log.info("{}", objectMapper.writeValueAsString(commonRequestFields));
      } catch (JsonProcessingException e) {
        log.error("Failed writing JSON for request metadata: {}", e.getMessage());
      }
    }

    ByteArrayOutputStream responseBodyStream = new ByteArrayOutputStream();
    AtomicBoolean responseLogged = new AtomicBoolean(false);
    ServerHttpResponse response = exchange.getResponse();

    ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(response) {
      @Override
      @SuppressWarnings("unchecked")
      public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        if (body instanceof Flux) {
          Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
          return super.writeWith(fluxBody
              .doOnNext(dataBuffer -> {
                try {
                  ByteBuffer byteBuffer = dataBuffer.toByteBuffer(dataBuffer.readPosition(),
                      dataBuffer.readableByteCount());
                  byte[] content = new byte[byteBuffer.remaining()];
                  byteBuffer.get(content);
                  responseBodyStream.write(content);
                } catch (Exception e) {
                  log.error("Failed reading response buffer: {}", e.getMessage());
                }
              })
              .doOnComplete(() -> logResponse(this, requestTimestamp,
                  responseBodyStream.toString(StandardCharsets.UTF_8), responseLogged)));
        } else if (body instanceof Mono) {
          Mono<? extends DataBuffer> monoBody = (Mono<? extends DataBuffer>) body;
          return super.writeWith(monoBody
              .doOnSuccess(dataBuffer -> {
                if (dataBuffer != null) {
                  try {
                    ByteBuffer byteBuffer = dataBuffer.toByteBuffer(dataBuffer.readPosition(),
                        dataBuffer.readableByteCount());
                    byte[] content = new byte[byteBuffer.remaining()];
                    byteBuffer.get(content);
                    responseBodyStream.write(content);
                  } catch (Exception e) {
                    log.error("Failed reading response buffer: {}", e.getMessage());
                  }
                }
                logResponse(this, requestTimestamp, responseBodyStream.toString(StandardCharsets.UTF_8),
                    responseLogged);
              }));
        }
        return super.writeWith(body);
      }

      @Override
      public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
        return super.writeAndFlushWith(body);
      }

      @Override
      public Mono<Void> setComplete() {
        logResponse(this, requestTimestamp, responseBodyStream.toString(StandardCharsets.UTF_8), responseLogged);
        return super.setComplete();
      }
    };

    ServerWebExchange decoratedExchange = exchange.mutate()
        .request(hasBody ? decoratedRequest : request)
        .response(decoratedResponse)
        .build();

    decoratedExchange.getResponse().beforeCommit(() -> {
      logResponse(decoratedResponse, requestTimestamp, responseBodyStream.toString(StandardCharsets.UTF_8),
          responseLogged);
      return Mono.empty();
    });

    return chain.filter(decoratedExchange)
        .doOnSuccess(aVoid -> logResponse(decoratedResponse, requestTimestamp,
            responseBodyStream.toString(StandardCharsets.UTF_8), responseLogged))
        .doOnError(throwable -> logResponse(decoratedResponse, requestTimestamp,
            responseBodyStream.toString(StandardCharsets.UTF_8), responseLogged));
  }

  private void logResponse(ServerHttpResponse response, LocalDateTime requestTimestamp, String responseBody,
      AtomicBoolean responseLogged) {
    if (responseLogged.compareAndSet(false, true)) {
      try {
        final LocalDateTime responseTimestamp = LocalDateTime.now();

        Map<String, Object> httpData = new HashMap<>();
        httpData.put("context", "http_response");
        httpData.put("timestamp", dtf.format(responseTimestamp));
        httpData.put("response_time", Duration.between(requestTimestamp, responseTimestamp));
        httpData.put("status_code", response.getStatusCode() != null ? response.getStatusCode().value() : 200);
        httpData.put("response_headers", headersToMap(response.getHeaders()));
        if (StringUtils.hasText(responseBody)) {
          httpData.put("response_body", responseBody);
        }

        log.info("{}", objectMapper.writeValueAsString(httpData));

      } catch (Exception e) {
        log.error("Failed to log http_response: {}", e.getMessage());
      }
    }
  }

  private Map<String, Object> commonRequestLoggingFields(ServerHttpRequest request, LocalDateTime requestTimestamp) {
    Map<String, Object> data = new HashMap<>();
    data.put("url", request.getURI().toString());
    data.put("method", request.getMethod().toString());
    data.put("context", "http_request");
    data.put("timestamp", dtf.format(requestTimestamp));

    if (request.getRemoteAddress() != null && request.getRemoteAddress().getAddress() != null) {
      data.put("remote_address", request.getRemoteAddress().getAddress().getHostAddress());
      data.put("remote_host", request.getRemoteAddress().getHostName());
      data.put("remote_port", request.getRemoteAddress().getPort());
    }

    data.put("request_headers", headersToMap(request.getHeaders()));
    data.put("request_parameter", request.getURI().getQuery());

    return data;
  }

  private static Map<String, String> headersToMap(HttpHeaders headers) {
    Map<String, String> map = new HashMap<>();
    headers.forEach((key, values) -> map.put(key, String.join(", ", values)));
    return map;
  }
}
