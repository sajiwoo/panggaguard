package dev.sajiwo.panggaguard.service.impl;

import java.util.Map;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import dev.sajiwo.panggaguard.entity.MailingConfig;
import dev.sajiwo.panggaguard.enumeration.Mailing;
import dev.sajiwo.panggaguard.repository.MailingRepository;
import dev.sajiwo.panggaguard.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailServiceImpl implements EmailService {

  private final JavaMailSender javaMailSender;
  private final MailingRepository mailingRepository;

  @Override
  public Mono<Boolean> sendMail(Mailing mailing, String email, Map<String, Object> additionalParam) {
    return Mono.fromCallable(() -> {
      log.info("Preparing to send {} email to {}", mailing, email);

      MailingConfig template = mailingRepository.findById(mailing.name())
          .orElseThrow(() -> new IllegalArgumentException("Mailing template not found in database: " + mailing));

      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(email);
      message.setSubject(template.getSubject());
      message.setFrom(template.getSender());
      
      String body = template.getMessage();
      if (additionalParam != null) {
          for (Map.Entry<String, Object> entry : additionalParam.entrySet()) {
              body = body.replace("{{" + entry.getKey() + "}}", String.valueOf(entry.getValue()));
          }
      }
      message.setText(body);

      javaMailSender.send(message);
      log.info("Email successfully sent to {}", email);
      return true;
    })
        .subscribeOn(Schedulers.boundedElastic())
        .onErrorResume(e -> {
          log.error("Failed to send email to {}: {}", email, e.getMessage(), e);
          return Mono.just(false);
        });
  }
}
