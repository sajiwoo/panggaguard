package dev.sajiwo.panggaguard.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import dev.sajiwo.panggaguard.dto.request.SignUpRequest;
import dev.sajiwo.panggaguard.dto.response.DataResponse;
import dev.sajiwo.panggaguard.entity.User;
import dev.sajiwo.panggaguard.exception.ErrorResponseException;
import dev.sajiwo.panggaguard.mapper.UserMapper;
import dev.sajiwo.panggaguard.repository.UserRepository;
import dev.sajiwo.panggaguard.service.UserService;
import dev.sajiwo.panggaguard.utilities.DataResponses;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;

  @Override
  public Mono<ResponseEntity<DataResponse<?>>> signUp(SignUpRequest request) {
    return Mono.fromCallable(() -> userRepository.findByEmail(request.email()))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(Mono::justOrEmpty)
        .flatMap(user -> Mono.<User>error(new ErrorResponseException(HttpStatus.CONFLICT, "Email sudah terdaftar")))
        .switchIfEmpty(Mono.defer(() -> Mono.fromCallable(() -> {
          User user = userMapper.map(request);
          user.setPassword(passwordEncoder.encode(request.password()));
          return userRepository.save(user);
        }).subscribeOn(Schedulers.boundedElastic())))
        .map(userMapper::map)
        .map(DataResponses::ok)
        .map(ResponseEntity::ok);
  }

}
