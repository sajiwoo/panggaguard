package dev.sajiwo.panggaguard.service.impl;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import dev.sajiwo.panggaguard.dto.JsonWebToken;
import dev.sajiwo.panggaguard.dto.response.DataResponse;
import dev.sajiwo.panggaguard.entity.User;
import dev.sajiwo.panggaguard.entity.UserActivity;
import dev.sajiwo.panggaguard.repository.UserActivityRepository;
import dev.sajiwo.panggaguard.repository.UserRepository;
import dev.sajiwo.panggaguard.service.JwtService;
import dev.sajiwo.panggaguard.service.Oauth2ProviderService;
import dev.sajiwo.panggaguard.utilities.DataResponses;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RequiredArgsConstructor
@Service
public class Oauth2ProviderServiceImpl implements Oauth2ProviderService {

  private final UserRepository userRepository;
  private final UserActivityRepository activityRepository;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void registerOauth2User(OAuth2User oauth2User) {
    Map<String, Object> attrs = oauth2User.getAttributes();

    String email = attrs.getOrDefault("email", "").toString();
    boolean existsByEmail = userRepository.existsByEmail(email);

    if (!existsByEmail) {
      User newUser = new User();
      newUser.setEmail(attrs.getOrDefault("email", "").toString());
      newUser.setFirstName(attrs.getOrDefault("name", "").toString());
      newUser.setPassword(passwordEncoder.encode("User123"));
      newUser.setRole("official");
      newUser = userRepository.save(newUser);

      UserActivity newActivity = new UserActivity();
      newActivity.setUserId(newUser.getId().toString());
      newActivity.setType("first time sigin with " + attrs.getOrDefault("iss", ""));
      newActivity.setCreatedAt(LocalDateTime.now());
      newActivity.setCreatedBy(newUser.getId().toString());
      activityRepository.save(newActivity);
    }
  }

  @Override
  public Mono<ResponseEntity<DataResponse<?>>> oauth2Login(OAuth2User oauth2User) {
    return Mono.fromCallable(() -> userRepository.findByEmail(oauth2User.getAttribute("email").toString()))
        .subscribeOn(Schedulers.boundedElastic()).flatMap(Mono::justOrEmpty)
        .switchIfEmpty(Mono.error(new RuntimeException("user not exists")))
        .flatMap(user -> {
          JsonWebToken jwt = jwtService.generateToken(user);

          UserActivity activity = new UserActivity();
          activity.setId(jwt.getJti());
          activity.setUserId(user.getId().toString());
          activity.setType("signin");
          activityRepository.save(activity);

          return Mono.just(jwt);
        }).map(data -> DataResponses.ok(data))
        .map(data -> ResponseEntity.ok(data));
  }

}
