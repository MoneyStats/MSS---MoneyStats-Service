package com.giova.service.moneystats.crypto.coinGecko;

import com.giova.service.moneystats.api.coingecko.dto.CoinGeckoMarketData;
import com.giova.service.moneystats.crypto.coinGecko.dto.MarketData;
import com.giova.service.moneystats.crypto.coinGecko.entity.MarketDataEntity;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class MarketDataMapper {

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_MAPPER)
  public MarketData fromCoinGeckoMarketDataToCoinGeckoModel(
      CoinGeckoMarketData coinGeckoMarketData) {
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
    return marketData;
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_MAPPER)
  public List<MarketData> fromCoinGeckoMarketDataListToCoinGeckoList(
      List<CoinGeckoMarketData> coinGeckoMarketData) {
    return coinGeckoMarketData.stream()
        .map(this::fromCoinGeckoMarketDataToCoinGeckoModel)
        .collect(Collectors.toList());
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_MAPPER)
  public List<MarketDataEntity> fromMarketDataToEntity(
      List<MarketData> marketData, String currency) {
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

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_MAPPER)
  public List<MarketData> fromEntityToMarketData(
      List<MarketDataEntity> marketDataEntities) {
    return marketDataEntities.stream()
        .map(
            marketDataEntity -> {
              MarketData marketData = new MarketData();
              BeanUtils.copyProperties(marketDataEntity, marketData);
              return marketData;
            })
        .collect(Collectors.toList());
  }
}
