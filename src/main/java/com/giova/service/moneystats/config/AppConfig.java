package com.giova.service.moneystats.config;

import com.giova.service.moneystats.authentication.entity.UserEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@ComponentScan(basePackages = "io.github.giovannilamarmora.utils")
@Configuration
@EnableScheduling
public class AppConfig {

  @Bean
  public UserEntity user() {
    return new UserEntity();
  }
}
