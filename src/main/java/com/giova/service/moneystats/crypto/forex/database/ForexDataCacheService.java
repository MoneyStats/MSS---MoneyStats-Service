package com.giova.service.moneystats.crypto.forex.database;

import com.giova.service.moneystats.config.cache.CacheUtils;
import com.giova.service.moneystats.config.cache.RedisCacheConfig;
import com.giova.service.moneystats.crypto.forex.entity.ForexDataEntity;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.utilities.Utilities;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ForexDataCacheService implements ForexDataRepository {

  private static final String CACHE_FOREX_DATA_BY_CURRENCY = "forex_data_";
  private static final String CACHE_FOREX_DATA = "forex_data_all";
  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  @Autowired private IForexDAO iForexDAO;

  @Autowired private RedisTemplate<String, ForexDataEntity> forexDataEntityTemplate;
  @Autowired private RedisTemplate<String, List<ForexDataEntity>> forexDataEntitiesTemplate;

  @LogInterceptor(type = LogTimeTracker.ActionType.CACHE)
  public ForexDataEntity findByCurrency(String currency) {
    String cacheKey = CACHE_FOREX_DATA_BY_CURRENCY + currency;
    try {
      return Optional.ofNullable(forexDataEntityTemplate.opsForValue().get(cacheKey))
          .orElseGet(
              () -> {
                LOG.info("[Caching] ForexData into Database by currency {}", currency);
                ForexDataEntity forexDataEntity = iForexDAO.findByCurrency(currency);

                if (!Utilities.isNullOrEmpty(forexDataEntity)) {
                  forexDataEntityTemplate.opsForValue().set(cacheKey, forexDataEntity);
                }
                return forexDataEntity;
              });
    } catch (Exception e) {
      LOG.error(RedisCacheConfig.REDIS_ERROR_LOG, e.getMessage());
      return iForexDAO.findByCurrency(currency);
    }
  }

  /**
   * Get All Forex Data
   *
   * @return List of ForexDataEntity
   */
  @Override
  public List<ForexDataEntity> findAll() {
    String cacheKey = CACHE_FOREX_DATA;
    try {
      return Optional.ofNullable(forexDataEntitiesTemplate.opsForValue().get(cacheKey))
          .orElseGet(
              () -> {
                LOG.info("[Caching] All ForexData into Database");
                List<ForexDataEntity> forexDataEntity = iForexDAO.findAll();

                if (!Utilities.isNullOrEmpty(forexDataEntity)) {
                  forexDataEntitiesTemplate.opsForValue().set(cacheKey, forexDataEntity);
                }
                return forexDataEntity;
              });
    } catch (Exception e) {
      LOG.error(RedisCacheConfig.REDIS_ERROR_LOG, e.getMessage());
      return iForexDAO.findAll();
    }
  }

  /**
   * Save the Forex Data for currency
   *
   * @param forexDataEntity To be saved
   * @param currency To be saved
   * @return ForexDataEntity
   */
  @LogInterceptor(type = LogTimeTracker.ActionType.CACHE)
  public ForexDataEntity save(ForexDataEntity forexDataEntity, String currency) {
    LOG.info("[Caching] Salving ForexData into Database for currency {}", currency);
    evictAllForexDataCache(currency);
    return iForexDAO.save(forexDataEntity);
  }

  /**
   * Select the Crypto Fiat Currency to be used deleted
   *
   * @return List of currency
   */
  @Override
  @LogInterceptor(type = LogTimeTracker.ActionType.CACHE)
  public List<String> selectDistinctCurrency() {
    return iForexDAO.selectDistinctCurrency();
  }

  /**
   * Delete Forex Data for currency
   *
   * @param currency To be deleted
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  @LogInterceptor(type = LogTimeTracker.ActionType.CACHE)
  public void deleteForexDataEntitiesByCurrency(String currency) {
    LOG.info("[Caching] Deleting ForexData into Database by currency {}", currency);
    evictAllForexDataCache(currency);
    iForexDAO.deleteForexDataEntitiesByCurrency(currency);
  }

  /** Method to delete the cache of the forex data of the user. */
  @LogInterceptor(type = LogTimeTracker.ActionType.CACHE)
  public void evictAllForexDataCache(String currency) {
    try {
      String keyForexDataByCurrency = CACHE_FOREX_DATA_BY_CURRENCY + currency;
      String keyFullForexData = CACHE_FOREX_DATA;

      if (!Utilities.isNullOrEmpty(
          forexDataEntityTemplate.opsForValue().get(keyForexDataByCurrency))) {
        forexDataEntityTemplate.delete(keyForexDataByCurrency);
        LOG.info("Cache evicted for key: {}", keyForexDataByCurrency);
      }

      if (!Utilities.isNullOrEmpty(forexDataEntitiesTemplate.opsForValue().get(keyFullForexData))) {
        forexDataEntitiesTemplate.delete(keyFullForexData);
        LOG.info("Cache evicted for key: {}", keyFullForexData);
      }
    } catch (Exception e) {
      LOG.error("Error while evicting cache for MarketData: {}", e.getMessage());
    }
  }

  /** Method to delete all the cache of the market data of the user. */
  @LogInterceptor(type = LogTimeTracker.ActionType.CACHE)
  public void clearAllForexDataCache() {
    LOG.info("Starting to clear all forex data cache.");
    CacheUtils.clearCache(forexDataEntityTemplate, "forex data");
    CacheUtils.clearCache(forexDataEntitiesTemplate, "forex entities data");
    LOG.info("Finished clearing forex data cache.");
  }
}
