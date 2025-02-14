package com.giova.service.moneystats.api.accessSphere;

import com.giova.service.moneystats.api.accessSphere.dto.shared.User;
import io.github.giovannilamarmora.utils.generic.Response;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.github.giovannilamarmora.utils.utilities.ObjectToolkit;
import io.github.giovannilamarmora.utils.webClient.UtilsUriBuilder;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Logged
public class AccessSphereClient extends AccessSphereConfig {

  @LogInterceptor(type = LogTimeTracker.ActionType.EXTERNAL)
  public Mono<ResponseEntity<Response>> getUserInfo(
      String access_token, String sessionId, Boolean getUserData) {
    Map<String, Object> params = new HashMap<>();
    if (getUserData) params.put("include_user_data", true);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    headers.add(HttpHeaders.AUTHORIZATION, access_token);
    if (!ObjectToolkit.isNullOrEmpty(sessionId)) headers.add("Session-ID", sessionId);
    setTracing(headers);

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
    params.put("assign_new_client", true);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    setTracing(headers);

    return webClientRest.perform(
        HttpMethod.POST,
        UtilsUriBuilder.buildUri(registerUrl, params),
        user,
        headers,
        Response.class);
  }
}
