package com.giova.service.moneystats.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import io.github.giovannilamarmora.utils.config.OpenAPIConfig;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Paths;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.EnableScheduling;

@ComponentScan(basePackages = "io.github.giovannilamarmora.utils")
@Configuration
@EnableScheduling
@EnableCaching
@OpenAPIDefinition(
    info = @Info(title = "MoneyStats Swagger", version = "1.0.0"),
    security = {@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)})
@SecurityScheme(
    type = SecuritySchemeType.APIKEY,
    name = HttpHeaders.AUTHORIZATION,
    in = SecuritySchemeIn.HEADER)
public class AppConfig {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
  @Autowired private ResourceLoader resourceLoader;

  @Bean
  public UserEntity user() {
    return new UserEntity();
  }

  @Bean
  public OpenApiCustomiser applyStandardOpenAPIModifications() {
    return openApi -> {
      Paths paths = new Paths();
      openApi.getPaths().entrySet().stream()
          .sorted(Map.Entry.comparingByKey())
          .forEach(
              entry ->
                  paths.addPathItem(
                      entry.getKey(),
                      OpenAPIConfig.addJSONExamplesOnResource(entry.getValue(), resourceLoader)));
      openApi.setPaths(paths);
    };
  }
}
