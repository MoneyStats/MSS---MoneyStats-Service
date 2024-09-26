package com.giova.service.moneystats.crypto.coinGecko;

import com.giova.service.moneystats.api.coingecko.CoinGeckoClient;
import com.giova.service.moneystats.api.coingecko.CoinGeckoException;
import com.giova.service.moneystats.api.coingecko.dto.CoinGeckoMarketData;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import com.giova.service.moneystats.crypto.coinGecko.dto.MarketData;
import com.giova.service.moneystats.crypto.coinGecko.entity.MarketDataEntity;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.github.giovannilamarmora.utils.math.MathService;
import java.util.ArrayList;
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

  private final UserEntity user;
  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  @Autowired private CoinGeckoClient coinGeckoClient;
  @Autowired private MarketDataMapper mapper;
  @Autowired private MarketDataCacheService marketDataCacheService;
  @Autowired private IMarketDataDAO marketDataDAO;

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public Mono<List<MarketData>> getCoinGeckoMarketData(String currency, Integer quantity) {
    LOG.info("Getting {} MarketData for {}", quantity, currency);

    // Ottieni tutte le pagine in modo reattivo usando un Flux
    Flux<CoinGeckoMarketData> marketDataFlux =
        Flux.fromIterable(getPage(quantity))
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

    // Ottieni i stablecoin come Mono
    Mono<List<CoinGeckoMarketData>> stablecoinMono =
        coinGeckoClient
            .getMarketData(currency, 1, true)
            .flatMap(
                response -> {
                  if (response.getBody() == null) {
                    LOG.error("Error on fetching Stablecoin MarketData");
                    return Mono.error(
                        new CoinGeckoException(
                            "An error occurred during calling CoinGecko, empty body"));
                  }
                  return Mono.just(response.getBody());
                });

    // Combina market data e stablecoin
    return marketDataFlux
        .collectList()
        .zipWith(stablecoinMono)
        .map(
            tuple -> {
              List<CoinGeckoMarketData> marketData = tuple.getT1();
              List<CoinGeckoMarketData> stablecoinData = tuple.getT2();

              // Rimuovi i stablecoin dai dati delle criptovalute
              marketData.removeAll(stablecoinData);

              // Mappa i dati
              List<MarketData> cryptocurrency =
                  mapper.fromCoinGeckoMarketDataListToCoinGeckoList(marketData, "Cryptocurrency");
              List<MarketData> stablecoin =
                  mapper.fromCoinGeckoMarketDataListToCoinGeckoList(stablecoinData, "Stablecoin");

              // Rimuovi i dati con Rank o CurrentPrice null
              Predicate<MarketData> hasRankOrCurrentPriceNull =
                  md -> md.getRank() == null || md.getCurrent_price() == null;
              cryptocurrency.removeIf(hasRankOrCurrentPriceNull);
              stablecoin.removeIf(hasRankOrCurrentPriceNull);

              // Unisci i dati criptovaluta e stablecoin
              cryptocurrency.removeAll(stablecoin);
              cryptocurrency.addAll(stablecoin);

              // Ordina per Rank
              cryptocurrency.sort(Comparator.comparing(MarketData::getRank));

              return cryptocurrency;
            });
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public List<MarketData> saveMarketData(List<MarketData> marketData, String currency) {
    LOG.info("Saving {} MarketData for currency {}", marketData.size(), currency);
    List<MarketDataEntity> marketDataEntities = mapper.fromMarketDataToEntity(marketData, currency);

    List<MarketDataEntity> saved = marketDataCacheService.saveAll(marketDataEntities, currency);

    return mapper.fromEntityToMarketData(saved);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public List<MarketData> getMarketData(String currency) {
    LOG.info("Getting MarketData from Database for {}", currency);
    List<MarketDataEntity> getMarketData = marketDataCacheService.findAllByCurrency(currency);

    if (getMarketData.isEmpty()) {
      LOG.error("No MarketData found");
      return new ArrayList<>();
    }
    return mapper.fromEntityToMarketData(getMarketData).stream()
        .sorted(Comparator.comparing(MarketData::getRank))
        .collect(Collectors.toList());
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public List<MarketData> getAllMarketData() {
    LOG.info("Getting All MarketData from Database");
    List<MarketDataEntity> getMarketData = marketDataDAO.findAll();

    if (getMarketData.isEmpty()) {
      LOG.error("No MarketData found");
      return new ArrayList<>();
    }
    return mapper.fromEntityToMarketData(getMarketData);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public void deleteMarketData() {
    LOG.info("Deleting MarketData from Database");
    List<String> currencies = marketDataDAO.selectDistinctCurrency();
    if (!currencies.isEmpty())
      currencies.stream()
          .peek(currency -> marketDataCacheService.deleteAllByCurrency(currency))
          .collect(Collectors.toList());
  }

  private List<Integer> getPage(Integer quantity) {
    int page = (int) MathService.round(((double) (quantity - 100) / 250), 0);
    LOG.info("For {} MarketData, {} Pages have to be checked", quantity, page);
    return IntStream.rangeClosed(1, page).boxed().collect(Collectors.toList());
  }
}
