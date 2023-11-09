package com.giova.service.moneystats.app;

import com.giova.service.moneystats.app.model.Dashboard;
import com.giova.service.moneystats.app.stats.dto.Stats;
import com.giova.service.moneystats.app.wallet.dto.Wallet;
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
public class AppMapper {

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_MAPPER)
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

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_MAPPER)
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

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_MAPPER)
  public void updateLastBalance(
      List<Stats> listFilter,
      List<LocalDate> filterDateByYear,
      AtomicReference<Double> lastBalance) {
    Double lastBalanceFilter =
        listFilter.stream()
            .filter(
                lF ->
                    lF.getDate()
                        .isEqual(
                            filterDateByYear.get(
                                filterDateByYear.size() > 1 ? filterDateByYear.size() - 2 : 0)))
            .findFirst()
            .map(Stats::getBalance)
            .orElse(0.0);
    lastBalance.updateAndGet(v -> v + lastBalanceFilter);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_MAPPER)
  public void mapDashboardBalanceAndPerformance(
      Dashboard dashboard,
      AtomicReference<Double> balance,
      AtomicReference<Double> lastBalance,
      AtomicReference<Double> initialBalance)
      throws UtilsException {
    dashboard.setPerformanceValue(MathService.round(balance.get() - initialBalance.get(), 2));
    dashboard.setLastStatsBalanceDifference(
        MathService.round(balance.get() - lastBalance.get(), 2));
    dashboard.setBalance(MathService.round(balance.get(), 2));
    dashboard.setPerformance(
        balance.get() == 0 && initialBalance.get() == 0
            ? 0D
            : MathService.round(
                ((balance.get() - initialBalance.get()) / initialBalance.get()) * 100, 2));
    dashboard.setLastStatsPerformance(
        balance.get() == 0 && lastBalance.get() == 0
            ? 0D
            : MathService.round(
                ((balance.get() - lastBalance.get()) / lastBalance.get()) * 100, 2));
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_MAPPER)
  public void mapWalletInThePast(Wallet wallet) throws UtilsException, RuntimeException {
    AtomicReference<Double> balance = new AtomicReference<>(0D);
    AtomicReference<Double> initialBalance = new AtomicReference<>(0D);
    AtomicReference<Double> lastBalance = new AtomicReference<>(0.00001D);
    Stats highPrice =
        wallet.getHistory().stream()
            .max(Comparator.comparing(Stats::getBalance))
            .orElseThrow(UtilsException::new);
    Stats lowPrice =
        wallet.getHistory().stream()
            .min(Comparator.comparing(Stats::getBalance))
            .orElseThrow(UtilsException::new);
    List<Stats> getStats = wallet.getHistory();
    wallet.setHighPrice(highPrice.getBalance());
    wallet.setHighPriceDate(highPrice.getDate());
    wallet.setLowPrice(lowPrice.getBalance());
    wallet.setLowPriceDate(lowPrice.getDate());

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
}
