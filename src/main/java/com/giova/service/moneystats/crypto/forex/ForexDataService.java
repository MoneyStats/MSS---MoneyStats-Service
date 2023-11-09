package com.giova.service.moneystats.crypto.forex;

import com.giova.service.moneystats.api.coingecko.CoinGeckoException;
import com.giova.service.moneystats.api.forex.ExchangeRatesClient;
import com.giova.service.moneystats.api.forex.dto.ExchangeRates;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import com.giova.service.moneystats.crypto.forex.dto.ForexData;
import com.giova.service.moneystats.crypto.forex.entity.ForexDataEntity;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import java.util.ArrayList;
import java.util.List;
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
public class ForexDataService {

  private final UserEntity user;
  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  @Autowired private ExchangeRatesClient exchangeRatesClient;
  @Autowired private ForexDataMapper mapper;
  @Autowired private ForexDataCacheService forexDataCacheService;
  @Autowired private IForexDataDAO iForexDataDAO;

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public ForexData getFromExchangeRateForexData(String currency) {
    LOG.info("Getting MarketData for {}", currency);
    ResponseEntity<ExchangeRates> exchangeRatesList = exchangeRatesClient.getForexData(currency);

    if (exchangeRatesList.getBody() == null) {
      LOG.error("Error on fetching Exchange Rates");
      throw new CoinGeckoException("An error occurred during calling Exchange Rates, empty body");
    }
    return mapper.fromExchangeRatesToForexData(exchangeRatesList.getBody());
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public ForexData saveForexData(ForexData forexData) {
    LOG.info(
        "Saving {} ForexData for currency {}",
        forexData.getQuotes().size(),
        forexData.getCurrency());
    ForexDataEntity forexDataEntity = mapper.fromForexDataToEntity(forexData);

    ForexDataEntity saved =
        forexDataCacheService.save(forexDataEntity, forexDataEntity.getCurrency());

    return mapper.fromEntityToForexData(saved);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public ForexData getForexData(String currency) {
    LOG.info("Getting ForexData from Database for {}", currency);
    ForexDataEntity getMarketData = forexDataCacheService.findByCurrency(currency);

    if (getMarketData == null) {
      LOG.error("No ForexData found");
      return new ForexData();
    }
    return mapper.fromEntityToForexData(getMarketData);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public List<ForexData> getAllForexData() {
    LOG.info("Getting All MarketData from Database");
    List<ForexDataEntity> getForexData = iForexDataDAO.findAll();

    if (getForexData.isEmpty()) {
      LOG.error("No MarketData found");
      return new ArrayList<>();
    }
    return mapper.fromEntitiesToForexDataList(getForexData);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public void deleteForexData() {
    LOG.info("Deleting ForexData from Database");
    List<String> currencies = iForexDataDAO.selectDistinctCurrency();
    if (!currencies.isEmpty())
      currencies.stream()
          .peek(currency -> forexDataCacheService.deleteAllByCurrency(currency))
          .collect(Collectors.toList());
  }
}
