package com.giova.service.moneystats.crypto.asset;

import com.giova.service.moneystats.app.stats.dto.Stats;
import com.giova.service.moneystats.app.stats.entity.StatsEntity;
import com.giova.service.moneystats.app.wallet.entity.WalletEntity;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import com.giova.service.moneystats.crypto.asset.dto.Asset;
import com.giova.service.moneystats.crypto.asset.entity.AssetEntity;
import com.giova.service.moneystats.crypto.coinGecko.dto.MarketData;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class AssetMapper {

  public List<Asset> fromAssetEntitiesToAssets(
      List<AssetEntity> assetEntityList, List<MarketData> marketData) {
    return assetEntityList.stream()
        .map(
            assetEntity -> {
              Asset asset = new Asset();
              BeanUtils.copyProperties(assetEntity, asset);
              asset.setValue(asset.getBalance() * getAssetValue(marketData, asset));
              if (assetEntity.getHistory() != null) {
                asset.setHistory(
                    assetEntity.getHistory().stream()
                        .map(
                            statsEntity -> {
                              Stats stats = new Stats();
                              BeanUtils.copyProperties(statsEntity, stats);
                              return stats;
                            })
                        .collect(Collectors.toList()));
              }
              return asset;
            })
        .collect(Collectors.toList());
  }

  public List<AssetEntity> fromAssetToAssetsEntities(
      List<Asset> assetList, UserEntity userEntity, WalletEntity walletEntity) {
    return assetList.stream()
        .map(
            asset -> {
              AssetEntity assetEntity = new AssetEntity();
              BeanUtils.copyProperties(asset, assetEntity);
              if (asset.getHistory() != null) {
                assetEntity.setHistory(
                    asset.getHistory().stream()
                        .map(
                            statsEntity -> {
                              StatsEntity stats = new StatsEntity();
                              BeanUtils.copyProperties(statsEntity, stats);
                              stats.setUser(userEntity);
                              stats.setAsset(assetEntity);
                              return stats;
                            })
                        .collect(Collectors.toList()));
              }
              assetEntity.setUser(userEntity);
              assetEntity.setWallet(walletEntity);
              return assetEntity;
            })
        .collect(Collectors.toList());
  }

  public List<Asset> mapAssetList(List<Asset> assetList, List<MarketData> marketData) {
    List<Asset> response = new ArrayList<>();
    assetList.stream()
        .peek(
            asset -> {
              if (!response.stream()
                  .filter(a -> a.getName().equalsIgnoreCase(asset.getName()))
                  .findAny()
                  .isEmpty()) {
                int index =
                    response.indexOf(
                        response.stream()
                            .filter(a -> a.getName().equalsIgnoreCase(asset.getName()))
                            .findFirst()
                            .get());
                Asset mapResponse = response.get(index);
                mapResponse.setBalance(mapResponse.getBalance() + asset.getBalance());
                if (mapResponse.getPerformance() != null)
                  mapResponse.setPerformance(
                      (mapResponse.getPerformance() + asset.getPerformance()) / 2);
                if (mapResponse.getTrend() != null)
                  mapResponse.setTrend(mapResponse.getTrend() + asset.getTrend());
                mapResponse.setInvested(mapResponse.getInvested() + asset.getInvested());
                mapResponse.setValue(mapResponse.getValue() + asset.getValue());
                if (asset.getHistory() != null) {
                  asset.getHistory().stream()
                      .peek(
                          stats -> {
                            int indexH =
                                response
                                    .get(index)
                                    .getHistory()
                                    .indexOf(
                                        response.get(index).getHistory().stream()
                                            .filter(h -> h.getDate().isEqual(stats.getDate()))
                                            .findFirst()
                                            .get());
                            Stats indexed = response.get(index).getHistory().get(indexH);
                            indexed.setBalance(indexed.getBalance() + stats.getBalance());
                            indexed.setTrend(indexed.getTrend() + stats.getTrend());
                            indexed.setPercentage(
                                (indexed.getPercentage() + stats.getPercentage()) / 2);
                          })
                      .collect(Collectors.toList());
                }
              } else {

                asset.setValue(asset.getBalance() * getAssetValue(marketData, asset));

                response.add(asset);
              }
            })
        .collect(Collectors.toList());
    return response;
  }

  private Double getAssetValue(List<MarketData> marketData, Asset asset) {
    return marketData.stream()
        .filter(marketData1 -> marketData1.getIdentifier().equalsIgnoreCase(asset.getIdentifier()))
        .findFirst()
        .get()
        .getCurrent_price();
  }
}
