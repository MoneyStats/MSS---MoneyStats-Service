package com.giova.service.moneystats.config;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ObjectUtils;

@Configuration
public class SwaggerConfig {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  @Autowired private ResourceLoader resourceLoader;

  @Bean
  public OpenApiCustomiser applyStandardOpenAPIModifications() {
    return openApi -> {
      Paths paths = new Paths();
      openApi.getPaths().entrySet().stream()
          .sorted(Map.Entry.comparingByKey())
          .forEach(entry -> paths.addPathItem(entry.getKey(), addExamples(entry.getValue())));
      openApi.setPaths(paths);
    };
  }

  private PathItem addExamples(PathItem pathItem) {
    if (!ObjectUtils.isEmpty(pathItem.getGet())) {
      addOperations(pathItem.getGet());
    }
    if (!ObjectUtils.isEmpty(pathItem.getPost())) {
      addOperations(pathItem.getPost());
    }
    if (!ObjectUtils.isEmpty(pathItem.getPatch())) {
      addOperations(pathItem.getPatch());
    }
    if (!ObjectUtils.isEmpty(pathItem.getPut())) {
      addOperations(pathItem.getPut());
    }
    if (!ObjectUtils.isEmpty(pathItem.getDelete())) {
      addOperations(pathItem.getDelete());
    }
    return pathItem;
  }

  private void addOperations(Operation operation) {
    if (!ObjectUtils.isEmpty(operation) && !ObjectUtils.isEmpty(operation.getResponses())) {
      operation
          .getResponses()
          .forEach(
              (statusCode, apiResponse) -> {
                if (!ObjectUtils.isEmpty(apiResponse.getContent())) {
                  apiResponse
                      .getContent()
                      .values()
                      .forEach(
                          content -> {
                            try {
                              if (ObjectUtils.isEmpty(content.getExample())) return;
                              LOG.debug("Content name is {}", content.getExample());
                              String fileName = getExampleFileName(content.getExample().toString());
                              LOG.debug("FileName is {}", fileName);
                              String jsonContent = getFilePathFromResources(fileName);
                              if (jsonContent != null) {
                                content.setExample(jsonContent);
                              }
                            } catch (Exception e) {
                              LOG.error(
                                  "An Exception occurred during read filename for Open API", e);
                            }
                          });
                }
              });
    }
  }

  private String getExampleFileName(String fileName) {
    return fileName.replaceFirst("@", "");
  }

  private String getFilePathFromResources(String fileName) throws IOException {
    Path path = getResourcePath(fileName);
    LOG.debug("The Path is {}", path);
    return path != null ? new String(Files.readAllBytes(path)) : null;
  }

  private Path getResourcePath(String fileName) throws IOException {
    Resource resource = resourceLoader.getResource("classpath:");

    try (Stream<Path> paths = Files.walk(Path.of(resource.getURI()))) {
      return paths
          .filter(path -> path.toFile().isFile() && path.toString().endsWith(fileName))
          .findFirst()
          .orElse(null);
    }
  }
}
