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
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.math.MathService;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AssetMapper {

  private final UserEntity user;
  @Autowired private OperationsMapper operationsMapper;

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_MAPPER)
  public List<Asset> fromAssetEntitiesToAssets(
      List<AssetEntity> assetEntityList, List<MarketData> marketData, List<LocalDate> getAllDates) {
    LocalDate lastDate =
        (getAllDates != null && !getAllDates.isEmpty())
            ? getAllDates.get(getAllDates.size() - 1)
            : LocalDate.now();
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
                Stats lastStats =
                    asset.getHistory().stream()
                        .filter(a -> a.getDate().isEqual(lastDate))
                        .findFirst()
                        .orElse(new Stats(lastDate, 0D, 0D, 0D));
                asset.setPerformance(
                    lastStats.getBalance() != 0
                        ? MathService.round(
                            ((asset.getValue() - lastStats.getBalance()) / lastStats.getBalance())
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

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_MAPPER)
  public List<AssetEntity> fromAssetToAssetsEntities(
      List<Asset> assetList, UserEntity userEntity, WalletEntity walletEntity) {
    return assetList.stream()
        .map(
            asset -> {
              AssetEntity assetEntity = new AssetEntity();
              asset.setBalance(MathService.round(asset.getBalance(), 8));
              asset.setInvested(MathService.round(asset.getInvested(), 2));
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

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_MAPPER)
  public List<Asset> mapAssetList(
      List<Asset> assetList, List<MarketData> marketData, List<LocalDate> getAllDates) {
    Map<String, Asset> assetMap = new HashMap<>();

    assetList.forEach(
        asset -> {
          Asset existingAsset = assetMap.get(asset.getName());

          if (existingAsset != null) {
            // Aggiorna l'asset esistente
            updateExistingAsset(existingAsset, asset, getAllDates);
          } else {
            // Crea un nuovo asset
            Asset newAsset = new Asset();
            BeanUtils.copyProperties(asset, newAsset);
            newAsset.setCurrent_price(getAssetValue(marketData, asset));
            newAsset.setValue(
                MathService.round(asset.getBalance() * newAsset.getCurrent_price(), 2));
            newAsset.setId(null);

            if (asset.getHistory() != null && !asset.getHistory().isEmpty()) {
              newAsset.setHistory(
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

            assetMap.put(asset.getName(), newAsset);
          }
        });

    return assetMap.values().stream()
        .sorted(Comparator.comparing(Asset::getRank))
        .collect(Collectors.toList());
  }

  private void updateExistingAsset(
      Asset existingAsset, Asset newAsset, List<LocalDate> getAllDates) {
    LocalDate lastDate = getAllDates.get(getAllDates.size() - 1);
    LocalDate beforeLastDate =
        getAllDates.size() >= 2 ? getAllDates.get(getAllDates.size() - 2) : null;
    existingAsset.setBalance(
        MathService.round(existingAsset.getBalance() + newAsset.getBalance(), 8));

    existingAsset.setValue(MathService.round(existingAsset.getValue() + newAsset.getValue(), 2));

    Stats existingAssetLastStats =
        existingAsset.getHistory() != null
            ? existingAsset.getHistory().stream()
                .filter(a -> a.getDate().isEqual(lastDate))
                .findFirst()
                .orElse(new Stats(lastDate, 0D, 0D, 0D))
            : new Stats(lastDate, 0D, 0D, 0D);
    Stats newAssetLastStats =
        newAsset.getHistory() != null
            ? newAsset.getHistory().stream()
                .filter(a -> a.getDate().isEqual(lastDate))
                .findFirst()
                .orElse(new Stats(lastDate, 0D, 0D, 0D))
            : new Stats(lastDate, 0D, 0D, 0D);

    if (existingAsset.getPerformance() != null && newAsset.getPerformance() != null) {
      // existingAsset.setPerformance(
      //    MathService.round((existingAsset.getPerformance() + newAsset.getPerformance()) / 2, 2));
      Double lastStatsBalance =
          existingAssetLastStats.getBalance() + newAssetLastStats.getBalance();
      existingAsset.setPerformance(
          lastStatsBalance != 0
              ? MathService.round(
                  ((existingAsset.getValue() - lastStatsBalance) / lastStatsBalance) * 100, 2)
              : 0.0);
    }

    if (existingAsset.getTrend() != null && newAsset.getTrend() != null) {
      existingAsset.setTrend(
          MathService.round(existingAssetLastStats.getTrend() + newAssetLastStats.getTrend(), 2));
    }

    existingAsset.setInvested(
        MathService.round(existingAsset.getInvested() + newAsset.getInvested(), 2));

    if (newAsset.getHistory() != null && !newAsset.getHistory().isEmpty()) {
      for (Stats newStats : newAsset.getHistory()) {
        if (existingAsset.getHistory() == null) {
          existingAsset.setHistory(new ArrayList<>());
        }

        Optional<Stats> existingStatsOptional =
            existingAsset.getHistory().stream()
                .filter(h -> h.getDate().isEqual(newStats.getDate()))
                .findFirst();

        Optional<Stats> beforeStatsOptional =
            beforeLastDate != null
                ? existingAsset.getHistory().stream()
                    .filter(h -> h.getDate().isEqual(beforeLastDate))
                    .findFirst()
                : Optional.empty();

        if (existingStatsOptional.isPresent()) {
          // Aggiorna l'elemento esistente nella history
          Stats existingStats = existingStatsOptional.get();
          existingStats.setBalance(
              MathService.round(existingStats.getBalance() + newStats.getBalance(), 2));
          existingStats.setTrend(
              MathService.round(existingStats.getTrend() + newStats.getTrend(), 2));
          if (beforeStatsOptional.isEmpty()) existingStats.setPercentage(0D);
          else
            existingStats.setPercentage(
                MathService.round(
                    ((existingStats.getBalance() - beforeStatsOptional.get().getBalance())
                            / beforeStatsOptional.get().getBalance())
                        * 100,
                    2));
        } else {
          // Aggiungi un nuovo elemento alla history
          Stats newStatsToAdd = new Stats();
          BeanUtils.copyProperties(newStats, newStatsToAdd);
          newStatsToAdd.setId(null);
          existingAsset.getHistory().add(newStatsToAdd);
        }
      }
    }

    if (newAsset.getOperations() != null && !newAsset.getOperations().isEmpty()) {
      // Aggiungi logica per aggiungere le nuove operazioni
      if (existingAsset.getOperations() != null) {
        existingAsset.getOperations().addAll(newAsset.getOperations());
      } else {
        existingAsset.setOperations(newAsset.getOperations());
      }
    }
  }

  @Deprecated
  public List<Asset> mapAssetListOLD(List<Asset> assetList, List<MarketData> marketData) {
    List<Asset> response = new ArrayList<>();
    assetList.forEach(
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
            if (mapResponse.getPerformance() != null && asset.getPerformance() != null)
              mapResponse.setPerformance(
                  MathService.round(
                      ((mapResponse.getPerformance() + asset.getPerformance()) / 2), 2));
            if (mapResponse.getTrend() != null && asset.getTrend() != null)
              mapResponse.setTrend(mapResponse.getTrend() + asset.getTrend());
            mapResponse.setInvested(mapResponse.getInvested() + asset.getInvested());
            mapResponse.setValue(MathService.round(mapResponse.getValue() + asset.getValue(), 2));
            if (asset.getHistory() != null && !asset.getHistory().isEmpty()) {
              mapResponse.setHistory(
                  asset.getHistory().stream()
                      .map(
                          stats -> {
                            if (response.get(index) == null
                                || response.get(index).getHistory() == null
                                || response.get(index).getHistory().stream()
                                    .filter(h -> h.getDate().isEqual(stats.getDate()))
                                    .findFirst()
                                    .isEmpty()) {
                              stats.setId(null);
                              return stats;
                            }
                            Stream<Stats> filter =
                                response.get(index).getHistory().stream()
                                    .filter(h -> h.getDate().isEqual(stats.getDate()));
                            int indexH =
                                response.get(index).getHistory().indexOf(filter.findFirst().get());
                            Stats indexed = response.get(index).getHistory().get(indexH);

                            indexed.setId(null);
                            indexed.setBalance(indexed.getBalance() + stats.getBalance());
                            indexed.setTrend(indexed.getTrend() + stats.getTrend());
                            indexed.setPercentage(
                                MathService.round(
                                    ((indexed.getPercentage() + stats.getPercentage()) / 2), 2));
                            return indexed;
                          })
                      .collect(Collectors.toList()));
              if (asset.getOperations() != null && !asset.getOperations().isEmpty())
                if (mapResponse.getOperations() != null)
                  mapResponse.getOperations().addAll(asset.getOperations());
                else mapResponse.setOperations(asset.getOperations());
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
        });
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
              .map(MarketData::getCurrent_price)
              .orElse(1D),
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
