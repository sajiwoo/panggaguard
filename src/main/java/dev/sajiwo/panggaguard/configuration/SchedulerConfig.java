package dev.sajiwo.panggaguard.configuration;

import java.util.Set;

import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@EnableScheduling
@Configuration
public class SchedulerConfig {
  private final ContextRefresher contextRefresher;

  @Scheduled(fixedRate = (1000 * 60 * 5))
  public void autoRefreshBeans() {
    System.out.println("Starting periodic bean configuration refresh...");

    Set<String> refreshedKeys = contextRefresher.refresh();

    if (!refreshedKeys.isEmpty()) {
      System.out.println("Successfully refreshed keys: " + refreshedKeys);
    } else {
      System.out.println("No configuration changes detected.");
    }
  }
}
