package com.giova.service.moneystats.crypto.coinGecko;

import com.giova.service.moneystats.crypto.coinGecko.entity.MarketDataEntity;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import java.util.List;
import java.util.Objects;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

@Service
public class MarketDataCacheService {

  private static final String MARKET_DATA_CACHE = "MarketData-Cache";
  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  @Autowired private CacheManager cacheManager;
  @Autowired private IMarketDataDAO marketDataDAO;

  @CachePut(value = MARKET_DATA_CACHE, key = "#currency", condition = "#currency!=null")
  @LogInterceptor(type = LogTimeTracker.ActionType.APP_CACHE)
  public List<MarketDataEntity> saveAll(
      List<MarketDataEntity> marketDataEntities, String currency) {
    LOG.info("[Caching] Salving MarketData into Database for currency {}", currency);
    return marketDataDAO.saveAll(marketDataEntities);
  }

  @Cacheable(value = MARKET_DATA_CACHE, key = "#currency", condition = "#currency!=null")
  @LogInterceptor(type = LogTimeTracker.ActionType.APP_CACHE)
  public List<MarketDataEntity> findAllByCurrency(String currency) {
    LOG.info("[Caching] MarketData into Database by currency {}", currency);
    return marketDataDAO.findAllByCurrency(currency);
  }

  @Transactional
  @CacheEvict(value = MARKET_DATA_CACHE, key = "#currency", condition = "#currency!=null")
  @LogInterceptor(type = LogTimeTracker.ActionType.APP_CACHE)
  //@Transactional(noRollbackFor = Exception.class)
  public void deleteAllByCurrency(String currency) {
    LOG.info("[Caching] Deleting MarketData into Database by currency {}", currency);
    marketDataDAO.deleteMarketDataEntitiesByCurrency(currency);
    //try {
    //  LOG.info("Resetting MarketData IDs to 1 with MySQL Dialect");
    //  marketDataDAO.resetIdToOneMySQL();
    //} catch (Exception e) {
    //  LOG.error("SQLGrammarException catch switch to H2 Dialect");
    //  marketDataDAO.resetIdToOneH2();
    //}
  }

  @Caching(evict = @CacheEvict(value = MARKET_DATA_CACHE))
  @LogInterceptor(type = LogTimeTracker.ActionType.APP_CACHE)
  public void deleteAllMarketDataCache() {
    LOG.info("[Caching] Deleting cache for {}", MARKET_DATA_CACHE);
    Objects.requireNonNull(cacheManager.getCache(MARKET_DATA_CACHE)).clear();
  }
}
