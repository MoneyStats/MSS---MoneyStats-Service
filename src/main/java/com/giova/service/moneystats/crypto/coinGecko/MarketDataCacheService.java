package com.giova.service.moneystats.crypto.coinGecko;

import com.giova.service.moneystats.crypto.coinGecko.entity.MarketDataEntity;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarketDataCacheService {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  @Autowired private IMarketDataDAO marketDataDAO;

  @CachePut(value = "MarketData", key = "#currency", condition = "#currency!=null")
  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public List<MarketDataEntity> saveAll(
      List<MarketDataEntity> marketDataEntities, String currency) {
    LOG.info("[Caching] Salving MarketData into Database for currency {}", currency);
    return marketDataDAO.saveAll(marketDataEntities);
  }

  @Cacheable(value = "MarketData", key = "#currency")
  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public List<MarketDataEntity> findAllByCurrency(String currency) {
    LOG.info("[Caching] MarketData into Database by currency {}", currency);
    return marketDataDAO.findAllByCurrency(currency);
  }

  @CacheEvict(value = "MarketData", key = "#currency")
  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  @Transactional(noRollbackFor = Exception.class)
  public void deleteAllByCurrency(String currency) {
    LOG.info("[Caching] Deleting MarketData into Database by currency {}", currency);
    marketDataDAO.deleteMarketDataEntitiesByCurrency(currency);
    try {
      LOG.info("Resetting MarketData IDs to 1 with MySQL Dialect");
      marketDataDAO.resetIdToOneMySQL();
    } catch (Exception e) {
      LOG.error("SQLGrammarException catch switch to H2 Dialect");
      marketDataDAO.resetIdToOneH2();
    }
  }
}
