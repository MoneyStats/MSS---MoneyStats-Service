package com.giova.service.moneystats.authentication;

import com.giova.service.moneystats.authentication.dto.User;
import com.giova.service.moneystats.authentication.dto.UserData;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import com.giova.service.moneystats.settings.UserSettingsMapper;
import com.giova.service.moneystats.settings.dto.UserSettingDTO;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.utilities.Mapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

  @Autowired private UserSettingsMapper userSettingsMapper;

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public static UserData mapAccessSphereUserToUserData(
      com.giova.service.moneystats.api.accessSphere.dto.shared.User user) {
    UserData userData = new UserData();
    BeanUtils.copyProperties(user, userData);
    if (user.getAttributes() != null && user.getAttributes().containsKey("money_stats_settings"))
      userData.setSettings(
          Mapper.convertObject(
              user.getAttributes().get("money_stats_settings"), UserSettingDTO.class));
    return userData;
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public UserEntity mapUserToUserEntity(User user) {
    UserEntity userEntity = new UserEntity();
    BeanUtils.copyProperties(user, userEntity);
    userEntity.setSettings(
        userSettingsMapper.fromUserSettingDTOToEntity(user.getSettings(), userEntity));
    return userEntity;
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public User mapUserEntityToUser(UserEntity userEntity) {
    User user = new User();
    BeanUtils.copyProperties(userEntity, user);
    if (userEntity.getSettings() != null)
      user.setSettings(userSettingsMapper.fromUserSettingsEntityToDTO(userEntity.getSettings()));
    return user;
  }
}
