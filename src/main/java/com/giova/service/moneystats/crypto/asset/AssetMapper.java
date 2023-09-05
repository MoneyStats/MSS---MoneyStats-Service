package com.giova.service.moneystats.crypto.asset;

import android.util.Base64;
import com.giova.service.moneystats.app.stats.dto.Stats;
import com.giova.service.moneystats.app.stats.entity.StatsEntity;
import com.giova.service.moneystats.app.wallet.entity.WalletEntity;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import com.giova.service.moneystats.crypto.asset.dto.Asset;
import com.giova.service.moneystats.crypto.asset.entity.AssetEntity;
import com.giova.service.moneystats.crypto.coinGecko.dto.MarketData;
import com.giova.service.moneystats.crypto.operations.OperationsMapper;
import io.github.giovannilamarmora.utils.math.MathService;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AssetMapper {

  @Autowired private OperationsMapper operationsMapper;

  public List<Asset> fromAssetEntitiesToAssets(
      List<AssetEntity> assetEntityList, List<MarketData> marketData) {
    return assetEntityList.stream()
        .map(
            assetEntity -> {
              Asset asset = new Asset();
              BeanUtils.copyProperties(assetEntity, asset);
              asset.setIcon(getByteArrayFromImageURL(assetEntity.getIcon()));
              asset.setCurrent_price(getAssetValue(marketData, asset));
              asset.setValue(MathService.round(asset.getBalance() * asset.getCurrent_price(), 2));

              if (assetEntity.getHistory() != null && !assetEntity.getHistory().isEmpty()) {
                asset.setHistory(
                    assetEntity.getHistory().stream()
                        .map(
                            statsEntity -> {
                              Stats stats = new Stats();
                              BeanUtils.copyProperties(statsEntity, stats);
                              return stats;
                            })
                        .collect(Collectors.toList()));
                Double lastStatsPerformance =
                    assetEntity.getHistory().get(assetEntity.getHistory().size() - 1).getBalance();
                asset.setPerformance(
                    lastStatsPerformance != 0
                        ? MathService.round(
                            ((asset.getValue() - lastStatsPerformance) / lastStatsPerformance)
                                * 100,
                            2)
                        : 0.0);
              }
              if (assetEntity.getOperations() != null && !assetEntity.getOperations().isEmpty())
                asset.setOperations(
                    operationsMapper.fromOperationsEntitiesToDTOS(assetEntity.getOperations()));

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
              asset.setBalance(MathService.round(asset.getBalance(), 8));
              BeanUtils.copyProperties(asset, assetEntity);
              assetEntity.setIcon(getByteArrayFromImageURL(asset.getIcon()));
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
              if (asset.getOperations() != null && !asset.getOperations().isEmpty())
                assetEntity.setOperations(
                    operationsMapper.fromOperationDTOSToEntities(
                        asset.getOperations(), userEntity, assetEntity));
              return assetEntity;
            })
        .collect(Collectors.toList());
  }

  public List<Asset> mapAssetList(List<Asset> assetList, List<MarketData> marketData) {
    List<Asset> response = new ArrayList<>();
    assetList.stream()
        .peek(
            asset -> {
              Asset assetToReturn = new Asset();
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
                mapResponse.setBalance(
                    MathService.round(mapResponse.getBalance() + asset.getBalance(), 8));
                if (mapResponse.getPerformance() != null)
                  mapResponse.setPerformance(
                      MathService.round(
                          ((mapResponse.getPerformance() + asset.getPerformance()) / 2), 2));
                if (mapResponse.getTrend() != null)
                  mapResponse.setTrend(mapResponse.getTrend() + asset.getTrend());
                mapResponse.setInvested(mapResponse.getInvested() + asset.getInvested());
                mapResponse.setValue(
                    MathService.round(mapResponse.getValue() + asset.getValue(), 2));
                if (asset.getHistory() != null && !asset.getHistory().isEmpty()) {
                  asset.getHistory().stream()
                      .peek(
                          stats -> {
                            Stats indexed = stats;
                            if (response.get(index) != null
                                && response.get(index).getHistory() != null) {
                              Stream<Stats> filter =
                                  response.get(index).getHistory().stream()
                                      .filter(h -> h.getDate().isEqual(stats.getDate()));
                              int indexH =
                                  response
                                      .get(index)
                                      .getHistory()
                                      .indexOf(filter.findFirst().get());
                              indexed = response.get(index).getHistory().get(indexH);
                            }
                            indexed.setId(null);
                            indexed.setBalance(indexed.getBalance() + stats.getBalance());
                            indexed.setTrend(indexed.getTrend() + stats.getTrend());
                            indexed.setPercentage(
                                MathService.round(
                                    ((indexed.getPercentage() + stats.getPercentage()) / 2), 2));
                          })
                      .collect(Collectors.toList());
                }
              } else {
                BeanUtils.copyProperties(asset, assetToReturn);
                assetToReturn.setCurrent_price(getAssetValue(marketData, asset));
                assetToReturn.setValue(
                    MathService.round(asset.getBalance() * asset.getCurrent_price(), 2));
                assetToReturn.setId(null);

                if (asset.getHistory() != null && !asset.getHistory().isEmpty()) {
                  assetToReturn.setHistory(
                      asset.getHistory().stream()
                          .map(
                              stats -> {
                                Stats statsToReturn = new Stats();
                                BeanUtils.copyProperties(stats, statsToReturn);
                                statsToReturn.setId(null);
                                return statsToReturn;
                              })
                          .collect(Collectors.toList()));
                }

                response.add(assetToReturn);
              }
            })
        .collect(Collectors.toList());
    return response.stream()
        .sorted(Comparator.comparing(Asset::getRank))
        .collect(Collectors.toList());
  }

  private Double getAssetValue(List<MarketData> marketData, Asset asset) {
    if (marketData.isEmpty()) {
      return 1D;
    } else {
      return MathService.round(
          marketData.stream()
              .filter(
                  marketData1 ->
                      marketData1.getIdentifier().equalsIgnoreCase(asset.getIdentifier()))
              .findFirst()
              .get()
              .getCurrent_price(),
          2);
    }
  }

  private String getByteArrayFromImageURL(String url) {
    if (url.contains("https://")) {
      try {
        URL imageUrl = new URL(url);
        URLConnection ucon = imageUrl.openConnection();
        InputStream is = ucon.getInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read = 0;
        while ((read = is.read(buffer, 0, buffer.length)) != -1) {
          baos.write(buffer, 0, read);
        }
        baos.flush();
        String encoded =
            Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT).replaceAll("\n", "");
        return "data:image/png;base64," + encoded;
      } catch (Exception e) {
        return url;
      }
    } else return url;
  }
}
