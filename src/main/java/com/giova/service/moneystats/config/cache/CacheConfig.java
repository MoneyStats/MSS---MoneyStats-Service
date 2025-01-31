package com.giova.service.moneystats.config.cache;

import com.giova.service.moneystats.app.wallet.database.WalletCacheService;
import com.giova.service.moneystats.app.wallet.database.WalletDAOAdapter;
import com.giova.service.moneystats.app.wallet.database.WalletRepository;
import com.giova.service.moneystats.authentication.service.AuthCacheService;
import com.giova.service.moneystats.authentication.service.AuthRepository;
import com.giova.service.moneystats.authentication.service.AuthService;
import com.giova.service.moneystats.crypto.asset.database.AssetCacheService;
import com.giova.service.moneystats.crypto.asset.database.AssetDAOAdapter;
import com.giova.service.moneystats.crypto.asset.database.AssetRepository;
import com.giova.service.moneystats.crypto.forex.database.ForexDataCacheService;
import com.giova.service.moneystats.crypto.forex.database.ForexDataDAOAdapter;
import com.giova.service.moneystats.crypto.forex.database.ForexDataRepository;
import com.giova.service.moneystats.crypto.marketData.database.MarketDataCacheService;
import com.giova.service.moneystats.crypto.marketData.database.MarketDataDAOAdapter;
import com.giova.service.moneystats.crypto.marketData.database.MarketDataRepository;
import io.github.giovannilamarmora.utils.logger.LoggerFilter;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

  @Bean(name = "marketDataRepository")
  public MarketDataRepository getMarketDataRepository() {
    if (redisCacheEnabled) return new MarketDataCacheService();
    return new MarketDataDAOAdapter();
  }

  @Bean(name = "forexDataRepository")
  public ForexDataRepository getForexDataRepository() {
    if (redisCacheEnabled) return new ForexDataCacheService();
    return new ForexDataDAOAdapter();
  }

  @Bean(name = "authRepository")
  public AuthRepository getAuthRepository() {
    if (redisCacheEnabled) return new AuthCacheService();
    return new AuthService();
  }
}
