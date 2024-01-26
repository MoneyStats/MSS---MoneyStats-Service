package com.giova.service.moneystats.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.*;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ObjectUtils;

@Configuration
public class SwaggerConfig {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
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
                              String fileName = getExampleFileName(content.getExample().toString());
                              LOG.debug(
                                  "Content {} and FileName {}", content.getExample(), fileName);
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
    Resource resource = resourceLoader.getResource("classpath:/");

    LOG.debug("The Resource URI is {}", resource.getURI());

    Path resourcesPath;

    if (resource.getURI().getScheme().equals("jar")) {
      URL resourceUrl = SwaggerConfig.class.getProtectionDomain().getCodeSource().getLocation();
      LOG.debug(
          "The Resource Path for scheme {} is {} and jar path {}, resource url {}",
          resource.getURI().getScheme(),
          resource.getURI(),
          resource.getURI().getPath(),
          resourceUrl);
      String path = resourceUrl.getPath();
      if (path.endsWith("/")) {
        // Remove trailing "/"
        path = path.substring(0, path.length() - 1);
      }
      String jarPath = path.substring(5, path.indexOf("!"));

      LOG.debug("JarPath {}", jarPath);

      try (JarFile jarFile = new JarFile(jarPath)) {
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
          JarEntry entry = entries.nextElement();
          if (!entry.isDirectory() && entry.getName().endsWith(fileName)) {
            // Found the entry matching the folder and file name
            try (InputStream entryStream = jarFile.getInputStream(entry)) {
              Path tempFile = Files.createTempFile("jar-entry", null);
              Files.copy(entryStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
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

      try (Stream<Path> paths = Files.walk(resourcesPath)) {
        Predicate<Path> validatePath =
            path -> path != null && path.toFile().isFile() && path.toString().endsWith(fileName);
        return paths.filter(validatePath).findFirst().orElse(null);
      }
    }
    return null;
  }
}
