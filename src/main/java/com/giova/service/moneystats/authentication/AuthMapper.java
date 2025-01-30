package com.giova.service.moneystats.authentication;

import com.giova.service.moneystats.api.accessSphere.dto.UserInfoResponse;
import com.giova.service.moneystats.api.accessSphere.dto.shared.User;
import com.giova.service.moneystats.authentication.dto.UserData;
import com.giova.service.moneystats.config.roles.AppRole;
import com.giova.service.moneystats.exception.config.ExceptionMap;
import com.giova.service.moneystats.settings.dto.UserSettingDTO;
import com.giova.service.moneystats.utilities.Utils;
import io.github.giovannilamarmora.utils.generic.Response;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.utilities.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AuthMapper {

  private static final Logger LOG = LoggerFactory.getLogger(AuthMapper.class);

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public static UserData mapAccessSphereUserToUserData(User user) {
    UserData userData = new UserData();
    BeanUtils.copyProperties(user, userData);
    if (user.getAttributes() != null && user.getAttributes().containsKey("money_stats_settings"))
      userData.setSettings(
          Mapper.convertObject(
              user.getAttributes().get("money_stats_settings"), UserSettingDTO.class));
    return userData;
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public static Mono<UserData> verifyAndMapAccessSphereResponse(
      ResponseEntity<Response> responseEntity) {
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
    return Mono.just(mapAccessSphereUserToUserData(userInfoResponse.getUser()));
  }
}
