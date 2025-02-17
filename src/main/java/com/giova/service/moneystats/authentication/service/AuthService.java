package com.giova.service.moneystats.authentication.service;

import com.giova.service.moneystats.api.accessSphere.AccessSphereClient;
import com.giova.service.moneystats.authentication.AuthException;
import com.giova.service.moneystats.authentication.AuthMapper;
import com.giova.service.moneystats.authentication.dto.UserData;
import com.giova.service.moneystats.exception.config.ExceptionMap;
import io.github.giovannilamarmora.utils.context.TraceUtils;
import io.github.giovannilamarmora.utils.generic.Response;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Logged
@RequiredArgsConstructor
public class AuthService implements AuthRepository {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  @Value(value = "${rest.client.access-sphere.token}")
  private String registration_token;

  @Value(value = "${app.invitationCode}")
  private String registerToken;

  @Autowired private AccessSphereClient accessSphereClient;

  /**
   * Authorize request to Access Sphere
   *
   * @param access_token to be validated
   * @param sessionId of the request
   * @return User Info
   */
  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public Mono<UserData> authorize(String access_token, String sessionId) {
    return accessSphereClient
        .getUserInfo(access_token, sessionId, true)
        .flatMap(AuthMapper::verifyAndMapAccessSphereResponse);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public Mono<ResponseEntity<Response>> checkRegistrationToken(String invitationCode) {

    if (!registerToken.equalsIgnoreCase(invitationCode)) {
      LOG.error("Invitation code: {}, is wrong", invitationCode);
      throw new AuthException(
          ExceptionMap.ERR_AUTH_MSS_005, ExceptionMap.ERR_AUTH_MSS_005.getMessage());
    }

    String message = "Invitation Code Successfully validated!";
    Map<String, String> token = new HashMap<>();
    token.put("registration_token", registration_token);

    Response response = new Response(HttpStatus.OK.value(), message, TraceUtils.getSpanID(), token);

    return Mono.just(ResponseEntity.ok(response));
  }
}
