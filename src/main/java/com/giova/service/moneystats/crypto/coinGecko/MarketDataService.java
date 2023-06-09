package com.giova.service.moneystats.crypto.coinGecko;

import com.giova.service.moneystats.api.coingecko.CoinGeckoClient;
import com.giova.service.moneystats.api.coingecko.dto.CoinGeckoMarketData;
import com.giova.service.moneystats.crypto.coinGecko.dto.MarketData;
import com.giova.service.moneystats.crypto.coinGecko.entity.MarketDataEntity;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Logged
@Service
public class MarketDataService {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  @Autowired private CoinGeckoClient coinGeckoClient;
  @Autowired private MarketDataMapper mapper;
  @Autowired private IMarketDataDAO marketDataDAO;

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public List<MarketData> getMarketData(String currency) {
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
  public List<MarketData> saveMarketData(List<MarketData> marketData, String currency){
    LOG.info("Saving MarketData for currency {}", currency);
    List<MarketDataEntity> marketDataEntities = mapper.fromMarketDataToEntity(marketData, currency);

    List<MarketDataEntity> saved = marketDataDAO.saveAll(marketDataEntities);

    return mapper.fromEntityToMarketData(saved);
  }
}
