package dev.sajiwo.panggaguard.service;

import java.util.Map;
import dev.sajiwo.panggaguard.enumeration.Mailing;
import reactor.core.publisher.Mono;

public interface EmailService {

  Mono<Boolean> sendMail(Mailing mailing, String email, Map<String, Object> additionalParam);

}
