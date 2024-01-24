package com.giova.service.moneystats.config;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ObjectUtils;

@Configuration
public class SwaggerConfig {

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
                              String fileName = getExampleFileName(content.getExample().toString());
                              String jsonContent = readJsonFromFile(fileName);
                              if (jsonContent != null) {
                                content.setExample(jsonContent);
                              }
                            } catch (Exception e) {
                              return;
                            }
                          });
                }
              });
    }
  }

  private String getExampleFileName(String fileName) {
    return fileName.replaceFirst("@", "");
  }

  private String readJsonFromFile(String fileName) throws IOException {
    Path path = getResourcePath(fileName);
    return path != null ? new String(Files.readAllBytes(path)) : null;
  }

  private Path getResourcePath(String fileName) throws IOException {
    URL resourceUrl = getResourceURL();
    if (resourceUrl == null) {
      throw new IOException("Resource folder not found");
    }

    try {
      Path resourcesPath;
      URI uri = resourceUrl.toURI();

      if (uri.getScheme().equals("jar")) {
        FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
        resourcesPath = fileSystem.getPath("/BOOT-INF/classes");
      } else {
        resourcesPath = Path.of(uri);
      }

      try (Stream<Path> paths = Files.walk(resourcesPath)) {
        return paths
            .filter(path -> path.toFile().isFile() && path.toString().endsWith(fileName))
            .findFirst()
            .orElse(null);
      }
    } catch (URISyntaxException e) {
      throw new IOException("Error converting URL to URI", e);
    }
  }

  private URL getResourceURL() {
    ClassLoader classLoader = getClass().getClassLoader();
    return classLoader.getResource("");
  }
}
