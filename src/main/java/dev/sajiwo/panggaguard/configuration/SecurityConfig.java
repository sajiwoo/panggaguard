package dev.sajiwo.panggaguard.configuration;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import dev.sajiwo.panggaguard.components.LogoutHandler;
import dev.sajiwo.panggaguard.repository.UserActivityRepository;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

  @Value("${add-config.cors.allow-origins}")
  private String origins;

  @Bean
  SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, UserActivityRepository activityRepository) {
    http.oauth2Login(oauth2 -> oauth2.loginPage("/auth/sign-in/method"));

    LogoutHandler logoutHandler = new LogoutHandler(activityRepository);

    http.logout(logout -> logout
        .requiresLogout(ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, "/sign-out"))
        .logoutHandler(logoutHandler)
        .logoutSuccessHandler(logoutHandler));

    http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

    http.authorizeExchange(
        authz -> authz.pathMatchers("/auth/**", "/oauth2/**", "/ping/public").permitAll().anyExchange()
            .authenticated());

    http.exceptionHandling(ex -> ex
        .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)));

    http.csrf(csrf -> csrf.disable());

    return http.build();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList(origins.split(",")));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

}
