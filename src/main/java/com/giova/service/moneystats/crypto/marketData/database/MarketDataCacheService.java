package com.giova.service.moneystats.crypto.marketData.database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.giova.service.moneystats.config.cache.CacheDataConfig;
import com.giova.service.moneystats.config.cache.RedisCacheConfig;
import com.giova.service.moneystats.crypto.marketData.entity.MarketDataEntity;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.utilities.Mapper;
import io.github.giovannilamarmora.utils.utilities.ObjectToolkit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class MarketDataCacheService extends CacheDataConfig implements MarketDataRepository {

  @Autowired private IMarketDataDAO iMarketDataDAO;

  @Autowired private RedisTemplate<String, String> marketDataEntityTemplate;

  /**
   * Get all MarketData with currency
   *
   * @param currency To get MarketData
   * @return List of MarketData
   */
  @Override
  @LogInterceptor(type = LogTimeTracker.ActionType.CACHE)
  public List<MarketDataEntity> findAllByCurrency(String currency) {
    String cacheKey = application_name + SPACE + CACHE_MARKET_DATA + currency;
    try {
      return Optional.ofNullable(marketDataEntityTemplate.opsForValue().get(cacheKey))
          .map(s -> Mapper.readObject(s, new TypeReference<List<MarketDataEntity>>() {}))
          .map(cache -> logCache(cache, cacheKey))
          .orElseGet(
              () -> {
                LOG.info("[Caching] MarketData into Database by currency {}", currency);
                List<MarketDataEntity> marketDataEntities =
                    iMarketDataDAO.findAllByCurrency(currency);

                if (!ObjectToolkit.isNullOrEmpty(marketDataEntities)) {
                  String json = Mapper.writeObjectToString(marketDataEntities);
                  marketDataEntityTemplate.opsForValue().set(cacheKey, json);
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
  @LogInterceptor(type = LogTimeTracker.ActionType.CACHE)
  public List<MarketDataEntity> findAll() {
    String cacheKey = application_name + SPACE + CACHE_MARKET_DATA_FULL;
    try {
      return Optional.ofNullable(marketDataEntityTemplate.opsForValue().get(cacheKey))
          .map(s -> Mapper.readObject(s, new TypeReference<List<MarketDataEntity>>() {}))
          .map(cache -> logCache(cache, cacheKey))
          .orElseGet(
              () -> {
                LOG.info("[Caching] MarketData into Database");
                List<MarketDataEntity> marketDataEntities = iMarketDataDAO.findAll();

                if (!ObjectToolkit.isNullOrEmpty(marketDataEntities)) {
                  String json = Mapper.writeObjectToString(marketDataEntities);
                  marketDataEntityTemplate.opsForValue().set(cacheKey, json);
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
  public List<MarketDataEntity> saveAll(
      List<MarketDataEntity> marketDataEntities, String currency) {
    evictMarketDataCache(currency);
    return iMarketDataDAO.saveAll(marketDataEntities);
  }

  /**
   * Delete MarketData for specific Currency
   *
   * @param currency To be deleted
   */
  @Override
  @LogInterceptor(type = LogTimeTracker.ActionType.CACHE)
  public void deleteMarketDataEntitiesByCurrency(String currency) {
    evictMarketDataCache(currency);
    iMarketDataDAO.deleteMarketDataEntitiesByCurrency(currency);
  }

  /**
   * Select the Crypto Fiat Currency to be used deleted
   *
   * @return List of currency
   */
  @Override
  @LogInterceptor(type = LogTimeTracker.ActionType.CACHE)
  public List<String> selectDistinctCurrency() {
    return iMarketDataDAO.selectDistinctCurrency();
  }

  /** Method to delete the cache of the market data of the user. */
  @LogInterceptor(type = LogTimeTracker.ActionType.CACHE)
  public void evictMarketDataCache(String currency) {
    try {
      // Cache key per wallets without assets and history
      String keyMarketData = application_name + SPACE + CACHE_MARKET_DATA + currency;
      // Cache key per full wallets list
      String keyFullMarketData = application_name + SPACE + CACHE_MARKET_DATA_FULL;

      if (!ObjectToolkit.isNullOrEmpty(marketDataEntityTemplate.opsForValue().get(keyMarketData))) {
        marketDataEntityTemplate.delete(keyMarketData);
        LOG.info("Cache evicted for key: {}", keyMarketData);
      }

      if (!ObjectToolkit.isNullOrEmpty(
          marketDataEntityTemplate.opsForValue().get(keyFullMarketData))) {
        marketDataEntityTemplate.delete(keyFullMarketData);
        LOG.info("Cache evicted for key: {}", keyFullMarketData);
      }
    } catch (Exception e) {
      LOG.error("Error while evicting cache for MarketData: {}", e.getMessage());
    }
  }

  /** Method to delete all the cache of the market data of the user. */
  @LogInterceptor(type = LogTimeTracker.ActionType.CACHE)
  public void clearAllMarketDataCache() {
    LOG.info("Starting to clear all market data cache.");
    if (marketDataEntityTemplate.getConnectionFactory() != null) {
      LOG.debug("Redis connection factory is available.");
      RedisConnection connection = marketDataEntityTemplate.getConnectionFactory().getConnection();

      if (!ObjectToolkit.isNullOrEmpty(connection)) {
        LOG.debug("Redis connection established successfully.");
        Set<String> keys = marketDataEntityTemplate.keys("*");

        if (keys != null && !keys.isEmpty()) {
          LOG.info("Found {} keys in the market data cache to delete.", keys.size());
          marketDataEntityTemplate.delete(keys);
          LOG.info("Successfully deleted all keys from the market data cache.");
        } else {
          LOG.info("No keys found in the market data cache to delete.");
        }
      } else {
        LOG.warn("Redis connection could not be established. Cache clearing aborted.");
      }
    } else {
      LOG.warn("Redis connection factory is unavailable. Cache clearing aborted.");
    }
    LOG.info("Finished clearing market data cache.");
  }
}
