package com.giova.service.moneystats.crypto.coinGecko;

import com.giova.service.moneystats.api.coingecko.CoinGeckoClient;
import com.giova.service.moneystats.api.coingecko.dto.CoinGeckoMarketData;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import com.giova.service.moneystats.crypto.coinGecko.dto.MarketData;
import com.giova.service.moneystats.crypto.coinGecko.entity.MarketDataEntity;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
        coinGeckoClient.getMarketData(currency);

    if (!getMarketData.hasBody()) {
      LOG.error("Error on fetching MarketData");
    }
    return mapper.fromCoinGeckoMarketDataListToCoinGeckoList(
        Objects.requireNonNull(getMarketData.getBody()));
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public List<MarketData> saveMarketData(List<MarketData> marketData, String currency) {
    LOG.info("Saving {} MarketData for currency {}", marketData.size(), currency);
    List<MarketDataEntity> marketDataEntities = mapper.fromMarketDataToEntity(marketData, currency);

    List<MarketDataEntity> saved =
        marketDataCacheService.saveAll(marketDataEntities, currency);

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
  public void deleteMarketData() {
    LOG.info("Deleting MarketData from Database");
    List<String> currencies = marketDataDAO.selectDistinctCurrency();
    if (!currencies.isEmpty())
      currencies.stream()
          .peek(currency -> marketDataCacheService.deleteAllByCurrency(currency))
          .collect(Collectors.toList());
  }
}
