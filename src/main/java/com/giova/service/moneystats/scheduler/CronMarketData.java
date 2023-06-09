package com.giova.service.moneystats.scheduler;

import com.giova.service.moneystats.authentication.AuthService;
import com.giova.service.moneystats.crypto.coinGecko.MarketDataService;
import com.giova.service.moneystats.crypto.coinGecko.dto.MarketData;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CronMarketData {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  @Autowired private AuthService authService;
  @Autowired private MarketDataService marketDataService;

  @Scheduled(
      fixedDelayString = "${rest.scheduled.marketData.delay.end}",
      initialDelayString = "${rest.scheduled.marketData.delay.start}")
  void scheduleAllCryptoAsset() {
    LOG.info("Scheduler Started at {}", LocalDateTime.now());

    // Ottenengo la lista di currency per cui fare il salvataggio a DB
    List<String> fiatCurrencies = authService.getCryptoFiatUsersCurrency();

    if (fiatCurrencies.isEmpty()) {
      LOG.info("No Currency found on Database, Stopping Scheduler");
      return;
    }

    fiatCurrencies.stream().peek(fiatCurrency -> {
      LOG.info("Getting and Saving MarketData for currency {}", fiatCurrency);
      List<MarketData> getMarketData = marketDataService.getMarketData(fiatCurrency);
      marketDataService.saveMarketData(getMarketData, fiatCurrency);
    }).collect(Collectors.toList());
  }
}
