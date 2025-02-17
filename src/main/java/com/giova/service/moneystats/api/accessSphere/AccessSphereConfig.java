package com.giova.service.moneystats.api.accessSphere;

import io.github.giovannilamarmora.utils.context.TraceUtils;
import io.github.giovannilamarmora.utils.webClient.WebClientRest;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class AccessSphereConfig {
  final WebClientRest webClientRest = new WebClientRest();

  @Value(value = "${rest.client.access-sphere.baseUrl}")
  String accessSphereBaseUrl;

  @Value(value = "${rest.client.access-sphere.userInfo}")
  String getUserInfoUrl;

  @Autowired private WebClient.Builder builder;

  @PostConstruct
  void init() {
    webClientRest.setBaseUrl(accessSphereBaseUrl);
    webClientRest.init(builder);
  }

  void setTracing(HttpHeaders headers) {
    headers.add("Span-ID", TraceUtils.getSpanID());
    headers.add("Parent-ID", TraceUtils.getParentID());
    headers.add("Trace-ID", TraceUtils.getTraceID());
  }
}
