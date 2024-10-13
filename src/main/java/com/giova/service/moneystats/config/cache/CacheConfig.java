package com.giova.service.moneystats.config.cache;

import com.giova.service.moneystats.app.wallet.database.WalletCacheService;
import com.giova.service.moneystats.app.wallet.database.WalletDAOAdapter;
import com.giova.service.moneystats.app.wallet.database.WalletRepository;
import com.giova.service.moneystats.authentication.AuthCacheService;
import com.giova.service.moneystats.crypto.asset.database.AssetCacheService;
import com.giova.service.moneystats.crypto.asset.database.AssetDAOAdapter;
import com.giova.service.moneystats.crypto.asset.database.AssetRepository;
import com.giova.service.moneystats.crypto.coinGecko.MarketDataCacheService;
import com.giova.service.moneystats.crypto.forex.ForexDataCacheService;
import io.github.giovannilamarmora.utils.logger.LoggerFilter;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
@EnableCaching
public class CacheConfig {

  private final Logger LOG = LoggerFilter.getLogger(this.getClass());

  @Value(value = "${spring.data.redis.enabled:false}")
  private Boolean redisCacheEnabled;

  @Autowired private RedisConnectionFactory redisConnectionFactory;

  @PostConstruct
  void init() {
    LOG.info("[CACHE] Redis cache status {}", redisCacheEnabled ? "ACTIVE" : "NOT ACTIVE");
  }

  @Bean(name = "walletRepository")
  public WalletRepository getWalletRepository() {
    if (redisCacheEnabled) return new WalletCacheService();
    return new WalletDAOAdapter();
  }

  @Bean(name = "assetRepository")
  public AssetRepository getAssetRepository() {
    if (redisCacheEnabled) return new AssetCacheService();
    return new AssetDAOAdapter();
  }

  @Bean
  public CacheManager cacheManager() {
    if (redisCacheEnabled) {
      return RedisCacheManager.builder(redisConnectionFactory).build();
    } else {
      return new ConcurrentMapCacheManager(
          AuthCacheService.USER_CACHE,
          WalletCacheService.CRYPTO_WALLET_CACHE,
          WalletCacheService.WALLET_CACHE,
          WalletCacheService.DETAILS_WALLET,
          MarketDataCacheService.MARKET_DATA_CACHE,
          ForexDataCacheService.FOREX_DATA_CACHE); // Usa cache in-memory
    }
  }
}
