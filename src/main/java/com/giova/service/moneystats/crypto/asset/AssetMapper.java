package com.giova.service.moneystats.crypto.asset;

import com.giova.service.moneystats.app.stats.dto.Stats;
import com.giova.service.moneystats.crypto.asset.dto.Asset;
import com.giova.service.moneystats.crypto.asset.entity.AssetEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class AssetMapper {

  public List<Asset> fromAssetEntitiesToAssets(List<AssetEntity> assetEntityList) {
    return assetEntityList.stream()
        .map(
            assetEntity -> {
              Asset asset = new Asset();
              BeanUtils.copyProperties(assetEntity, asset);
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

  public List<Asset> mapAssetList(List<AssetEntity> assetEntities) {
    List<Asset> mapAsset = fromAssetEntitiesToAssets(assetEntities);
    List<Asset> response = new ArrayList<>();
    mapAsset.stream()
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
              } else response.add(asset);
            })
        .collect(Collectors.toList());
    return response;
  }
}
