package com.giova.service.moneystats.authentication;

import com.giova.service.moneystats.api.accessSphere.dto.shared.User;
import com.giova.service.moneystats.authentication.dto.UserData;
import com.giova.service.moneystats.settings.dto.UserSettingDTO;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.utilities.Mapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

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
}
