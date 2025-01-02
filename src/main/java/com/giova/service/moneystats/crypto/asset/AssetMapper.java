package com.giova.service.moneystats.crypto.asset;

import com.giova.service.moneystats.app.stats.StatsMapper;
import com.giova.service.moneystats.app.stats.dto.Stats;
import com.giova.service.moneystats.app.stats.entity.StatsEntity;
import com.giova.service.moneystats.app.wallet.entity.WalletEntity;
import com.giova.service.moneystats.authentication.dto.UserData;
import com.giova.service.moneystats.crypto.asset.dto.Asset;
import com.giova.service.moneystats.crypto.asset.dto.AssetLivePrice;
import com.giova.service.moneystats.crypto.asset.dto.AssetWithoutOpAndStats;
import com.giova.service.moneystats.crypto.asset.entity.AssetEntity;
import com.giova.service.moneystats.crypto.marketData.dto.MarketData;
import com.giova.service.moneystats.crypto.operations.OperationsMapper;
import com.giova.service.moneystats.crypto.operations.dto.Operations;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.math.MathService;
import io.github.giovannilamarmora.utils.utilities.Utilities;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class AssetMapper {

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public static List<Asset> fromAssetEntitiesToAssets(
      List<AssetEntity> assetEntityList, List<MarketData> marketData, List<LocalDate> getAllDates) {
    if (Utilities.isNullOrEmpty(assetEntityList)) return Collections.emptyList();
    LocalDate lastDate =
        (!Utilities.isNullOrEmpty(getAllDates)) ? getAllDates.getLast() : LocalDate.now();
    return assetEntityList.stream()
        .map(
            assetEntity -> {
              Asset asset = new Asset();
              BeanUtils.copyProperties(assetEntity, asset);
              asset.setIcon(Utilities.getByteArrayFromImageURL(assetEntity.getIcon()));
              asset.setCurrent_price(getAssetValue(marketData, asset));
              asset.setValue(MathService.round(asset.getBalance() * asset.getCurrent_price(), 2));

              if (!Utilities.isNullOrEmpty(assetEntity.getHistory())) {
                asset.setHistory(StatsMapper.fromEntityToStats(assetEntity.getHistory()));
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
              asset.setOperations(
                  OperationsMapper.fromOperationsEntitiesToDTOS(assetEntity.getOperations()));
              return asset;
            })
        .toList();
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public static List<Asset> fromAssetEntitiesLivePriceToAssets(
      List<AssetEntity> assetEntityList, List<MarketData> marketData) {
    if (Utilities.isNullOrEmpty(assetEntityList)) return null;
    return assetEntityList.stream()
        .map(
            assetEntity -> {
              Asset asset = new Asset();
              BeanUtils.copyProperties(assetEntity, asset);
              asset.setCurrent_price(getAssetValue(marketData, asset));
              asset.setValue(MathService.round(asset.getBalance() * asset.getCurrent_price(), 2));
              return asset;
            })
        .toList();
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public static List<AssetEntity> fromAssetLivePricesToAssetEntities(
      List<AssetLivePrice> assetLivePrices, Long walletID) {
    if (Utilities.isNullOrEmpty(assetLivePrices)) return null;
    return assetLivePrices.stream()
        .filter(assetLivePrice -> Objects.equals(assetLivePrice.getWalletId(), walletID))
        .map(
            assetLivePrice -> {
              AssetEntity assetEntity = new AssetEntity();
              BeanUtils.copyProperties(assetLivePrice, assetEntity);
              return assetEntity;
            })
        .toList();
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public static List<AssetEntity> fromAssetToAssetEntities(
      List<AssetWithoutOpAndStats> assetWithoutOpAndStatsList, Long walletID) {
    if (Utilities.isNullOrEmpty(assetWithoutOpAndStatsList)) return null;
    return assetWithoutOpAndStatsList.stream()
        .filter(
            assetWithoutOpAndStats ->
                Objects.equals(assetWithoutOpAndStats.getWalletId(), walletID))
        .map(
            assetWithoutOpAndStats -> {
              AssetEntity assetEntity = new AssetEntity();
              BeanUtils.copyProperties(assetWithoutOpAndStats, assetEntity);
              return assetEntity;
            })
        .toList();
  }

  private static Double getAssetValue(List<MarketData> marketData, Asset asset) {
    if (Utilities.isNullOrEmpty(marketData) || marketData.isEmpty()) {
      return 1D;
    } else {
      double current_price =
          marketData.stream()
              .filter(
                  marketData1 ->
                      marketData1.getIdentifier().equalsIgnoreCase(asset.getIdentifier()))
              .findFirst()
              .map(MarketData::getCurrent_price)
              .orElse(1D);
      final double THRESHOLD = 1;
      if (current_price < THRESHOLD && current_price > 0) {
        return current_price;
      } else {
        return MathService.round(current_price, 2);
      }
    }
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public static List<AssetEntity> fromAssetToAssetsEntities(
          List<Asset> assetList, UserData user, WalletEntity walletEntity) {
    if (Utilities.isNullOrEmpty(assetList)) return null;
    return assetList.stream()
        .map(
            asset -> {
              AssetEntity assetEntity = new AssetEntity();
              asset.setBalance(MathService.round(asset.getBalance(), 8));
              asset.setInvested(MathService.round(asset.getInvested(), 2));
              BeanUtils.copyProperties(asset, assetEntity);
              assetEntity.setIcon(Utilities.getByteArrayFromImageURL(asset.getIcon()));
              if (asset.getHistory() != null) {
                assetEntity.setHistory(
                    asset.getHistory().stream()
                        .map(
                            statsEntity -> {
                              StatsEntity stats = new StatsEntity();
                              BeanUtils.copyProperties(statsEntity, stats);
                              stats.setUserIdentifier(user.getIdentifier());
                              stats.setAsset(assetEntity);
                              return stats;
                            })
                        .collect(Collectors.toList()));
              }
              assetEntity.setUserIdentifier(user.getIdentifier());
              assetEntity.setWallet(walletEntity);
              assetEntity.setOperations(
                  OperationsMapper.fromOperationDTOSToEntities(
                      asset.getOperations(), user, assetEntity));
              return assetEntity;
            })
        .toList();
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public static List<Asset> mapAssetList(
      List<Asset> assetList, List<MarketData> marketData, List<LocalDate> getAllDates) {
    Map<String, Asset> assetMap = new HashMap<>();

    assetList.forEach(
        asset -> {
          Asset existingAsset = assetMap.get(asset.getName());

          if (!Utilities.isNullOrEmpty(existingAsset)) {
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

            if (!Utilities.isNullOrEmpty(asset.getHistory())) {
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
    return assetMap.values().stream().sorted(Comparator.comparing(Asset::getRank)).toList();
  }

  private static void updateExistingAsset(
      Asset existingAsset, Asset newAsset, List<LocalDate> getAllDates) {
    LocalDate lastDate = getAllDates.getLast();
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

    if (!Utilities.isNullOrEmpty(existingAsset.getPerformance())
        && !Utilities.isNullOrEmpty(newAsset.getPerformance())) {
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

    if (!Utilities.isNullOrEmpty(existingAsset.getTrend())
        && !Utilities.isNullOrEmpty(newAsset.getTrend())) {
      existingAsset.setTrend(
          MathService.round(existingAssetLastStats.getTrend() + newAssetLastStats.getTrend(), 2));
    }

    existingAsset.setInvested(
        MathService.round(existingAsset.getInvested() + newAsset.getInvested(), 2));

    if (!Utilities.isNullOrEmpty(newAsset.getHistory())) {
      newAsset
          .getHistory()
          .forEach(
              newStats -> {
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
                  List<Stats> histories = new ArrayList<>(existingAsset.getHistory());
                  histories.add(newStatsToAdd);
                  existingAsset.setHistory(histories);
                }
              });
    }

    if (!Utilities.isNullOrEmpty(newAsset.getOperations())) {
      // Aggiungi logica per aggiungere le nuove operazioni
      if (!Utilities.isNullOrEmpty(existingAsset.getOperations())) {
        List<Operations> operations = new ArrayList<>(existingAsset.getOperations());
        operations.addAll(newAsset.getOperations());
        existingAsset.setOperations(operations);
      } else {
        existingAsset.setOperations(newAsset.getOperations());
      }
    }
  }
}
