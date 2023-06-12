package com.giova.service.moneystats.authentication;

import com.giova.service.moneystats.authentication.entity.UserEntity;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;

@Component
public class AuthCacheService {

  private static final String USER_CACHE = "Users-Cache";

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  @Autowired private CacheManager cacheManager;
  @Autowired private IAuthDAO authDAO;

  @Cacheable(value = USER_CACHE, key = "#email", condition = "#email!=null")
  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public UserEntity findUserEntityByEmail(String email) {
    LOG.info("[Caching] UserEntity into Database by email {}", email);
    return authDAO.findUserEntityByEmail(email);
  }

  @Caching(
      cacheable = {
        @Cacheable(value = USER_CACHE, key = "#username", condition = "#username!=null"),
        @Cacheable(value = "User", key = "#email", condition = "#email!=null")
      })
  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public UserEntity findUserEntityByUsernameOrEmail(String username, String email) {
    LOG.info("[Caching] UserEntity into Database by username {} and email {}", username, email);
    return authDAO.findUserEntityByUsernameOrEmail(username, email);
  }

  @Caching(evict = @CacheEvict(value = USER_CACHE))
  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public void deleteUserCache() {
    LOG.info("[Caching] Deleting cache for {}", USER_CACHE);
    Objects.requireNonNull(cacheManager.getCache(USER_CACHE)).clear();
  }

  public UserEntity save(UserEntity user) {
    deleteUserCache();
    return authDAO.save(user);
  }

  public UserEntity findUserEntityByTokenReset(String token) {
    return authDAO.findUserEntityByTokenReset(token);
  }

  public List<String> selectDistinctCryptoFiatCurrency() {
    return authDAO.selectDistinctCryptoFiatCurrency();
  }
}
