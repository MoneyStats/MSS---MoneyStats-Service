package com.giova.service.moneystats.crypto;

import com.giova.service.moneystats.app.stats.dto.Stats;
import com.giova.service.moneystats.app.wallet.dto.Wallet;
import com.giova.service.moneystats.crypto.asset.dto.Asset;
import com.giova.service.moneystats.crypto.model.CryptoDashboard;
import com.giova.service.moneystats.crypto.model.DashboardInfo;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.math.MathService;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CryptoMapper {

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_MAPPER)
  public void updateBalance(
      List<Stats> listFilter, List<LocalDate> filterDateByYear, AtomicReference<Double> balance) {
    Double balanceFilter =
        listFilter.stream()
                    .filter(
                        lF ->
                            lF.getDate().isEqual(filterDateByYear.get(filterDateByYear.size() - 1)))
                    .collect(Collectors.toList())
                    .size()
                != 0
            ? listFilter.stream()
                .filter(
                    lF -> lF.getDate().isEqual(filterDateByYear.get(filterDateByYear.size() - 1)))
                .collect(Collectors.toList())
                .get(0)
                .getBalance()
            : 0;
    balance.updateAndGet(v -> v + balanceFilter);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_MAPPER)
  public void updateInitialBalance(
      List<Stats> listFilter,
      List<LocalDate> filterDateByYear,
      AtomicReference<Double> initialBalance) {
    Double initialBalanceFilter =
        listFilter.stream()
                    .filter(lF -> lF.getDate().isEqual(filterDateByYear.get(0)))
                    .collect(Collectors.toList())
                    .size()
                != 0
            ? listFilter.stream()
                .filter(lF -> lF.getDate().isEqual(filterDateByYear.get(0)))
                .collect(Collectors.toList())
                .get(0)
                .getBalance()
            : 0;
    initialBalance.updateAndGet(v -> v + initialBalanceFilter);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_MAPPER)
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
                                        filterDateByYear.size() > 1
                                            ? filterDateByYear.size() - 2
                                            : 0)))
                    .collect(Collectors.toList())
                    .size()
                != 0
            ? listFilter.stream()
                .filter(
                    lF ->
                        lF.getDate()
                            .isEqual(
                                filterDateByYear.get(
                                    filterDateByYear.size() > 1 ? filterDateByYear.size() - 2 : 0)))
                .collect(Collectors.toList())
                .get(0)
                .getBalance()
            : 0;
    lastBalance.updateAndGet(v -> v + lastBalanceFilter);
    if (isHolding) holdingLastBalance.updateAndGet(v -> v + lastBalanceFilter);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_MAPPER)
  public void mapWalletInThePast(Wallet wallet) throws UtilsException, RuntimeException {
    AtomicReference<Double> balance = new AtomicReference<>(0D);
    AtomicReference<Double> lastBalance = new AtomicReference<>(0.00001D);
    //Stats highPrice =
    //    wallet.getHistory().stream()
    //        .max(Comparator.comparing(Stats::getBalance))
    //        .orElseThrow(UtilsException::new);
    //Stats lowPrice =
    //    wallet.getHistory().stream()
    //        .min(Comparator.comparing(Stats::getBalance))
    //        .orElseThrow(UtilsException::new);
    List<Stats> getStats = wallet.getHistory();
    //wallet.setHighPrice(highPrice.getBalance());
    //wallet.setHighPriceDate(highPrice.getDate());
    //wallet.setLowPrice(lowPrice.getBalance());
    //wallet.setLowPriceDate(lowPrice.getDate());

    balance.updateAndGet(
        v -> v + getStats.get(getStats.size() > 1 ? getStats.size() - 1 : 0).getBalance());
    lastBalance.updateAndGet(
        v -> v + getStats.get(getStats.size() > 1 ? getStats.size() - 2 : 0).getBalance());
    wallet.setDateLastStats(getStats.get(getStats.size() - 1).getDate());
    wallet.setDifferenceLastStats(MathService.round(balance.get() - lastBalance.get(), 2));
    wallet.setBalance(MathService.round(balance.get(), 2));

    wallet.setPerformanceLastStats(
        MathService.round(((balance.get() - lastBalance.get()) / lastBalance.get()) * 100, 2));
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_MAPPER)
  public void mapDashboardBalanceAndPerformance(
      CryptoDashboard dashboard,
      AtomicReference<Double> balance,
      AtomicReference<Double> lastBalance,
      AtomicReference<Double> holdingBalance,
      AtomicReference<Double> holdingLastBalance,
      Double BTC_VALUE)
      throws UtilsException {
    dashboard.setBalance(MathService.round(balance.get(), 2));
    dashboard.setBtcBalance(MathService.round(balance.get() / BTC_VALUE, 8));
    DashboardInfo performance = new DashboardInfo();
    performance.setPerformance(
        balance.get() == 0 && lastBalance.get() == 0
            ? 0D
            : MathService.round(
                ((balance.get() - lastBalance.get()) / lastBalance.get()) * 100, 2));
    performance.setBalance(MathService.round(balance.get() - lastBalance.get(), 2));
    performance.setLastUpdate(dashboard.getLastUpdate());
    dashboard.setPerformance(performance);

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
    trading.setPerformance(0D);
    trading.setBalance(0D);
    trading.setLastUpdate(dashboard.getLastUpdate());
    dashboard.setTrading(trading);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_MAPPER)
  public CryptoDashboard mapCryptoDashboardWithoutStats(
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
                          //asset.setValue(asset.getBalance() * BTC_VALUE);
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
