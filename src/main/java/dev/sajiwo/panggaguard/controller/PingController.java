package dev.sajiwo.panggaguard.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping(path = "/ping")
public class PingController {

  @GetMapping("/public")
  public Mono<ResponseEntity<String>> pingPublic() {
    return Mono.just(ResponseEntity.ok("pong"));
  }

  @GetMapping("/private")
  public Mono<ResponseEntity<String>> pingPrivate() {
    Mono<ResponseEntity<String>> result = ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .switchIfEmpty(Mono.error(new RuntimeException("Unauthorized: No authentication found in context")))
        .map(data -> ResponseEntity.ok("pong"));
    return result;
  }

}
