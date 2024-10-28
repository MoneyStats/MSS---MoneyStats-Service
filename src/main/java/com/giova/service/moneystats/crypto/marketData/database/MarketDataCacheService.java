package com.giova.service.moneystats.crypto.marketData.database;

import com.giova.service.moneystats.config.cache.RedisCacheConfig;
import com.giova.service.moneystats.crypto.marketData.entity.MarketDataEntity;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.utilities.Utilities;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class MarketDataCacheService implements MarketDataRepository {

  private static final String CACHE_MARKET_DATA = "market_data_";
  private static final String CACHE_MARKET_DATA_FULL = "market_data_full";
  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  @Autowired private IMarketDataDAO iMarketDataDAO;

  @Autowired private RedisTemplate<String, List<MarketDataEntity>> marketDataEntityTemplate;

  /**
   * Get all MarketData with currency
   *
   * @param currency To get MarketData
   * @return List of MarketData
   */
  @Override
  @LogInterceptor(type = LogTimeTracker.ActionType.CACHE)
  public List<MarketDataEntity> findAllByCurrency(String currency) {
    String cacheKey = CACHE_MARKET_DATA + currency;
    try {
      return Optional.ofNullable(marketDataEntityTemplate.opsForValue().get(cacheKey))
          .orElseGet(
              () -> {
                LOG.info("[Caching] MarketData into Database by currency {}", currency);
                List<MarketDataEntity> marketDataEntities =
                    iMarketDataDAO.findAllByCurrency(currency);

                if (!Utilities.isNullOrEmpty(marketDataEntities)) {
                  marketDataEntityTemplate.opsForValue().set(cacheKey, marketDataEntities);
                }
                return marketDataEntities;
              });
    } catch (Exception e) {
      LOG.error(RedisCacheConfig.REDIS_ERROR_LOG, e.getMessage());
      return iMarketDataDAO.findAllByCurrency(currency);
    }
  }

  /**
   * Get all MarketData
   *
   * @return List of MarketData
   */
  @Override
  public List<MarketDataEntity> findAll() {
    String cacheKey = CACHE_MARKET_DATA_FULL;
    try {
      return Optional.ofNullable(marketDataEntityTemplate.opsForValue().get(cacheKey))
          .orElseGet(
              () -> {
                LOG.info("[Caching] MarketData into Database");
                List<MarketDataEntity> marketDataEntities = iMarketDataDAO.findAll();

                if (!Utilities.isNullOrEmpty(marketDataEntities)) {
                  marketDataEntityTemplate.opsForValue().set(cacheKey, marketDataEntities);
                }
                return marketDataEntities;
              });
    } catch (Exception e) {
      LOG.error(RedisCacheConfig.REDIS_ERROR_LOG, e.getMessage());
      return iMarketDataDAO.findAll();
    }
  }

  @Override
  @LogInterceptor(type = LogTimeTracker.ActionType.CACHE)
  public List<MarketDataEntity> saveAll(List<MarketDataEntity> marketDataEntities) {
    evictAllMarketDataCache();
    return iMarketDataDAO.saveAll(marketDataEntities);
  }

  /**
   * Delete MarketData for specific Currency
   *
   * @param currency To be deleted
   */
  @Override
  public void deleteMarketDataEntitiesByCurrency(String currency) {
    evictAllMarketDataCache();
    iMarketDataDAO.deleteMarketDataEntitiesByCurrency(currency);
  }

  /**
   * Select the Crypto Fiat Currency to be used deleted
   *
   * @return List of currency
   */
  @Override
  public List<String> selectDistinctCurrency() {
    return iMarketDataDAO.selectDistinctCurrency();
  }

  /** Method to delete the cache of the market data of the user. */
  public void evictAllMarketDataCache() {
    try {
      // Cache key per wallets without assets and history
      String keyMarketData = CACHE_MARKET_DATA;
      // Cache key per full wallets list
      String keyFullMarketData = CACHE_MARKET_DATA_FULL;

      if (!Utilities.isNullOrEmpty(marketDataEntityTemplate.opsForValue().get(keyMarketData))) {
        marketDataEntityTemplate.delete(keyMarketData);
        LOG.info("Cache evicted for key: {}", keyMarketData);
      }

      if (!Utilities.isNullOrEmpty(marketDataEntityTemplate.opsForValue().get(keyFullMarketData))) {
        marketDataEntityTemplate.delete(keyFullMarketData);
        LOG.info("Cache evicted for key: {}", keyFullMarketData);
      }
    } catch (Exception e) {
      LOG.error("Error while evicting cache for MarketData: {}", e.getMessage());
    }
  }
}
