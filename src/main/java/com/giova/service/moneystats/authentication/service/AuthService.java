package com.giova.service.moneystats.authentication.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.giova.service.moneystats.api.accessSphere.AccessSphereClient;
import com.giova.service.moneystats.api.accessSphere.dto.shared.User;
import com.giova.service.moneystats.authentication.AuthException;
import com.giova.service.moneystats.authentication.AuthMapper;
import com.giova.service.moneystats.authentication.dto.UserData;
import com.giova.service.moneystats.exception.config.ExceptionCode;
import com.giova.service.moneystats.exception.config.ExceptionMap;
import com.giova.service.moneystats.utilities.RegEx;
import com.giova.service.moneystats.utilities.Utils;
import io.github.giovannilamarmora.utils.context.TraceUtils;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.exception.dto.ExceptionResponse;
import io.github.giovannilamarmora.utils.generic.Response;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.github.giovannilamarmora.utils.utilities.Mapper;
import io.github.giovannilamarmora.utils.utilities.ObjectToolkit;
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
  public Mono<ResponseEntity<Response>> register(User user, String invitationCode) {

    if (!registerToken.equalsIgnoreCase(invitationCode)) {
      LOG.error("Invitation code: {}, is wrong", invitationCode);
      throw new AuthException(
          ExceptionMap.ERR_AUTH_MSS_005, ExceptionMap.ERR_AUTH_MSS_005.getMessage());
    }

    if (!Utils.checkCharacterAndRegexValid(user.getPassword(), RegEx.PASSWORD_FULL.getValue())) {
      LOG.error("Invalid regex for field password for user {}", user.getUsername());
      throw new AuthException(ExceptionMap.ERR_AUTH_MSS_009, "Invalid regex for field password");
    }

    if (!Utils.checkCharacterAndRegexValid(user.getEmail(), RegEx.EMAIL.getValue())) {
      LOG.error("Invalid regex for field email for user {}", user.getUsername());
      throw new AuthException(ExceptionMap.ERR_AUTH_MSS_009, "Invalid regex for field email");
    }

    return accessSphereClient
        .register(user)
        .flatMap(
            responseEntity -> {
              if (responseEntity.getStatusCode().isError()
                  || ObjectToolkit.areNullOrEmpty(
                      responseEntity,
                      ObjectToolkit.lift(ResponseEntity<Response>::getBody),
                      ObjectToolkit.lift(Response::getData))) {
                throw new AuthException(
                    ExceptionMap.ERR_AUTH_MSS_401, ExceptionMap.ERR_AUTH_MSS_401.getMessage());
              }
              User userResponse =
                  Mapper.convertObject(responseEntity.getBody().getData(), User.class);

              String message = "User: " + user.getUsername() + " Successfully registered!";

              Response response =
                  new Response(
                      HttpStatus.OK.value(), message, TraceUtils.getSpanID(), userResponse);

              return Mono.just(ResponseEntity.ok(response));
            })
        .onErrorResume(
            throwable -> {
              if (throwable instanceof UtilsException exception) {
                if (ObjectToolkit.isInstanceOf(
                    exception.getExceptionMessage(), new TypeReference<ExceptionResponse>() {})) {
                  ExceptionResponse exceptionResponse =
                      Mapper.readObject(exception.getExceptionMessage(), ExceptionResponse.class);
                  throw new AuthException(
                      ExceptionMap.ERR_AUTH_MSS_400,
                      ExceptionCode.ACCESS_SPHERE_EXCEPTION,
                      exceptionResponse.getError().getMessage());
                }
              }
              return Mono.error(throwable);
            });
  }
}
