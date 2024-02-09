package com.giova.service.moneystats.scheduler;

import com.giova.service.moneystats.crypto.forex.ForexDataService;
import com.giova.service.moneystats.crypto.forex.dto.ForexData;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.web.ThreadManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CronForexData {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  @Value(value = "#{new Boolean(${rest.scheduled.forex.active:false})}")
  private Boolean isSchedulerActive;

  @Autowired private ForexDataService forexDataService;

  @Scheduled(
      fixedDelayString = "${rest.scheduled.marketData.delay.end}",
      initialDelayString = "${rest.scheduled.marketData.delay.start}")
  @LogInterceptor(type = LogTimeTracker.ActionType.SCHEDULER)
  public void scheduleAllCryptoAsset() {
    LOG.info("[Forex] Scheduler Started at {}", LocalDateTime.now());

    if (!isSchedulerActive) {
      LOG.info("[Forex] Scheduler Active status is NOT-ACTIVE, Stopping Scheduler");
      return;
    }

    // Ottenengo la lista di currency per cui fare il salvataggio a DB
    // List<String> fiatCurrencies = authService.getCryptoFiatUsersCurrency();
    List<String> fiatCurrencies = List.of("USD", "EUR", "GBP");

    if (fiatCurrencies.isEmpty()) {
      LOG.info("No Currency found on Database, Stopping Scheduler");
      return;
    }

    // Mi salvo tutti i Market Data presenti a DB in caso di rollback
    List<ForexData> allForexData = forexDataService.getAllForexData();

    // Cancello tutti i dati dalla tabella MarketData
    forexDataService.deleteForexData();
    try {
      IntStream.range(0, fiatCurrencies.size())
          .forEach(
              index -> {
                LOG.info("Getting and Saving ForexData for currency {}", fiatCurrencies.get(index));
                ForexData forexData;

                forexData =
                    forexDataService.getFromExchangeRateForexData(fiatCurrencies.get(index));
                LOG.info("Found {} rates of Forex Data", forexData.getQuotes().size());
                forexDataService.saveForexData(forexData);
                if (index != fiatCurrencies.size() - 1) ThreadManager.threadSeep(5000);
              });
    } catch (Exception e) {
      LOG.error(
          "Transaction is rolling back cause an error happen during getting Forex for a currency");
      LOG.error("The exception message is {}", e.getMessage());
      LOG.error("Cleaning Forex Database");
      rollBackForexData(fiatCurrencies, allForexData);
      return;
    }
    LOG.info("Scheduler Finished at {}", LocalDateTime.now());
  }

  private void rollBackForexData(List<String> fiatCurrencies, List<ForexData> forexDataList) {
    forexDataService.deleteForexData();
    fiatCurrencies.forEach(
        fc -> {
          LOG.info("Found {} data of Forex Data to RollBack", forexDataList.size());
          if (!forexDataList.isEmpty())
            forexDataService.saveForexData(
                forexDataList.stream()
                    .filter(forex -> forex.getCurrency().equalsIgnoreCase(fc))
                    .findFirst()
                    .orElse(new ForexData()));
        });
  }
}
