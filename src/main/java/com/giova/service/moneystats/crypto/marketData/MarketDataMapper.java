package com.giova.service.moneystats.crypto.marketData;

import com.giova.service.moneystats.api.coingecko.dto.CoinGeckoMarketData;
import com.giova.service.moneystats.crypto.marketData.dto.MarketData;
import com.giova.service.moneystats.crypto.marketData.entity.MarketDataEntity;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.utilities.ObjectToolkit;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class MarketDataMapper {

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public static List<MarketData> fromEntityToMarketData(List<MarketDataEntity> marketDataEntities) {
    if (ObjectToolkit.isNullOrEmpty(marketDataEntities)) return Collections.emptyList();
    return marketDataEntities.stream()
        .map(
            marketDataEntity -> {
              MarketData marketData = new MarketData();
              BeanUtils.copyProperties(marketDataEntity, marketData);
              return marketData;
            })
        .collect(Collectors.toList());
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public static MarketData fromCoinGeckoMarketDataToCoinGeckoModel(
      CoinGeckoMarketData coinGeckoMarketData, String category) {
    MarketData marketData = new MarketData();
    marketData.setIdentifier(coinGeckoMarketData.getId());
    marketData.setName(coinGeckoMarketData.getName());
    marketData.setIcon(coinGeckoMarketData.getImage());
    marketData.setMarket_cap(coinGeckoMarketData.getMarket_cap());
    marketData.setCurrent_price(coinGeckoMarketData.getCurrent_price());
    marketData.setRank(coinGeckoMarketData.getMarket_cap_rank());
    marketData.setSymbol(coinGeckoMarketData.getSymbol().toUpperCase());
    marketData.setHigh_24h(coinGeckoMarketData.getHigh_24h());
    marketData.setLow_24h(coinGeckoMarketData.getLow_24h());
    marketData.setMarket_cap_change_24h(coinGeckoMarketData.getMarket_cap_change_24h());
    marketData.setTotal_volume(coinGeckoMarketData.getTotal_volume());
    marketData.setPrice_change_24h(coinGeckoMarketData.getPrice_change_24h());
    marketData.setMarket_cap_change_percentage_24h(
        coinGeckoMarketData.getMarket_cap_change_percentage_24h());
    marketData.setPrice_change_percentage_24h(coinGeckoMarketData.getPrice_change_percentage_24h());
    marketData.setCategory(category);
    return marketData;
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public static List<MarketData> fromCoinGeckoMarketDataListToCoinGeckoList(
      List<CoinGeckoMarketData> coinGeckoMarketData, String category) {
    if (ObjectToolkit.isNullOrEmpty(coinGeckoMarketData)) return Collections.emptyList();
    return coinGeckoMarketData.stream()
        .map(c -> fromCoinGeckoMarketDataToCoinGeckoModel(c, category))
        .collect(Collectors.toList());
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public static List<MarketDataEntity> fromMarketDataToEntity(
      List<MarketData> marketData, String currency) {
    if (ObjectToolkit.isNullOrEmpty(marketData)) return Collections.emptyList();
    return marketData.stream()
        .map(
            marketData1 -> {
              MarketDataEntity marketDataEntity = new MarketDataEntity();
              BeanUtils.copyProperties(marketData1, marketDataEntity);
              marketDataEntity.setCurrency(currency);
              return marketDataEntity;
            })
        .collect(Collectors.toList());
  }
}
