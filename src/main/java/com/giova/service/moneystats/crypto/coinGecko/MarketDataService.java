package com.giova.service.moneystats.crypto.coinGecko;

import com.giova.service.moneystats.api.coingecko.CoinGeckoClient;
import com.giova.service.moneystats.api.coingecko.CoinGeckoException;
import com.giova.service.moneystats.api.coingecko.dto.CoinGeckoMarketData;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import com.giova.service.moneystats.crypto.coinGecko.dto.MarketData;
import com.giova.service.moneystats.crypto.coinGecko.entity.MarketDataEntity;
import com.giova.service.moneystats.exception.ExceptionMap;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Logged
@Service
@RequiredArgsConstructor
public class MarketDataService {

  private final UserEntity user;
  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  @Autowired private CoinGeckoClient coinGeckoClient;
  @Autowired private MarketDataMapper mapper;
  @Autowired private MarketDataCacheService marketDataCacheService;
  @Autowired private IMarketDataDAO marketDataDAO;

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public List<MarketData> getCoinGeckoMarketData(String currency) {
    LOG.info("Getting MarketData for {}", currency);
    ResponseEntity<List<CoinGeckoMarketData>> getMarketData =
        coinGeckoClient.getMarketData(currency, false);

    try {
      LOG.info("Thread is sleeping for {} millisecond", 3000);
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      LOG.info("An error occurred during sleeping thread, MarketDataService:42");
      throw new CoinGeckoException(
          ExceptionMap.ERR_THREAD_001,
          "An error occurred during sleeping thread, MarketDataService:42");
    }
    ResponseEntity<List<CoinGeckoMarketData>> getStableData =
        coinGeckoClient.getMarketData(currency, true);

    if (getMarketData.getBody() == null || getStableData.getBody() == null) {
      LOG.error("Error on fetching MarketData");
      throw new CoinGeckoException("An error occurred during calling CoinGecko, empty body");
    }

    List<MarketData> cryptocurency =
        mapper.fromCoinGeckoMarketDataListToCoinGeckoList(
            getMarketData.getBody(), "Cryptocurrency");
    List<MarketData> stablecoin =
        mapper.fromCoinGeckoMarketDataListToCoinGeckoList(getStableData.getBody(), "StableCoin");

    Predicate<MarketData> hasRankNull = md -> md.getRank() == null;
    stablecoin.removeIf(hasRankNull);

    cryptocurency.removeAll(stablecoin);
    cryptocurency.addAll(stablecoin);

    return cryptocurency;
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public List<MarketData> saveMarketData(List<MarketData> marketData, String currency) {
    LOG.info("Saving {} MarketData for currency {}", marketData.size(), currency);
    List<MarketDataEntity> marketDataEntities = mapper.fromMarketDataToEntity(marketData, currency);

    List<MarketDataEntity> saved = marketDataCacheService.saveAll(marketDataEntities, currency);

    return mapper.fromEntityToMarketData(saved);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public List<MarketData> getMarketData(String currency) {
    LOG.info("Getting MarketData from Database for {}", currency);
    List<MarketDataEntity> getMarketData = marketDataCacheService.findAllByCurrency(currency);

    if (getMarketData.isEmpty()) {
      LOG.error("No MarketData found");
      return new ArrayList<>();
    }
    return mapper.fromEntityToMarketData(getMarketData);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public List<MarketData> getAllMarketData() {
    LOG.info("Getting All MarketData from Database");
    List<MarketDataEntity> getMarketData = marketDataDAO.findAll();

    if (getMarketData.isEmpty()) {
      LOG.error("No MarketData found");
      return new ArrayList<>();
    }
    return mapper.fromEntityToMarketData(getMarketData);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public void deleteMarketData() {
    LOG.info("Deleting MarketData from Database");
    List<String> currencies = marketDataDAO.selectDistinctCurrency();
    if (!currencies.isEmpty())
      currencies.stream()
          .peek(currency -> marketDataCacheService.deleteAllByCurrency(currency))
          .collect(Collectors.toList());
  }
}
