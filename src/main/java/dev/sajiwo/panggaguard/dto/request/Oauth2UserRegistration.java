package dev.sajiwo.panggaguard.dto.request;

import org.springframework.security.oauth2.core.user.OAuth2User;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Oauth2UserRegistration extends InternalBaseRequest {

  private OAuth2User user;

  private String role;

}
