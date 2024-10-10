package com.giova.service.moneystats.config.cache;

import io.github.giovannilamarmora.utils.logger.LoggerFilter;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

  private final Logger LOG = LoggerFilter.getLogger(this.getClass());

  @Value(value = "${spring.data.redis.enabled:false}")
  private Boolean redisCacheEnabled;

  @PostConstruct
  void init() {
    LOG.info("[CACHE] Redis cache status {}", redisCacheEnabled ? "ACTIVE" : "NOT ACTIVE");
  }

  // @Bean(name = "walletRepository")
  // @ConditionalOnProperty(prefix = "spring.data.redis", name = "enabled", havingValue = "true")
  // public WalletRepository getWalletRepository() {
  //  return new WalletCacheService();
  // }
  //
  // @Bean(name = "cmsService")
  // @ConditionalOnProperty(
  //    prefix = "spring.data.cache",
  //    name = "active",
  //    havingValue = "false",
  //    matchIfMissing = true)
  // public CmsService getData() {
  //  LOG.info("[CACHE] Cache Service Disabled");
  //  return new ExternalService();
  // }
}
