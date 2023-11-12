package com.giova.service.moneystats.settings;

import com.giova.service.moneystats.settings.entity.UserSettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IUserSettingsDAO extends JpaRepository<UserSettingEntity, Long> {}
