package com.giova.service.moneystats.scheduler;

import com.giova.service.moneystats.crypto.coinGecko.MarketDataService;
import com.giova.service.moneystats.crypto.coinGecko.dto.MarketData;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CronMarketData {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  @Value(value = "#{new Boolean(${rest.scheduled.marketData.active:false})}")
  private Boolean isSchedulerActive;

  @Autowired private MarketDataService marketDataService;

  @Scheduled(
      fixedDelayString = "${rest.scheduled.marketData.delay.end}",
      initialDelayString = "${rest.scheduled.marketData.delay.start}")
  @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = Exception.class)
  void scheduleAllCryptoAsset() {
    LOG.info("Scheduler Started at {}", LocalDateTime.now());

    if (!isSchedulerActive) {
      LOG.info("Scheduler Active status is {}, Stopping Scheduler", isSchedulerActive);
      return;
    }

    // Ottenengo la lista di currency per cui fare il salvataggio a DB
    // List<String> fiatCurrencies = authService.getCryptoFiatUsersCurrency();
    List<String> fiatCurrencies = List.of("USD", "EUR", "GBP");

    if (fiatCurrencies.isEmpty()) {
      LOG.info("No Currency found on Database, Stopping Scheduler");
      return;
    }

    // Cancello tutti i dati dalla tabella MarketData
    marketDataService.deleteMarketData();

    fiatCurrencies.stream()
        .peek(
            fiatCurrency -> {
              LOG.info("Getting and Saving MarketData for currency {}", fiatCurrency);
              List<MarketData> getMarketData =
                  marketDataService.getCoinGeckoMarketData(fiatCurrency);
              marketDataService.saveMarketData(getMarketData, fiatCurrency);
            })
        .collect(Collectors.toList());
    LOG.info("Scheduler Finished at {}", LocalDateTime.now());
  }
}
