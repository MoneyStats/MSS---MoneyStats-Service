package com.giova.service.moneystats.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.Map;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
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
                                  searchFileFromResources(fileName, resourceLoader);
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

  @LogInterceptor(type = LogTimeTracker.ActionType.UTILS_LOGGER)
  public String searchFileFromResources(String fileName, ResourceLoader resourceLoader)
      throws IOException {
    Path path = getResourcePath(fileName, resourceLoader);
    return path != null ? new String(java.nio.file.Files.readAllBytes(path)) : null;
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.UTILS_LOGGER)
  public Path getResourcePath(String fileName, ResourceLoader resourceLoader) throws IOException {
    Resource resource = resourceLoader.getResource("classpath:/");

    LOG.debug("The Resource URI is {}", resource.getURI());

    Path resourcesPath = null;

    if (resource.getURI().getScheme().equals("jar")) {
      // URL resourceUrl = FilesUtils.class.getProtectionDomain().getCodeSource().getLocation();
      String path = resource.getURI().getPath();
      if (path.endsWith("/")) path = path.substring(0, path.length() - 1);
      String jarPath = path.substring(5, path.indexOf("!"));

      try (JarFile jarFile = new JarFile(jarPath)) {
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
          JarEntry entry = entries.nextElement();
          if (!entry.isDirectory() && entry.getName().endsWith(fileName)) {
            // Found the entry matching the folder and file name
            try (InputStream entryStream = jarFile.getInputStream(entry)) {
              Path tempFile = java.nio.file.Files.createTempFile("jar-entry", null);
              java.nio.file.Files.copy(entryStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
              LOG.debug(
                  "The Resource Path for scheme {} is {}", resource.getURI().getScheme(), tempFile);
              return tempFile;
            }
          }
        }
      }
    } else {
      resourcesPath = Path.of(resource.getURI());
      LOG.debug(
          "The Resource Path for scheme {} is {}",
          resource.getURI().getScheme(),
          resourcesPath.toUri());

      try (Stream<Path> paths = java.nio.file.Files.walk(resourcesPath)) {
        Predicate<Path> validatePath =
            path -> path != null && path.toFile().isFile() && path.toString().endsWith(fileName);
        return paths.filter(validatePath).findFirst().orElse(null);
      }
    }
    LOG.warn("The Resource Path was found");
    return resourcesPath;
  }
}
