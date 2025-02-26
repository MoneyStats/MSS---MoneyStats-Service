package com.giova.service.moneystats.scheduler;

import com.giova.service.moneystats.crypto.forex.ForexDataService;
import com.giova.service.moneystats.crypto.forex.dto.ForexData;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.logger.MDCUtils;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class CronForexData {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  @Value(value = "${env:Default}")
  private String env;

  @Value(value = "#{new Boolean(${rest.scheduled.forex.active:false})}")
  private Boolean isSchedulerActive;

  @Autowired private ForexDataService forexDataService;

  @Scheduled(
      fixedDelayString = "${rest.scheduled.forex.delay.end}",
      initialDelayString = "${rest.scheduled.forex.delay.start}")
  @LogInterceptor(type = LogTimeTracker.ActionType.SCHEDULER)
  public void scheduleAllCryptoAsset() {
    MDCUtils.registerDefaultMDC(env).subscribe();
    Map<String, String> contextMap = MDC.getCopyOfContextMap();
    LOG.info("[Forex] Scheduler Started at {}", LocalDateTime.now());

    if (!isSchedulerActive) {
      LOG.info("[Forex] Scheduler Active status is NOT-ACTIVE, Stopping Scheduler");
      return;
    }

    // Ottenengo la lista di currency per cui fare il salvataggio a DB
    // List<String> fiatCurrencies = authService.getCryptoFiatUsersCurrency();
    List<String> fiatCurrencies = List.of("USD", "EUR", "GBP");

    if (fiatCurrencies.isEmpty()) {
      LOG.info("[Forex] No Currency found on Database, Stopping Scheduler");
      return;
    }

    // Mi salvo tutti i Market Data presenti a DB in caso di rollback
    List<ForexData> allForexData = forexDataService.getAllForexData();
    AtomicInteger counter = new AtomicInteger(0);

    // Cancello tutti i dati dalla tabella MarketData
    forexDataService.deleteForexData();
    Flux.fromIterable(fiatCurrencies)
        .concatMap(
            currency ->
                forexDataService
                    .getFromExchangeRateForexData(currency)
                    .doOnNext(
                        forexData -> {
                          LOG.info(
                              "[Forex] Found {} rates of Forex Data for {}",
                              forexData.getQuotes().size(),
                              currency);
                          forexDataService.saveForexData(forexData);
                        })
                    .delaySubscription(Duration.ofSeconds(counter.getAndIncrement() > 0 ? 5 : 0)))
        .doOnError(
            throwable -> {
              LOG.error(
                  "[Forex] Transaction is rolling back due to an error: {}",
                  throwable.getMessage());
              LOG.error("[Forex] Cleaning Forex Database");
              rollBackForexData(fiatCurrencies, allForexData);
            })
        .doOnTerminate(
            () -> {
              LOG.info("[Forex] All operations completed");
              LOG.info("[Forex] Scheduler Finished at {}", LocalDateTime.now());
            })
        .contextWrite(MDCUtils.contextViewMDC(env))
        .doOnEach(signal -> MDCUtils.setContextMap(contextMap))
        .subscribe();
  }

  private void rollBackForexData(List<String> fiatCurrencies, List<ForexData> forexDataList) {
    forexDataService.deleteForexData();
    fiatCurrencies.forEach(
        fc -> {
          LOG.info("[Forex] Found {} data of Forex Data to RollBack", forexDataList.size());
          if (!forexDataList.isEmpty())
            forexDataService.saveForexData(
                forexDataList.stream()
                    .filter(forex -> forex.getCurrency().equalsIgnoreCase(fc))
                    .findFirst()
                    .orElse(new ForexData()));
        });
  }
}
