package com.giova.service.moneystats.app.wallet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giova.service.moneystats.app.model.Category;
import com.giova.service.moneystats.app.stats.StatsMapper;
import com.giova.service.moneystats.app.stats.dto.Stats;
import com.giova.service.moneystats.app.wallet.dto.Wallet;
import com.giova.service.moneystats.app.wallet.entity.WalletEntity;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import com.giova.service.moneystats.crypto.asset.AssetMapper;
import com.giova.service.moneystats.crypto.asset.dto.Asset;
import com.giova.service.moneystats.crypto.coinGecko.MarketDataService;
import com.giova.service.moneystats.crypto.coinGecko.dto.MarketData;
import com.giova.service.moneystats.crypto.forex.ForexDataService;
import com.giova.service.moneystats.crypto.forex.dto.ForexData;
import com.giova.service.moneystats.crypto.operations.dto.Operations;
import com.giova.service.moneystats.utilities.Utils;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.math.MathService;
import io.github.giovannilamarmora.utils.utilities.Mapper;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WalletMapper {

  private final UserEntity user;
  private final ObjectMapper mapper = new ObjectMapper();
  @Autowired private AssetMapper assetMapper;
  @Autowired private MarketDataService marketDataService;
  @Autowired private ForexDataService forexDataService;

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
    if (Utils.isNullOrEmpty(walletEntities)) return null;
    AtomicReference<Double> lastBalance = new AtomicReference<>(0D);
    return walletEntities.stream()
        .map(
            (walletEntity -> {
              Wallet wallet = new Wallet();
              BeanUtils.copyProperties(walletEntity, wallet);
              if (!Utils.isNullOrEmpty(walletEntity.getInfo())) {
                wallet.setInfo(Mapper.readObject(walletEntity.getInfo(), new TypeReference<>() {}));
              }
              if (!Utils.isNullOrEmpty(walletEntity.getHistory())) {
                lastBalance.set(walletEntity.getHistory().getLast().getBalance());
                wallet.setHistory(StatsMapper.fromEntityToStats(walletEntity.getHistory()));
              }
              if ((includeAssets || includeFullAssets)
                  && !Utils.isNullOrEmpty(walletEntity.getAssets())) {

                wallet.setAssets(
                    AssetMapper.fromAssetEntitiesToAssets(
                        walletEntity.getAssets(), marketData, getAllCryptoDates));
              } else if (live
                  && !includeAssets
                  && !includeFullAssets
                  && !Utils.isNullOrEmpty(walletEntity.getAssets())) {

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
    if (Utils.isNullOrEmpty(forex) || Utils.isNullOrEmpty(wallet.getAssets())) return;

    if (!wallet.getCategory().equalsIgnoreCase(Category.CRYPTO.getValue())) return;
    AtomicReference<Double> balance = new AtomicReference<>(0D);
    wallet.getAssets().forEach(asset -> balance.updateAndGet(v -> v + asset.getValue()));
    double converter = forex.getQuotes().get(userCurrency);
    wallet.setBalance(MathService.round(balance.get() * converter, 2));
    wallet.setDifferenceLastStats(MathService.round(wallet.getBalance() - lastBalance.get(), 2));
    wallet.setPerformanceLastStats(
        wallet.getBalance() == 0 && lastBalance.get() == 0
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
  public static WalletEntity fromWalletToWalletEntity(Wallet wallet, UserEntity userEntity) {
    WalletEntity walletEntity = new WalletEntity();
    BeanUtils.copyProperties(wallet, walletEntity);
    walletEntity.setUser(userEntity);

    // if (wallet.getInfoString() != null && !wallet.getInfoString().isEmpty()) {
    walletEntity.setInfo(wallet.getInfoString());
    // } else
    if (!Utils.isNullOrEmpty(wallet.getInfo())) {
      walletEntity.setInfo(Mapper.convertMapToString(wallet.getInfo()));
    }
    walletEntity.setHistory(
        StatsMapper.fromStatsToEntity(wallet.getHistory(), walletEntity, userEntity));

    walletEntity.setAssets(
        AssetMapper.fromAssetToAssetsEntities(wallet.getAssets(), userEntity, walletEntity));
    return walletEntity;
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public static void mapWalletEntityToBeSaved(WalletEntity walletEntity, WalletEntity getFromDB) {
    if (Utils.isNullOrEmpty(getFromDB)) return;
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

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public static Wallet fromWalletEntityToWallet(
      WalletEntity walletEntity,
      List<LocalDate> getAllCryptoDates,
      List<MarketData> marketDataList) {
    Wallet wallet = new Wallet();
    BeanUtils.copyProperties(walletEntity, wallet);

    if (!Utils.isNullOrEmpty(walletEntity.getInfo()))
      wallet.setInfo(
          Mapper.readObject(walletEntity.getInfo(), new TypeReference<Map<String, String>>() {}));

    wallet.setHistory(StatsMapper.fromEntityToStats(walletEntity.getHistory()));
    wallet.setAssets(
        AssetMapper.fromAssetEntitiesToAssets(
            walletEntity.getAssets(), marketDataList, getAllCryptoDates));
    return wallet;
  }

  /* @important OLD DATA */
  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public List<Wallet> fromWalletEntitiesToWallets(
      List<WalletEntity> walletEntities, Boolean live, List<LocalDate> getAllCryptoDates) {
    ForexData forex = null;
    if (live) forex = forexDataService.getForexData(user.getSettings().getCryptoCurrency());
    ForexData finalForex = forex;
    AtomicReference<Double> lastBalance = new AtomicReference<>(0D);
    return walletEntities.stream()
        .map(
            (walletEntity -> {
              Wallet wallet = new Wallet();
              BeanUtils.copyProperties(walletEntity, wallet);
              if (walletEntity.getInfo() != null) {
                try {
                  wallet.setInfo(
                      mapper.readValue(
                          walletEntity.getInfo(), new TypeReference<Map<String, String>>() {}));
                } catch (JsonProcessingException e) {
                  throw new RuntimeException(e);
                }
              }
              if (walletEntity.getHistory() != null && !walletEntity.getHistory().isEmpty()) {
                lastBalance.set(
                    walletEntity
                        .getHistory()
                        .get(walletEntity.getHistory().size() - 1)
                        .getBalance());
                wallet.setHistory(
                    walletEntity.getHistory().stream()
                        .map(
                            statsEntity -> {
                              Stats stats = new Stats();
                              BeanUtils.copyProperties(statsEntity, stats);
                              return stats;
                            })
                        .collect(Collectors.toList()));
              }
              if (walletEntity.getAssets() != null) {
                List<MarketData> marketData =
                    marketDataService.getMarketData(user.getSettings().getCryptoCurrency());
                wallet.setAssets(
                    assetMapper.fromAssetEntitiesToAssets(
                        walletEntity.getAssets(), marketData, getAllCryptoDates));
              }
              if (live) setLivePriceInWallet(wallet, finalForex, lastBalance);
              return wallet;
            }))
        .collect(Collectors.toList());
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public List<Wallet> deleteWalletIds(List<Wallet> wallets) {
    return wallets.stream()
        .map(
            (walletToEdit -> {
              Wallet wallet = new Wallet();
              walletToEdit.setId(null);
              BeanUtils.copyProperties(walletToEdit, wallet);

              if (walletToEdit.getHistory() != null) {
                wallet.setHistory(
                    walletToEdit.getHistory().stream()
                        .map(
                            statsEntity -> {
                              Stats stats = new Stats();
                              statsEntity.setId(null);
                              BeanUtils.copyProperties(statsEntity, stats);
                              return stats;
                            })
                        .collect(Collectors.toList()));
              }
              if (walletToEdit.getAssets() != null) {
                wallet.setAssets(
                    walletToEdit.getAssets().stream()
                        .map(
                            assetMap -> {
                              Asset asset = new Asset();
                              assetMap.setId(null);
                              BeanUtils.copyProperties(assetMap, asset);
                              if (assetMap.getHistory() != null) {
                                asset.setHistory(
                                    assetMap.getHistory().stream()
                                        .map(
                                            statsEntity -> {
                                              Stats stats = new Stats();
                                              statsEntity.setId(null);
                                              BeanUtils.copyProperties(statsEntity, stats);
                                              return stats;
                                            })
                                        .collect(Collectors.toList()));
                              }
                              if (assetMap.getOperations() != null) {
                                asset.setOperations(
                                    assetMap.getOperations().stream()
                                        .map(
                                            operationEntity -> {
                                              Operations operations = new Operations();
                                              operationEntity.setId(null);
                                              BeanUtils.copyProperties(operationEntity, operations);
                                              return operations;
                                            })
                                        .collect(Collectors.toList()));
                              }
                              return asset;
                            })
                        .collect(Collectors.toList()));
              }
              return wallet;
            }))
        .collect(Collectors.toList());
  }

  private void setLivePriceInWallet(
      Wallet wallet, ForexData forex, AtomicReference<Double> lastBalance) {
    if (Utils.isNullOrEmpty(forex) || Utils.isNullOrEmpty(wallet.getAssets())) return;
    // if (user.getSettings().getCryptoCurrency().equalsIgnoreCase(user.getSettings().getCurrency())
    //    || !wallet.getCategory().equalsIgnoreCase("Crypto")) return;

    if (!wallet.getCategory().equalsIgnoreCase(Category.CRYPTO.getValue())) return;
    AtomicReference<Double> balance = new AtomicReference<>(0D);
    wallet.getAssets().forEach(asset -> balance.updateAndGet(v -> v + asset.getValue()));
    double converter = forex.getQuotes().get(user.getSettings().getCurrency());
    wallet.setBalance(MathService.round(balance.get() * converter, 2));
    wallet.setDifferenceLastStats(MathService.round(wallet.getBalance() - lastBalance.get(), 2));
    wallet.setPerformanceLastStats(
        wallet.getBalance() == 0 && lastBalance.get() == 0
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
}
