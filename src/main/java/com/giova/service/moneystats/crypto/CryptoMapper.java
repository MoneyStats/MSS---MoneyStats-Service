package com.giova.service.moneystats.crypto;

import com.giova.service.moneystats.app.stats.dto.Stats;
import com.giova.service.moneystats.app.wallet.dto.Wallet;
import com.giova.service.moneystats.crypto.asset.dto.Asset;
import com.giova.service.moneystats.crypto.marketData.dto.MarketData;
import com.giova.service.moneystats.crypto.model.CryptoDashboard;
import com.giova.service.moneystats.crypto.model.DashboardInfo;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.math.MathService;
import io.github.giovannilamarmora.utils.utilities.ObjectToolkit;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Component;

@Component
public class CryptoMapper {

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public static void updateBalance(
      List<Stats> listFilter, List<LocalDate> filterDateByYear, AtomicReference<Double> balance) {
    Double balanceFilter =
        listFilter.stream()
            .filter(lF -> lF.getDate().isEqual(filterDateByYear.getLast()))
            .findFirst()
            .map(Stats::getBalance)
            .orElse(0.0);
    balance.updateAndGet(v -> v + balanceFilter);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public static void updateInitialBalance(
      List<Stats> listFilter,
      List<LocalDate> filterDateByYear,
      AtomicReference<Double> initialBalance) {
    Double initialBalanceFilter =
        listFilter.stream()
            .filter(lF -> lF.getDate().isEqual(filterDateByYear.getFirst()))
            .findFirst()
            .map(Stats::getBalance)
            .orElse(0.0);
    initialBalance.updateAndGet(v -> v + initialBalanceFilter);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public static void updateLastBalance(
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
  public static void mapWalletInThePast(Wallet wallet) {
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
  public static void mapDashboardBalanceAndPerformance(
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
    trading.setPerformance(
        (tradingBalance.get() == 0 || tradingLastBalance.get() == 0
            ? 0D
            : MathService.round(
                ((tradingBalance.get() - tradingLastBalance.get()) / tradingLastBalance.get())
                    * 100,
                2)));
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
  public static CryptoDashboard mapCryptoDashboardWithoutStats(
      String currency, List<Wallet> getAllWallet, List<Asset> getAllAsset, Double BTC_VALUE) {
    CryptoDashboard dashboard = new CryptoDashboard();
    dashboard.setLastUpdate(LocalDate.now());
    dashboard.setCurrency(currency);
    Double balance = 0.0;
    Double holdingBalance = 0.0;
    Double lastBalance = 0.0;
    for (Wallet wallet : getAllWallet) {
      wallet.setHistory(null);
      if (!ObjectToolkit.isNullOrEmpty(wallet.getAssets())) {
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
}
