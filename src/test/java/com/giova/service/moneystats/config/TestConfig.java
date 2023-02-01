package com.giova.service.moneystats.config;

import com.giova.service.moneystats.authentication.entity.UserEntity;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@ComponentScan(basePackages = "io.github.giovannilamarmora.utils")
@Profile("test")
@Configuration
public class TestConfig {

    @Bean
    public UserEntity user() {
        return Mockito.mock(UserEntity.class);
    }
}
