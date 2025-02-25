package com.giova.service.moneystats.crypto.marketData;

import com.giova.service.moneystats.api.coingecko.CoinGeckoClient;
import com.giova.service.moneystats.api.coingecko.CoinGeckoException;
import com.giova.service.moneystats.api.coingecko.dto.CoinGeckoMarketData;
import com.giova.service.moneystats.authentication.dto.UserData;
import com.giova.service.moneystats.crypto.marketData.database.MarketDataCacheService;
import com.giova.service.moneystats.crypto.marketData.database.MarketDataRefreshCache;
import com.giova.service.moneystats.crypto.marketData.database.MarketDataRepository;
import com.giova.service.moneystats.crypto.marketData.dto.MarketData;
import com.giova.service.moneystats.crypto.marketData.entity.MarketDataEntity;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.github.giovannilamarmora.utils.math.MathService;
import jakarta.transaction.Transactional;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Logged
@Service
@RequiredArgsConstructor
public class MarketDataService {

  private final UserData user;
  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  @Autowired private CoinGeckoClient coinGeckoClient;
  @Autowired private MarketDataCacheService marketDataCacheService;
  @Autowired private MarketDataRepository marketDataRepository;
  @Autowired private MarketDataRefreshCache marketDataRefreshCache;

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public List<MarketData> getMarketData(String currency) {
    if (marketDataRefreshCache.isUpdating())
      return marketDataRefreshCache.getMarketDataByCurrency(currency);

    LOG.info("Getting MarketData for currency {}", currency);
    List<MarketDataEntity> getMarketData = marketDataRepository.findAllByCurrency(currency);

    if (getMarketData.isEmpty()) {
      LOG.error("No MarketData found into the Database");
      return Collections.emptyList();
    }
    return MarketDataMapper.fromEntityToMarketData(getMarketData).stream()
        .sorted(Comparator.comparing(MarketData::getRank))
        .collect(Collectors.toList());
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public List<MarketData> getAllMarketData() {
    if (marketDataRefreshCache.isUpdating()) return marketDataRefreshCache.getMarketData();

    LOG.info("Getting All MarketData from Database");
    List<MarketDataEntity> getMarketData = marketDataRepository.findAll();

    if (getMarketData.isEmpty()) {
      LOG.error("No MarketData List found into the Database");
      return Collections.emptyList();
    }
    return MarketDataMapper.fromEntityToMarketData(getMarketData);
  }

  @Transactional
  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public void deleteMarketData() {
    LOG.info("Deleting All MarketData from Database");
    List<String> currencies = marketDataRepository.selectDistinctCurrency();
    if (!currencies.isEmpty()) {
      currencies.forEach(
          currency -> marketDataRepository.deleteMarketDataEntitiesByCurrency(currency));
      LOG.info("Reset Auto Increment");
      marketDataRepository.resetAutoIncrement();
    }
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public List<MarketData> saveMarketData(List<MarketData> marketData, String currency) {
    LOG.info("Saving {} MarketData for currency {}", marketData.size(), currency);
    List<MarketDataEntity> marketDataEntities =
        MarketDataMapper.fromMarketDataToEntity(marketData, currency);

    List<MarketDataEntity> saved = marketDataRepository.saveAll(marketDataEntities, currency);

    return MarketDataMapper.fromEntityToMarketData(saved);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public Mono<List<MarketData>> getCoinGeckoMarketData(String currency, Integer quantity) {
    List<Integer> pages = getPage(quantity);
    LOG.info(
        "Getting {} MarketData for {}, number of page are {}", quantity, currency, pages.getLast());

    Flux<CoinGeckoMarketData> marketDataFlux =
        Flux.fromIterable(pages)
            .flatMap(
                page ->
                    coinGeckoClient
                        .getMarketData(currency, page, false)
                        .flatMapMany(
                            response -> {
                              if (response.getBody() == null) {
                                LOG.error("Error on fetching MarketData for page {}", page);
                                return Mono.error(
                                    new CoinGeckoException(
                                        "An error occurred during calling CoinGecko, empty body"));
                              }
                              return Flux.fromIterable(response.getBody());
                            }));

    Mono<List<CoinGeckoMarketData>> stablecoinMono =
        coinGeckoClient
            .getMarketData(currency, 1, true)
            .flatMap(
                response -> {
                  if (response.getBody() == null) {
                    LOG.error("Error on fetching StableCoin MarketData");
                    return Mono.error(
                        new CoinGeckoException(
                            "An error occurred during calling CoinGecko, empty body"));
                  }
                  return Mono.just(response.getBody());
                });

    return marketDataFlux
        .collectList()
        .zipWith(stablecoinMono)
        .flatMap(
            tuple -> {
              List<CoinGeckoMarketData> marketData = tuple.getT1();
              List<CoinGeckoMarketData> stableCoinData = tuple.getT2();

              marketData.removeAll(stableCoinData);

              List<MarketData> cryptocurrency =
                  MarketDataMapper.fromCoinGeckoMarketDataListToCoinGeckoList(
                      marketData, "Cryptocurrency");
              List<MarketData> stableCoin =
                  MarketDataMapper.fromCoinGeckoMarketDataListToCoinGeckoList(
                      stableCoinData, "Stablecoin");

              Predicate<MarketData> hasRankOrCurrentPriceNull =
                  md -> md.getRank() == null || md.getCurrent_price() == null;
              cryptocurrency.removeIf(hasRankOrCurrentPriceNull);
              stableCoin.removeIf(hasRankOrCurrentPriceNull);

              cryptocurrency.removeAll(stableCoin);
              cryptocurrency.addAll(stableCoin);

              cryptocurrency.sort(Comparator.comparing(MarketData::getRank));

              return Mono.just(cryptocurrency);
            });
  }

  private List<Integer> getPage(Integer quantity) {
    int page = (int) MathService.round(((double) (quantity - 100) / 250), 0);
    LOG.info("For {} MarketData, {} Pages have to be checked", quantity, page);
    return IntStream.rangeClosed(1, page).boxed().collect(Collectors.toList());
  }
}
