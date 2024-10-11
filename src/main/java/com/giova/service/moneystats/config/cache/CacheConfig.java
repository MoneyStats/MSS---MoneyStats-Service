package com.giova.service.moneystats.config.cache;

import com.giova.service.moneystats.app.wallet.database.WalletCacheService;
import com.giova.service.moneystats.app.wallet.database.WalletDAOAdapter;
import com.giova.service.moneystats.app.wallet.database.WalletRepository;
import io.github.giovannilamarmora.utils.logger.LoggerFilter;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
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

  @Bean(name = "walletRepository")
  public WalletRepository getWalletRepository() {
    if (redisCacheEnabled) return new WalletCacheService();
    return new WalletDAOAdapter();
  }
}
