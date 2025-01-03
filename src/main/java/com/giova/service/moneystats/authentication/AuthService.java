package com.giova.service.moneystats.authentication;

import com.fasterxml.jackson.core.type.TypeReference;
import com.giova.service.moneystats.api.accessSphere.AccessSphereClient;
import com.giova.service.moneystats.api.accessSphere.dto.UserInfoResponse;
import com.giova.service.moneystats.api.accessSphere.dto.shared.User;
import com.giova.service.moneystats.authentication.dto.UserData;
import com.giova.service.moneystats.config.AppRole;
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
import io.github.giovannilamarmora.utils.utilities.Utilities;
import java.util.*;
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
public class AuthService {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  @Value(value = "${app.invitationCode}")
  private String registerToken;

  @Autowired private AccessSphereClient accessSphereClient;

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public Mono<UserData> authorize(String access_token, String sessionId) {
    return accessSphereClient
        .getUserInfo(access_token, sessionId, true)
        .flatMap(
            responseEntity -> {
              if (responseEntity.getStatusCode().isError()
                  || responseEntity.getBody() == null
                  || responseEntity.getBody().getData() == null) {
                throw new AuthException(
                    ExceptionMap.ERR_AUTH_MSS_008, ExceptionMap.ERR_AUTH_MSS_008.getMessage());
              }
              UserInfoResponse userInfoResponse =
                  Mapper.convertObject(responseEntity.getBody().getData(), UserInfoResponse.class);
              if (userInfoResponse.getUserInfo().getRoles().stream()
                  .noneMatch(string -> Utils.isEnumValue(string, AppRole.class))) {
                LOG.error(
                    "The current user role {} not match with the app role",
                    userInfoResponse.getUserInfo().getRoles());
                throw new AuthException(
                    ExceptionMap.ERR_AUTH_MSS_010, ExceptionMap.ERR_AUTH_MSS_010.getMessage());
              }
              return Mono.just(
                  AuthMapper.mapAccessSphereUserToUserData(userInfoResponse.getUser()));
            });
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
                  || responseEntity.getBody() == null
                  || responseEntity.getBody().getData() == null) {
                throw new AuthException(
                    ExceptionMap.ERR_AUTH_MSS_008, ExceptionMap.ERR_AUTH_MSS_008.getMessage());
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
                if (Utilities.isInstanceOf(
                    exception.getExceptionMessage(), new TypeReference<ExceptionResponse>() {})) {
                  ExceptionResponse exceptionResponse =
                      Mapper.readObject(exception.getExceptionMessage(), ExceptionResponse.class);
                  throw new AuthException(
                      ExceptionMap.ERR_AUTH_MSS_003, exceptionResponse.getError().getMessage());
                }
              }
              return Mono.error(throwable);
            });
  }
}
