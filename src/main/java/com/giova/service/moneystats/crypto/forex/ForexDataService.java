package com.giova.service.moneystats.crypto.forex;

import com.giova.service.moneystats.api.forex.anyApi.AnyAPIClient;
import com.giova.service.moneystats.authentication.dto.UserData;
import com.giova.service.moneystats.crypto.forex.database.ForexDataRepository;
import com.giova.service.moneystats.crypto.forex.dto.ForexData;
import com.giova.service.moneystats.crypto.forex.entity.ForexDataEntity;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.github.giovannilamarmora.utils.utilities.Utilities;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Logged
@Service
@RequiredArgsConstructor
public class ForexDataService {

  private final UserData user;
  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  @Autowired private AnyAPIClient anyAPIClient;
  @Autowired private ForexDataRepository forexDataRepository;

  /**
   * Get Forex Data By Currency into Database
   *
   * @param currency To be searched
   * @return Forex Data
   */
  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public ForexData getForexDataByCurrency(String currency) {
    LOG.info("Getting ForexData from Database for {}", currency);
    ForexDataEntity getMarketData = forexDataRepository.findByCurrency(currency);

    if (getMarketData == null) {
      LOG.error("No ForexData found into the Database");
      return new ForexData();
    }
    return ForexDataMapper.fromEntityToForexData(getMarketData);
  }

  /**
   * Det All Forex Data
   *
   * @return List of Forex Data
   */
  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public List<ForexData> getAllForexData() {
    LOG.info("Getting All MarketData from Database");
    List<ForexDataEntity> getForexData = forexDataRepository.findAll();

    if (getForexData.isEmpty()) {
      LOG.error("No ForexData List found into the Database");
      return new ArrayList<>();
    }
    return ForexDataMapper.fromEntitiesToForexDataList(getForexData);
  }

  /**
   * API To Save the forex data
   *
   * @param forexData To be saved
   * @return Forex Data Saved
   */
  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public ForexData saveForexData(ForexData forexData) {
    LOG.info(
        "Saving {} ForexData for currency {}",
        forexData.getQuotes().size(),
        forexData.getCurrency());
    ForexDataEntity forexDataEntity = ForexDataMapper.fromForexDataToEntity(forexData);

    ForexDataEntity saved =
        forexDataRepository.save(forexDataEntity, forexDataEntity.getCurrency());

    return ForexDataMapper.fromEntityToForexData(saved);
  }

  /** API To Delete the forex data */
  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public void deleteForexData() {
    LOG.info("Deleting ForexData from Database");
    List<String> currencies = forexDataRepository.selectDistinctCurrency();
    if (!currencies.isEmpty())
      currencies.forEach(
          currency -> forexDataRepository.deleteForexDataEntitiesByCurrency(currency));
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public Mono<ForexData> getFromExchangeRateForexData(String currency) {
    LOG.info("Getting MarketData for {}", currency);
    // ResponseEntity<ExchangeRates> exchangeRatesList = exchangeRatesClient.getForexData(currency);

    return anyAPIClient
        .getAnyApiForexData(currency)
        .flatMap(
            exchangeRatesList -> {
              if (!Utilities.isNullOrEmpty(exchangeRatesList.getBody())) {
                LOG.error("Error on fetching Exchange Rates");
                throw new ForexDataException(
                    "An error occurred during calling Exchange Rates, empty body");
              }
              // return mapper.fromExchangeRatesToForexData(exchangeRatesList.getBody());
              return Mono.just(ForexDataMapper.fromRatesToForexData(exchangeRatesList.getBody()));
            });
  }
}
