package dev.sajiwo.panggaguard.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.sajiwo.panggaguard.dto.response.DataResponse;
import dev.sajiwo.panggaguard.dto.response.UserProfileResponse;
import dev.sajiwo.panggaguard.service.UserService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {

  private final UserService userService;

  @GetMapping
  public Mono<ResponseEntity<DataResponse<UserProfileResponse>>> profile() {
    return userService.profile();
  }

}
