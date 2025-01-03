package com.giova.service.moneystats.api.accessSphere;

import com.giova.service.moneystats.api.accessSphere.dto.shared.User;
import io.github.giovannilamarmora.utils.generic.Response;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.github.giovannilamarmora.utils.utilities.Utilities;
import io.github.giovannilamarmora.utils.webClient.UtilsUriBuilder;
import io.github.giovannilamarmora.utils.webClient.WebClientRest;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Logged
public class AccessSphereClient {
  private final WebClientRest webClientRest = new WebClientRest();

  @Value(value = "${rest.client.access-sphere.client-id}")
  private String clientID;

  @Value(value = "${rest.client.access-sphere.token}")
  private String registration_token;

  @Value(value = "${rest.client.access-sphere.baseUrl}")
  private String accessSphereBaseUrl;

  @Value(value = "${rest.client.access-sphere.userInfo}")
  private String getUserInfoUrl;

  @Value(value = "${rest.client.access-sphere.register}")
  private String registerUrl;

  @Autowired private WebClient.Builder builder;

  @PostConstruct
  void init() {
    webClientRest.setBaseUrl(accessSphereBaseUrl);
    webClientRest.init(builder);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.EXTERNAL)
  public Mono<ResponseEntity<Response>> getUserInfo(
      String access_token, String sessionId, Boolean getUserData) {
    Map<String, Object> params = new HashMap<>();
    if (getUserData) params.put("include_user_data", true);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    headers.add(HttpHeaders.AUTHORIZATION, access_token);
    if (!Utilities.isNullOrEmpty(sessionId)) headers.add("Session-ID", sessionId);

    return webClientRest.perform(
        HttpMethod.GET,
        UtilsUriBuilder.buildUri(getUserInfoUrl, params),
        null,
        headers,
        Response.class);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.EXTERNAL)
  public Mono<ResponseEntity<Response>> register(User user) {
    Map<String, Object> params = new HashMap<>();
    params.put("client_id", clientID);
    params.put("registration_token", registration_token);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

    return webClientRest.perform(
        HttpMethod.POST,
        UtilsUriBuilder.buildUri(registerUrl, params),
        user,
        headers,
        Response.class);
  }
}
