package com.giova.service.moneystats.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import io.github.giovannilamarmora.utils.config.OpenAPIConfig;
import io.github.giovannilamarmora.utils.utilities.FilesUtils;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
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
import org.springframework.util.ObjectUtils;

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

  private static String getExampleFileName(String fileName) {
    return fileName.replaceFirst("@", "");
  }

  public PathItem addJSONExamplesOnResource(PathItem pathItem, ResourceLoader resourceLoader) {
    if (!ObjectUtils.isEmpty(pathItem.getGet())) {
      addOperations(pathItem.getGet(), resourceLoader);
    }

    if (!ObjectUtils.isEmpty(pathItem.getPost())) {
      addOperations(pathItem.getPost(), resourceLoader);
    }

    if (!ObjectUtils.isEmpty(pathItem.getPatch())) {
      addOperations(pathItem.getPatch(), resourceLoader);
    }

    if (!ObjectUtils.isEmpty(pathItem.getPut())) {
      addOperations(pathItem.getPut(), resourceLoader);
    }

    if (!ObjectUtils.isEmpty(pathItem.getDelete())) {
      addOperations(pathItem.getDelete(), resourceLoader);
    }

    return pathItem;
  }

  private void addOperations(Operation operation, ResourceLoader resourceLoader) {
    if (!ObjectUtils.isEmpty(operation) && !ObjectUtils.isEmpty(operation.getResponses())) {
      operation
          .getResponses()
          .forEach(
              (statusCode, apiResponse) -> {
                if (!ObjectUtils.isEmpty(apiResponse.getContent())
                    && !ObjectUtils.isEmpty(apiResponse.getContent().values())) {
                  apiResponse
                      .getContent()
                      .values()
                      .forEach(
                          (content) -> {
                            try {
                              if (!content.getExampleSetFlag()) {
                                return;
                              }

                              String fileName = getExampleFileName(content.getExample().toString());
                              LOG.debug("FileName is {}", fileName);
                              String jsonContent =
                                  FilesUtils.searchFileFromResources(fileName, resourceLoader);
                              if (jsonContent != null) {
                                content.setExample(jsonContent);
                              }
                            } catch (Exception var4) {
                              LOG.error(
                                  "An Exception occurred during read filename for Open API", var4);
                            }
                          });
                }
              });
    }
  }

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
                      entry.getKey(), addJSONExamplesOnResource(entry.getValue(), resourceLoader)));
      openApi.setPaths(paths);
    };
  }
}
