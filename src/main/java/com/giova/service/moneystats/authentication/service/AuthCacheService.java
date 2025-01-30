package com.giova.service.moneystats.authentication.service;

import com.giova.service.moneystats.api.accessSphere.AccessSphereClient;
import com.giova.service.moneystats.authentication.AuthMapper;
import com.giova.service.moneystats.authentication.dto.UserData;
import com.giova.service.moneystats.config.cache.RedisCacheConfig;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Mono;

public class AuthCacheService implements AuthRepository {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  @Autowired private ReactiveRedisTemplate<String, UserData> userDataTemplate;
  @Autowired private AccessSphereClient accessSphereClient;

  /**
   * Authorize request to Access Sphere
   *
   * @param access_token to be validated
   * @param sessionId of the request
   * @return User Info
   */
  @Override
  public Mono<UserData> authorize(String access_token, String sessionId) {
    // Chiave di cache basata sui parametri
    String cacheKey = "authorize:" + access_token + ":" + sessionId;

    try {
      return userDataTemplate
          .opsForValue()
          .get(cacheKey)
          .flatMap(
              cachedUserData -> {
                LOG.info("Cache hit for key: {}", cacheKey);
                return Mono.just(cachedUserData);
              })
          .switchIfEmpty(
              Mono.defer(
                      () -> {
                        LOG.info(
                            "[Caching] UserInfo into Access Sphere for session id {}", sessionId);
                        return accessSphereClient
                            .getUserInfo(access_token, sessionId, true)
                            .flatMap(AuthMapper::verifyAndMapAccessSphereResponse);
                      })
                  .doOnSuccess(
                      userData -> {
                        userDataTemplate
                            .opsForValue()
                            .set(cacheKey, userData, Duration.ofMinutes(10))
                            .thenReturn(userData);
                      }));
    } catch (Exception e) {
      LOG.error(RedisCacheConfig.REDIS_ERROR_LOG, e.getMessage());
      return accessSphereClient
          .getUserInfo(access_token, sessionId, true)
          .flatMap(AuthMapper::verifyAndMapAccessSphereResponse);
    }
  }
}
