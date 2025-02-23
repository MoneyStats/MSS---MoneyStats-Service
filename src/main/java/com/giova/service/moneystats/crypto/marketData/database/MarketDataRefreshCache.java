package com.giova.service.moneystats.crypto.marketData.database;

import com.giova.service.moneystats.crypto.marketData.dto.MarketData;
import io.github.giovannilamarmora.utils.utilities.ObjectToolkit;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MarketDataRefreshCache {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  private final List<MarketData> marketDataCache = new CopyOnWriteArrayList<>();
  private final AtomicBoolean isMarketDataUpdating = new AtomicBoolean(false);

  public List<MarketData> getMarketData() {
    LOG.info("MarketData are currently updating, getting cached data");
    return List.copyOf(marketDataCache);
  }

  public synchronized void setMarketData(List<MarketData> marketData) {
    isMarketDataUpdating.set(true);
    marketDataCache.clear();
    marketDataCache.addAll(marketData);
  }

  public List<MarketData> getMarketDataByCurrency(String currency) {
    if (ObjectToolkit.isNullOrEmpty(marketDataCache)) return List.of();
    LOG.info("MarketData are currently updating, getting cached data for currency {}", currency);
    return marketDataCache.stream()
        .filter(marketData -> marketData.getCurrency().equalsIgnoreCase(currency))
        .sorted(Comparator.comparing(MarketData::getRank))
        .toList();
  }

  public synchronized void removeMarketData() {
    isMarketDataUpdating.set(false);
    marketDataCache.clear();
  }

  public boolean isUpdating() {
    return isMarketDataUpdating.get();
  }
}
