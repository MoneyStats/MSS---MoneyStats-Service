package com.giova.service.moneystats.scheduler;

import com.giova.service.moneystats.api.coingecko.CoinGeckoException;
import com.giova.service.moneystats.crypto.coinGecko.MarketDataService;
import com.giova.service.moneystats.crypto.coinGecko.dto.MarketData;
import com.giova.service.moneystats.exception.ExceptionMap;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
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

  @Value(value = "#{new Integer(${rest.scheduled.marketData.quantity:250})}")
  private Integer marketDataQuantity;

  @Autowired private MarketDataService marketDataService;

  @Scheduled(
      fixedDelayString = "${rest.scheduled.marketData.delay.end}",
      initialDelayString = "${rest.scheduled.marketData.delay.start}")
  @LogInterceptor(type = LogTimeTracker.ActionType.SCHEDULER)
  public void scheduleAllCryptoAsset() {
    LOG.info("Scheduler Started at {}", LocalDateTime.now());

    if (!isSchedulerActive) {
      LOG.info("Scheduler Active status is NOT-ACTIVE, Stopping Scheduler");
      return;
    }

    // Ottenengo la lista di currency per cui fare il salvataggio a DB
    // List<String> fiatCurrencies = authService.getCryptoFiatUsersCurrency();
    List<String> fiatCurrencies = List.of("USD", "EUR", "GBP");

    // if (fiatCurrencies.isEmpty()) {
    //  LOG.info("No Currency found on Database, Stopping Scheduler");
    //  return;
    // }

    // Mi salvo tutti i Market Data presenti a DB in caso di rollback
    List<MarketData> allMarketData = marketDataService.getAllMarketData();

    // Cancello tutti i dati dalla tabella MarketData
    marketDataService.deleteMarketData();
    AtomicInteger index = new AtomicInteger(0);

    try {
      fiatCurrencies.forEach(
          fiatCurrency -> {
            LOG.info("Getting and Saving MarketData for currency {}", fiatCurrency);
            List<MarketData> getMarketData = new ArrayList<>();

            getMarketData =
                marketDataService.getCoinGeckoMarketData(fiatCurrency, marketDataQuantity);
            LOG.info("Found {} data of Market Data", getMarketData.size());
            marketDataService.saveMarketData(getMarketData, fiatCurrency);

            if (index.getAndIncrement() != fiatCurrencies.size() - 1) threadSeep();
          });
    } catch (Exception e) {
      LOG.error(
          "Transaction is rolling back cause an error happen during getting MarketData for currency {}",
          fiatCurrencies.get(index.get()));
      LOG.error("The exception message is {}", e.getMessage());
      LOG.error("Cleaning MarketData Database");
      rollBackMarketData(fiatCurrencies, allMarketData);
      return;
    }
    LOG.info("Scheduler Finished at {}", LocalDateTime.now());
  }

  private void rollBackMarketData(List<String> fiatCurrencies, List<MarketData> allMarketData) {
    marketDataService.deleteMarketData();
    fiatCurrencies.forEach(
        fc -> {
          LOG.info("Found {} data of Market Data to RollBack", allMarketData.size());
          if (!allMarketData.isEmpty())
            marketDataService.saveMarketData(
                allMarketData.stream()
                    .filter(marketData -> marketData.getCurrency().equalsIgnoreCase(fc))
                    .collect(Collectors.toList()),
                fc);
        });
  }

  private void threadSeep() {
    try {
      LOG.info("Thread is sleeping for {} millisecond", 60000);
      Thread.sleep(60000);
    } catch (InterruptedException e) {
      LOG.info("An error occurred during sleeping thread, MarketDataService:42");
      throw new CoinGeckoException(
          ExceptionMap.ERR_THREAD_001,
          "An error occurred during sleeping thread, MarketDataService:42");
    }
  }
}
