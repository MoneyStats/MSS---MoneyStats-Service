package com.giova.service.moneystats.crypto;

import com.giova.service.moneystats.app.stats.dto.Stats;
import com.giova.service.moneystats.app.wallet.dto.Wallet;
import com.giova.service.moneystats.crypto.asset.dto.Asset;
import com.giova.service.moneystats.crypto.coinGecko.dto.MarketData;
import com.giova.service.moneystats.crypto.model.CryptoDashboard;
import com.giova.service.moneystats.crypto.model.DashboardInfo;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.math.MathService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CryptoMapper {

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public void updateBalance(
      List<Stats> listFilter, List<LocalDate> filterDateByYear, AtomicReference<Double> balance) {
    Double balanceFilter =
        listFilter.stream()
            .filter(lF -> lF.getDate().isEqual(filterDateByYear.get(filterDateByYear.size() - 1)))
            .findFirst()
            .map(Stats::getBalance)
            .orElse(0.0);
    balance.updateAndGet(v -> v + balanceFilter);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public void updateInitialBalance(
      List<Stats> listFilter,
      List<LocalDate> filterDateByYear,
      AtomicReference<Double> initialBalance) {
    Double initialBalanceFilter =
        listFilter.stream()
            .filter(lF -> lF.getDate().isEqual(filterDateByYear.get(0)))
            .findFirst()
            .map(Stats::getBalance)
            .orElse(0.0);
    initialBalance.updateAndGet(v -> v + initialBalanceFilter);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public void updateLastBalance(
      List<Stats> listFilter,
      List<LocalDate> filterDateByYear,
      AtomicReference<Double> lastBalance,
      AtomicReference<Double> holdingLastBalance,
      Boolean isHolding) {
    Double lastBalanceFilter =
        listFilter.stream()
            .filter(
                lF ->
                    lF.getDate()
                        .isEqual(
                            filterDateByYear.get(
                                filterDateByYear.size() > 1 ? filterDateByYear.size() - 1 : 0)))
            .findFirst()
            .map(Stats::getBalance)
            .orElse(0.0);
    lastBalance.updateAndGet(v -> v + lastBalanceFilter);
    if (isHolding) holdingLastBalance.updateAndGet(v -> v + lastBalanceFilter);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public void mapWalletInThePast(Wallet wallet) throws RuntimeException {
    // AtomicReference<Double> balance = new AtomicReference<>(0D);
    // AtomicReference<Double> lastBalance = new AtomicReference<>(0.00001D);

    List<Stats> getStats = wallet.getHistory();

    Double balance = getStats.isEmpty() ? 0.00001 : getStats.get(getStats.size() - 1).getBalance();
    Double lastBalance =
        getStats.size() > 1 ? getStats.get(getStats.size() - 2).getBalance() : 0.00001;

    // balance.updateAndGet(
    //    v -> v + getStats.get(getStats.size() > 1 ? getStats.size() - 1 : 0).getBalance());
    // lastBalance.updateAndGet(
    //    v -> v + getStats.get(getStats.size() > 1 ? getStats.size() - 2 : 0).getBalance());
    wallet.setDateLastStats(getStats.get(getStats.size() - 1).getDate());
    // wallet.setDifferenceLastStats(MathService.round(balance.get() - lastBalance.get(), 2));
    // wallet.setBalance(MathService.round(balance.get(), 2));
    wallet.setDifferenceLastStats(MathService.round(balance - lastBalance, 2));
    wallet.setBalance(MathService.round(balance, 2));

    // wallet.setPerformanceLastStats(
    //    MathService.round(((balance.get() - lastBalance.get()) / lastBalance.get()) * 100, 2));
    wallet.setPerformanceLastStats(
        MathService.round(((balance - lastBalance) / lastBalance) * 100, 2));
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public void mapDashboardBalanceAndPerformance(
      CryptoDashboard dashboard,
      AtomicReference<Double> balance,
      AtomicReference<Double> holdingBalance,
      AtomicReference<Double> holdingLastBalance,
      AtomicReference<Double> tradingBalance,
      AtomicReference<Double> tradingLastBalance,
      Double BTC_VALUE,
      List<MarketData> marketData)
      throws UtilsException {
    dashboard.setBalance(MathService.round(balance.get(), 2));
    dashboard.setBtcBalance(MathService.round(balance.get() / BTC_VALUE, 8));

    DashboardInfo holding = new DashboardInfo();
    System.out.println(holdingLastBalance);
    System.out.println(holdingBalance);
    holding.setPerformance(
        (holdingBalance.get() == 0 || holdingLastBalance.get() == 0
            ? 0D
            : MathService.round(
                ((holdingBalance.get() - holdingLastBalance.get()) / holdingLastBalance.get())
                    * 100,
                2)));
    holding.setBalance(MathService.round(holdingBalance.get(), 2));
    holding.setLastUpdate(dashboard.getLastUpdate());
    dashboard.setHoldingLong(holding);
    DashboardInfo trading = new DashboardInfo();
    trading.setPerformance(tradingLastBalance.get());
    trading.setBalance(MathService.round(tradingBalance.get(), 2));
    // trading.setPerformance(0D);
    // trading.setBalance(0D);
    trading.setLastUpdate(dashboard.getLastUpdate());
    dashboard.setTrading(trading);
    dashboard.setLastUpdate(
        marketData.stream()
            .findFirst()
            .map(MarketData::getUpdateDate)
            .orElse(LocalDateTime.now())
            .toLocalDate());
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public CryptoDashboard mapCryptoDashboardWithoutStats(
      String currency, List<Wallet> getAllWallet, List<Asset> getAllAsset, Double BTC_VALUE)
      throws UtilsException {
    CryptoDashboard dashboard = new CryptoDashboard();
    dashboard.setLastUpdate(LocalDate.now());
    dashboard.setCurrency(currency);
    Double balance = 0.0;
    Double holdingBalance = 0.0;
    Double lastBalance = 0.0;
    for (Wallet wallet : getAllWallet) {
      wallet.setHistory(null);
      if (wallet.getAssets() != null && !wallet.getAssets().isEmpty()) {
        for (Asset asset : wallet.getAssets()) {
          balance += asset.getValue();
          if (wallet.getType().equalsIgnoreCase("Holding")) {
            holdingBalance += asset.getValue();
          }
        }
      }
    }
    dashboard.setBalance(MathService.round(balance, 2));
    dashboard.setBtcBalance(MathService.round(balance / BTC_VALUE, 8));
    DashboardInfo performance = new DashboardInfo();
    performance.setPerformance(0.0);
    performance.setBalance(0.0);
    performance.setLastUpdate(dashboard.getLastUpdate());
    dashboard.setPerformance(performance);
    DashboardInfo holding = new DashboardInfo();
    holding.setPerformance(
        (holdingBalance == 0 || lastBalance == 0
            ? 0.0
            : MathService.round(((holdingBalance - lastBalance) / lastBalance) * 100, 2)));
    holding.setBalance(MathService.round(holdingBalance, 2));
    holding.setLastUpdate(dashboard.getLastUpdate());
    dashboard.setHoldingLong(holding);
    DashboardInfo trading = new DashboardInfo();
    trading.setPerformance(0.0);
    trading.setBalance(0.0);
    trading.setLastUpdate(dashboard.getLastUpdate());
    dashboard.setTrading(trading);
    dashboard.setWallets(getAllWallet);
    dashboard.setAssets(getAllAsset);
    return dashboard;
  }

  @Deprecated
  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public CryptoDashboard mapCryptoDashboardWithoutStats_OLD(
      String currency, List<Wallet> getAllWallet, List<Asset> getAllAsset, Double BTC_VALUE)
      throws UtilsException {
    CryptoDashboard dashboard = new CryptoDashboard();
    dashboard.setLastUpdate(LocalDate.now());
    dashboard.setCurrency(currency);
    AtomicReference<Double> balance = new AtomicReference<>(0D);
    AtomicReference<Double> holdingBalance = new AtomicReference<>(0D);
    getAllWallet.stream()
        .peek(
            wallet -> {
              wallet.setHistory(null);
              if (wallet.getAssets() != null && !wallet.getAssets().isEmpty())
                wallet.getAssets().stream()
                    .peek(
                        asset -> {
                          // TODO: Modica 2 con Asset Price anche giù nel mapDashboard
                          // asset.setValue(asset.getBalance() * BTC_VALUE);
                          balance.updateAndGet(v -> v + asset.getValue());
                          if (wallet.getType().equalsIgnoreCase("Holding"))
                            holdingBalance.updateAndGet(v -> v + asset.getValue());
                        })
                    .collect(Collectors.toList());
            })
        .collect(Collectors.toList());
    dashboard.setBalance(MathService.round(balance.get(), 2));
    dashboard.setBtcBalance(MathService.round(balance.get() / BTC_VALUE, 8));
    DashboardInfo performance = new DashboardInfo();
    performance.setPerformance(0D);
    performance.setBalance(0D);
    performance.setLastUpdate(dashboard.getLastUpdate());
    dashboard.setPerformance(performance);

    DashboardInfo holding = new DashboardInfo();
    holding.setPerformance(1000D);
    holding.setBalance(MathService.round(holdingBalance.get(), 2));
    holding.setLastUpdate(dashboard.getLastUpdate());
    dashboard.setHoldingLong(holding);

    DashboardInfo trading = new DashboardInfo();
    trading.setPerformance(0D);
    trading.setBalance(0D);
    trading.setLastUpdate(dashboard.getLastUpdate());
    dashboard.setTrading(trading);
    dashboard.setWallets(getAllWallet);
    dashboard.setAssets(getAllAsset);
    return dashboard;
  }
}
