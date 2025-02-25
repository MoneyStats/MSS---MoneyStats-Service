package com.giova.service.moneystats.authentication.service;

import com.giova.service.moneystats.api.accessSphere.AccessSphereClient;
import com.giova.service.moneystats.authentication.AuthMapper;
import com.giova.service.moneystats.authentication.dto.UserData;
import com.giova.service.moneystats.config.cache.CacheDataConfig;
import com.giova.service.moneystats.config.cache.RedisCacheConfig;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.utilities.ObjectToolkit;
import io.github.giovannilamarmora.utils.web.RequestManager;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthCacheService extends CacheDataConfig implements AuthRepository {

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
  @LogInterceptor(type = LogTimeTracker.ActionType.CACHE)
  public Mono<UserData> authorize(String access_token, String sessionId) {
    // Chiave di cache basata sui parametri
    String cacheKey = application_name + SPACE + "authorize_" + sessionId;

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
                      () ->
                          accessSphereClient
                              .getUserInfo(access_token, sessionId, true)
                              .flatMap(AuthMapper::verifyAndMapAccessSphereResponse))
                  .doOnSuccess(
                      userData -> {
                        LOG.info(
                            "[Caching] UserInfo into Access Sphere for session id {}", sessionId);
                        long expiration = AuthMapper.expireDate / 2;
                        userDataTemplate
                            .opsForValue()
                            // .set(cacheKey, userData, Duration.ofMinutes(30))
                            .set(cacheKey, userData, Duration.ofSeconds(expiration))
                            .subscribe(); // Assicura che l'operazione di salvataggio venga eseguita
                      }))
          .onErrorResume(
              throwable -> {
                LOG.error(RedisCacheConfig.REDIS_ERROR_LOG, throwable.getMessage());
                return accessSphereClient
                    .getUserInfo(access_token, sessionId, true)
                    .flatMap(AuthMapper::verifyAndMapAccessSphereResponse);
              });
    } catch (Exception e) {
      LOG.error(RedisCacheConfig.REDIS_ERROR_LOG, e.getMessage());
      return accessSphereClient
          .getUserInfo(access_token, sessionId, true)
          .flatMap(AuthMapper::verifyAndMapAccessSphereResponse);
    }
  }

  /** Method to delete the cache of the user. */
  public void evictAuthenticationCache(ServerWebExchange exchange) {
    if (redisCacheEnabled) {
      ServerHttpRequest request = exchange.getRequest();
      String sessionId = RequestManager.getCookieOrHeaderData("Session-ID", request);

      if (ObjectToolkit.isNullOrEmpty(sessionId)) return;

      try {
        String cacheKey = application_name + SPACE + "authorize_" + sessionId;

        if (!ObjectToolkit.isNullOrEmpty(userDataTemplate.opsForValue().get(cacheKey))) {
          userDataTemplate.delete(cacheKey).subscribe();
          LOG.info("Cache evicted for key: {}", cacheKey);
        }
      } catch (Exception e) {
        LOG.error("Error while evicting cache for session {}: {}", sessionId, e.getMessage());
      }
    }
  }
}
