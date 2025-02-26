package com.giova.service.moneystats.scheduler;

import com.giova.service.moneystats.crypto.marketData.MarketDataService;
import com.giova.service.moneystats.crypto.marketData.database.MarketDataRefreshCache;
import com.giova.service.moneystats.crypto.marketData.dto.MarketData;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.logger.MDCUtils;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class CronMarketData {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  @Value(value = "${env:Default}")
  private String env;

  @Value(value = "#{new Boolean(${rest.scheduled.marketData.active:false})}")
  private Boolean isSchedulerActive;

  @Value(value = "#{new Integer(${rest.scheduled.marketData.quantity:250})}")
  private Integer marketDataQuantity;

  @Autowired private MarketDataService marketDataService;
  @Autowired private MarketDataRefreshCache marketDataRefreshCache;

  @Scheduled(
      fixedDelayString = "${rest.scheduled.marketData.delay.end}",
      initialDelayString = "${rest.scheduled.marketData.delay.start}")
  @LogInterceptor(type = LogTimeTracker.ActionType.SCHEDULER)
  public void scheduleAllCryptoAsset() {
    MDCUtils.registerDefaultMDC(env).subscribe();
    Map<String, String> contextMap = MDC.getCopyOfContextMap();
    LOG.info("[Market Data] Scheduler Started at {}", LocalDateTime.now());

    if (!isSchedulerActive) {
      LOG.info("[Market Data] Scheduler Active status is NOT-ACTIVE, Stopping Scheduler");
      return;
    }

    // Ottengo la lista di currency per cui fare il salvataggio a DB
    List<String> fiatCurrencies = List.of("USD", "EUR", "GBP");

    // Mi salvo tutti i Market Data presenti a DB in caso di rollback
    List<MarketData> allMarketData = marketDataService.getAllMarketData();
    marketDataRefreshCache.setMarketData(allMarketData);

    // Cancello tutti i dati dalla tabella MarketData
    marketDataService.deleteMarketData();
    AtomicInteger counter = new AtomicInteger(0);

    // Uso un Flux per gestire in modo reattivo ogni operazione su ciascuna valuta
    Flux.fromIterable(fiatCurrencies)
        .concatMap(
            currency -> {
              LOG.info("[Market Data] Getting and Saving MarketData for currency {}", currency);

              // Chiamata reattiva al servizio per ottenere MarketData
              return marketDataService
                  .getCoinGeckoMarketData(currency, marketDataQuantity)
                  .flatMap(
                      getMarketData -> {
                        LOG.info(
                            "[Market Data] Found {} data of Market Data", getMarketData.size());

                        // Salvataggio dei dati al DB
                        return Mono.just(marketDataService.saveMarketData(getMarketData, currency));
                      })
                  // Aspetta 60 secondi tra una valuta e l'altra
                  .delaySubscription(Duration.ofSeconds(counter.getAndIncrement() > 0 ? 90 : 0));
            })
        .doOnError(
            e -> {
              LOG.error(
                  "[Market Data] Transaction is rolling back due to an error during MarketData processing");
              LOG.error("[Market Data] Exception: {}", e.getMessage());
              rollBackMarketData(fiatCurrencies, allMarketData);
            })
        .doOnTerminate(
            () -> {
              marketDataRefreshCache.removeMarketData();
              LOG.info("[Market Data] Scheduler Finished at {}", LocalDateTime.now());
            })
        .contextWrite(MDCUtils.contextViewMDC(env))
        .doOnEach(signal -> MDCUtils.setContextMap(contextMap))
        .subscribe(); // Necessario per attivare il flusso reattivo
  }

  private void rollBackMarketData(List<String> fiatCurrencies, List<MarketData> allMarketData) {
    marketDataService.deleteMarketData();
    fiatCurrencies.forEach(
        fc -> {
          LOG.info("[Market Data] Found {} data of Market Data to RollBack", allMarketData.size());
          if (!allMarketData.isEmpty())
            marketDataService.saveMarketData(
                allMarketData.stream()
                    .filter(marketData -> marketData.getCurrency().equalsIgnoreCase(fc))
                    .collect(Collectors.toList()),
                fc);
        });
  }
}
