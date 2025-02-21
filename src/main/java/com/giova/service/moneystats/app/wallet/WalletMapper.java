package com.giova.service.moneystats.app.wallet;

import com.fasterxml.jackson.core.type.TypeReference;
import com.giova.service.moneystats.app.model.Category;
import com.giova.service.moneystats.app.stats.StatsMapper;
import com.giova.service.moneystats.app.stats.dto.Stats;
import com.giova.service.moneystats.app.wallet.dto.Wallet;
import com.giova.service.moneystats.app.wallet.entity.WalletEntity;
import com.giova.service.moneystats.authentication.dto.UserData;
import com.giova.service.moneystats.crypto.asset.AssetMapper;
import com.giova.service.moneystats.crypto.asset.dto.Asset;
import com.giova.service.moneystats.crypto.forex.dto.ForexData;
import com.giova.service.moneystats.crypto.marketData.dto.MarketData;
import com.giova.service.moneystats.crypto.operations.dto.Operations;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.math.MathService;
import io.github.giovannilamarmora.utils.utilities.Mapper;
import io.github.giovannilamarmora.utils.utilities.ObjectToolkit;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WalletMapper {

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public static List<Wallet> mapFromWalletEntitiesToWalletList(
      List<WalletEntity> walletEntities,
      Boolean live,
      Boolean includeAssets,
      Boolean includeFullAssets,
      List<LocalDate> getAllCryptoDates,
      ForexData forex,
      List<MarketData> marketData,
      String userCurrency) {
    if (ObjectToolkit.isNullOrEmpty(walletEntities)) return null;
    AtomicReference<Double> lastBalance = new AtomicReference<>(0D);
    return walletEntities.stream()
        .map(
            (walletEntity -> {
              Wallet wallet = new Wallet();
              BeanUtils.copyProperties(walletEntity, wallet);
              if (!ObjectToolkit.isNullOrEmpty(walletEntity.getInfo())) {
                wallet.setInfo(Mapper.readObject(walletEntity.getInfo(), new TypeReference<>() {}));
              }
              if (!ObjectToolkit.isNullOrEmpty(walletEntity.getHistory())) {
                lastBalance.set(walletEntity.getHistory().getLast().getBalance());
                wallet.setHistory(StatsMapper.fromEntityToStats(walletEntity.getHistory()));
              }
              if ((includeAssets || includeFullAssets)
                  && !ObjectToolkit.isNullOrEmpty(walletEntity.getAssets())) {

                wallet.setAssets(
                    AssetMapper.fromAssetEntitiesToAssets(
                        walletEntity.getAssets(), marketData, getAllCryptoDates));
              } else if (live
                  && !includeAssets
                  && !includeFullAssets
                  && !ObjectToolkit.isNullOrEmpty(walletEntity.getAssets())) {

                wallet.setAssets(
                    AssetMapper.fromAssetEntitiesLivePriceToAssets(
                        walletEntity.getAssets(), marketData));
              }
              if (live) setLivePriceInWallet(wallet, forex, lastBalance, userCurrency);
              if (live && !includeAssets && !includeFullAssets) wallet.setAssets(null);
              return wallet;
            }))
        .toList();
  }

  private static void setLivePriceInWallet(
      Wallet wallet, ForexData forex, AtomicReference<Double> lastBalance, String userCurrency) {
    if (ObjectToolkit.isNullOrEmpty(forex)
        || ObjectToolkit.isNullOrEmpty(forex.getQuotes())
        || ObjectToolkit.isNullOrEmpty(wallet.getAssets())) return;

    if (!wallet.getCategory().equalsIgnoreCase(Category.CRYPTO.getValue())) return;
    AtomicReference<Double> balance = new AtomicReference<>(0D);
    wallet.getAssets().forEach(asset -> balance.updateAndGet(v -> v + asset.getValue()));
    double converter = forex.getQuotes().get(userCurrency);
    wallet.setBalance(MathService.round(balance.get() * converter, 2));
    wallet.setDifferenceLastStats(MathService.round(wallet.getBalance() - lastBalance.get(), 2));
    wallet.setPerformanceLastStats(
        wallet.getBalance() == 0 || lastBalance.get() == 0
            ? 0D
            : MathService.round(
                ((wallet.getBalance() - lastBalance.get()) / lastBalance.get()) * 100, 2));
    if (wallet.getBalance() > wallet.getHighPrice()) {
      wallet.setHighPrice(wallet.getBalance());
      wallet.setHighPriceDate(LocalDate.now());
    }
    if (wallet.getBalance() < wallet.getLowPrice()) {
      wallet.setLowPrice(wallet.getBalance());
      wallet.setLowPriceDate(LocalDate.now());
    }
    if (wallet.getBalance() > wallet.getAllTimeHigh()) {
      wallet.setAllTimeHigh(wallet.getBalance());
      wallet.setAllTimeHighDate(LocalDate.now());
    }
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public static WalletEntity fromWalletToWalletEntity(Wallet wallet, UserData userData) {
    WalletEntity walletEntity = new WalletEntity();
    BeanUtils.copyProperties(wallet, walletEntity);
    mapEmptyDataWalletEntity(walletEntity);
    walletEntity.setUserIdentifier(userData.getIdentifier());

    // if (wallet.getInfoString() != null && !wallet.getInfoString().isEmpty()) {
    walletEntity.setInfo(wallet.getInfoString());
    // } else
    if (!ObjectToolkit.isNullOrEmpty(wallet.getInfo())) {
      walletEntity.setInfo(Mapper.convertMapToString(wallet.getInfo()));
    }
    walletEntity.setHistory(
        StatsMapper.fromStatsToEntity(wallet.getHistory(), walletEntity, userData));

    walletEntity.setAssets(
        AssetMapper.fromAssetToAssetsEntities(wallet.getAssets(), userData, walletEntity));
    return walletEntity;
  }

  /**
   * Used to Prevent Live wallet to update data into the Database
   *
   * @param walletEntity Edited with Database Data
   * @param getFromDB Database Wallet
   */
  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public static void mapWalletEntityToBeSaved(WalletEntity walletEntity, WalletEntity getFromDB) {
    if (ObjectToolkit.isNullOrEmpty(getFromDB)) return;
    walletEntity.setBalance(getFromDB.getBalance());
    walletEntity.setPerformanceLastStats(getFromDB.getPerformanceLastStats());
    walletEntity.setDifferenceLastStats(getFromDB.getDifferenceLastStats());
    walletEntity.setHighPrice(getFromDB.getHighPrice());
    walletEntity.setHighPriceDate(getFromDB.getHighPriceDate());
    walletEntity.setLowPrice(getFromDB.getLowPrice());
    walletEntity.setLowPriceDate(getFromDB.getLowPriceDate());
    walletEntity.setAllTimeHigh(getFromDB.getAllTimeHigh());
    walletEntity.setAllTimeHighDate(getFromDB.getAllTimeHighDate());
  }

  /**
   * Used to map Empty Field during the Save Wallet
   *
   * @param walletEntity Edited with empty data
   */
  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public static void mapEmptyDataWalletEntity(WalletEntity walletEntity) {
    if (ObjectToolkit.isNullOrEmpty(walletEntity.getBalance())) walletEntity.setBalance(0D);
    if (ObjectToolkit.isNullOrEmpty(walletEntity.getPerformanceLastStats()))
      walletEntity.setPerformanceLastStats(0D);
    if (ObjectToolkit.isNullOrEmpty(walletEntity.getDifferenceLastStats()))
      walletEntity.setDifferenceLastStats(0D);
    if (ObjectToolkit.isNullOrEmpty(walletEntity.getHighPrice())) walletEntity.setHighPrice(0D);
    if (ObjectToolkit.isNullOrEmpty(walletEntity.getHighPriceDate()))
      walletEntity.setHighPriceDate(LocalDate.now());
    if (ObjectToolkit.isNullOrEmpty(walletEntity.getLowPrice())) walletEntity.setLowPrice(0D);
    if (ObjectToolkit.isNullOrEmpty(walletEntity.getLowPriceDate()))
      walletEntity.setLowPriceDate(LocalDate.now());
    if (ObjectToolkit.isNullOrEmpty(walletEntity.getAllTimeHigh())) walletEntity.setAllTimeHigh(0D);
    if (ObjectToolkit.isNullOrEmpty(walletEntity.getAllTimeHighDate()))
      walletEntity.setAllTimeHighDate(LocalDate.now());
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public static Wallet fromWalletEntityToWallet(
      WalletEntity walletEntity,
      List<LocalDate> getAllCryptoDates,
      List<MarketData> marketDataList) {
    Wallet wallet = new Wallet();
    BeanUtils.copyProperties(walletEntity, wallet);

    if (!ObjectToolkit.isNullOrEmpty(walletEntity.getInfo()))
      wallet.setInfo(
          Mapper.readObject(walletEntity.getInfo(), new TypeReference<Map<String, String>>() {}));

    wallet.setHistory(StatsMapper.fromEntityToStats(walletEntity.getHistory()));
    wallet.setAssets(
        AssetMapper.fromAssetEntitiesToAssets(
            walletEntity.getAssets(), marketDataList, getAllCryptoDates));
    return wallet;
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public List<Wallet> deleteWalletIds(List<Wallet> wallets) {
    return wallets.stream()
        .map(
            (walletToEdit -> {
              Wallet wallet = new Wallet();
              walletToEdit.setId(null);
              BeanUtils.copyProperties(walletToEdit, wallet);

              if (!ObjectToolkit.isNullOrEmpty(walletToEdit.getHistory())) {
                wallet.setHistory(
                    walletToEdit.getHistory().stream()
                        .map(
                            statsEntity -> {
                              Stats stats = new Stats();
                              statsEntity.setId(null);
                              BeanUtils.copyProperties(statsEntity, stats);
                              return stats;
                            })
                        .toList());
              }
              if (!ObjectToolkit.isNullOrEmpty(walletToEdit.getAssets())) {
                wallet.setAssets(
                    walletToEdit.getAssets().stream()
                        .map(
                            assetMap -> {
                              Asset asset = new Asset();
                              assetMap.setId(null);
                              BeanUtils.copyProperties(assetMap, asset);
                              if (!ObjectToolkit.isNullOrEmpty(assetMap.getHistory())) {
                                asset.setHistory(
                                    assetMap.getHistory().stream()
                                        .map(
                                            statsEntity -> {
                                              Stats stats = new Stats();
                                              statsEntity.setId(null);
                                              BeanUtils.copyProperties(statsEntity, stats);
                                              return stats;
                                            })
                                        .toList());
                              }
                              if (!ObjectToolkit.isNullOrEmpty(assetMap.getOperations())) {
                                asset.setOperations(
                                    assetMap.getOperations().stream()
                                        .map(
                                            operationEntity -> {
                                              Operations operations = new Operations();
                                              operationEntity.setId(null);
                                              BeanUtils.copyProperties(operationEntity, operations);
                                              return operations;
                                            })
                                        .toList());
                              }
                              return asset;
                            })
                        .toList());
              }
              return wallet;
            }))
        .toList();
  }
}
