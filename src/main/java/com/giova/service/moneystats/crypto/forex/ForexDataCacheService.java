package com.giova.service.moneystats.crypto.forex;

import com.giova.service.moneystats.crypto.forex.entity.ForexDataEntity;
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
public class ForexDataCacheService {

  private static final String FOREX_DATA_CACHE = "ForexData-Cache";
  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  @Autowired private CacheManager cacheManager;
  @Autowired private IForexDataDAO forexDataDAO;

  @CachePut(value = FOREX_DATA_CACHE, key = "#currency", condition = "#currency!=null")
  @LogInterceptor(type = LogTimeTracker.ActionType.APP_CACHE)
  public List<ForexDataEntity> saveAll(List<ForexDataEntity> forexDataEntities, String currency) {
    LOG.info("[Caching] Salving ForexData into Database for currency {}", currency);
    return forexDataDAO.saveAll(forexDataEntities);
  }

  @CachePut(value = FOREX_DATA_CACHE, key = "#currency", condition = "#currency!=null")
  @LogInterceptor(type = LogTimeTracker.ActionType.APP_CACHE)
  public ForexDataEntity save(ForexDataEntity forexDataEntity, String currency) {
    LOG.info("[Caching] Salving ForexData into Database for currency {}", currency);
    return forexDataDAO.save(forexDataEntity);
  }

  @Cacheable(value = FOREX_DATA_CACHE, key = "#currency", condition = "#currency!=null")
  @LogInterceptor(type = LogTimeTracker.ActionType.APP_CACHE)
  public ForexDataEntity findByCurrency(String currency) {
    LOG.info("[Caching] ForexData into Database by currency {}", currency);
    return forexDataDAO.findByCurrency(currency);
  }

  @Transactional(Transactional.TxType.REQUIRES_NEW)
  @CacheEvict(value = FOREX_DATA_CACHE, key = "#currency", condition = "#currency!=null")
  @LogInterceptor(type = LogTimeTracker.ActionType.APP_CACHE)
  // @Transactional(noRollbackFor = Exception.class)
  public void deleteAllByCurrency(String currency) {
    LOG.info("[Caching] Deleting ForexData into Database by currency {}", currency);
    forexDataDAO.deleteForexDataEntitiesByCurrency(currency);
    // try {
    //  LOG.info("Resetting MarketData IDs to 1 with MySQL Dialect");
    //  marketDataDAO.resetIdToOneMySQL();
    // } catch (Exception e) {
    //  LOG.error("SQLGrammarException catch switch to H2 Dialect");
    //  marketDataDAO.resetIdToOneH2();
    // }
  }

  @Caching(evict = @CacheEvict(value = FOREX_DATA_CACHE))
  @LogInterceptor(type = LogTimeTracker.ActionType.APP_CACHE)
  public void deleteAllForexDataCache() {
    LOG.info("[Caching] Deleting cache for {}", FOREX_DATA_CACHE);
    Objects.requireNonNull(cacheManager.getCache(FOREX_DATA_CACHE)).clear();
  }
}
