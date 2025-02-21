package com.giova.service.moneystats.crypto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.giova.service.moneystats.app.stats.StatsComponent;
import com.giova.service.moneystats.app.stats.dto.Stats;
import com.giova.service.moneystats.app.wallet.WalletService;
import com.giova.service.moneystats.app.wallet.dto.Wallet;
import com.giova.service.moneystats.authentication.dto.UserData;
import com.giova.service.moneystats.crypto.asset.AssetMapper;
import com.giova.service.moneystats.crypto.asset.AssetService;
import com.giova.service.moneystats.crypto.asset.dto.Asset;
import com.giova.service.moneystats.crypto.marketData.MarketDataService;
import com.giova.service.moneystats.crypto.marketData.dto.MarketData;
import com.giova.service.moneystats.crypto.model.CryptoDashboard;
import com.giova.service.moneystats.crypto.operations.dto.Operations;
import io.github.giovannilamarmora.utils.context.TraceUtils;
import io.github.giovannilamarmora.utils.generic.Response;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.github.giovannilamarmora.utils.math.MathService;
import io.github.giovannilamarmora.utils.utilities.Mapper;
import io.github.giovannilamarmora.utils.utilities.ObjectToolkit;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Logged
@Service
@AllArgsConstructor
public class CryptoService {

  private static final String BTC_SYMBOL = "BTC";
  private final UserData user;
  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  @Autowired private WalletService walletService;
  @Autowired private AssetService assetService;
  @Autowired private StatsComponent statsComponent;
  @Autowired private MarketDataService marketDataService;

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public ResponseEntity<Response> getCryptoDashboardData() {
    List<MarketData> marketData =
        marketDataService.getMarketData(user.getSettings().getCryptoCurrency());

    List<LocalDate> getAllDates = statsComponent.getCryptoDistinctDates(user);
    List<LocalDate> filter;
    CryptoDashboard dashboard = new CryptoDashboard();
    if (!getAllDates.isEmpty()) {
      int currentYear = getAllDates.getLast().getYear();
      filter = getAllDates.stream().filter(d -> d.getYear() == currentYear).toList();

      Map<String, CryptoDashboard> getData = mapDashBoard(filter, marketData, false);
      if (!ObjectToolkit.isNullOrEmpty(getData)) {
        dashboard = getData.get(String.valueOf(currentYear));
      }
    } else {
      List<Wallet> getAllWallet = new ArrayList<>();
      ResponseEntity<Response> responseEntityWallet = walletService.getCryptoWallets(false);
      if (!ObjectToolkit.isNullOrEmpty(responseEntityWallet.getBody())
          & !ObjectToolkit.isNullOrEmpty(responseEntityWallet.getBody().getData()))
        getAllWallet =
            Mapper.convertObject(
                responseEntityWallet.getBody().getData(), new TypeReference<List<Wallet>>() {});
      dashboard =
          CryptoMapper.mapCryptoDashboardWithoutStats(
              user.getSettings().getCryptoCurrency(),
              getAllWallet,
              getCryptoAssetsList(false, null, getAllWallet, marketData, null),
              getAssetValue(marketData, BTC_SYMBOL));
    }

    dashboard.setWallets(null);

    String message = "Data for Crypto Dashboard!";

    Response response =
        new Response(HttpStatus.OK.value(), message, TraceUtils.getSpanID(), dashboard);
    return ResponseEntity.ok(response);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public ResponseEntity<Response> getCryptoResumeData(Long year) {
    List<MarketData> marketData =
        marketDataService.getMarketData(user.getSettings().getCryptoCurrency());
    List<LocalDate> getAllDates = statsComponent.getCryptoDistinctDates(user);
    Map<String, CryptoDashboard> getData = new HashMap<>();
    CryptoDashboard dashboard = new CryptoDashboard();
    int thisYear = LocalDate.now().getYear();
    if (!getAllDates.isEmpty()) {
      List<LocalDate> filterDateByYear =
          getAllDates.stream().filter(localDate -> localDate.getYear() == year).toList();
      Map<String, CryptoDashboard> getDataProvision =
          mapDashBoard(filterDateByYear, marketData, true);
      if (!ObjectToolkit.isNullOrEmpty(getDataProvision)) {
        dashboard = getDataProvision.get(String.valueOf(year));
      }
      dashboard.setYearsWalletStats(
          getAllDates.stream()
              .map(LocalDate::getYear)
              .distinct()
              .sorted(Collections.reverseOrder())
              .toList());
      getData.put(String.valueOf(year), dashboard);
    } else {
      List<Wallet> getAllWallet = new ArrayList<>();
      ResponseEntity<Response> responseEntityWallet = walletService.getCryptoWallets(false);
      if (!ObjectToolkit.isNullOrEmpty(responseEntityWallet.getBody())
          & !ObjectToolkit.isNullOrEmpty(responseEntityWallet.getBody().getData()))
        getAllWallet =
            Mapper.convertObject(
                responseEntityWallet.getBody().getData(), new TypeReference<List<Wallet>>() {});
      dashboard =
          CryptoMapper.mapCryptoDashboardWithoutStats(
              user.getSettings().getCryptoCurrency(),
              getAllWallet,
              getCryptoAssetsList(false, null, getAllWallet, marketData, null),
              getAssetValue(marketData, BTC_SYMBOL));
      getData.put(String.valueOf(thisYear), dashboard);
    }

    String message = "Data for Crypto Resume!";

    Response response =
        new Response(HttpStatus.OK.value(), message, TraceUtils.getSpanID(), getData);
    return ResponseEntity.ok(response);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public ResponseEntity<Response> getCryptoHistoryData() {
    LOG.info("Starting getting history data crypto for user {}", user.getUsername());

    Map<String, CryptoDashboard> dashboardHashMap = new LinkedHashMap<>();
    List<LocalDate> getAllDates = statsComponent.getCryptoDistinctDates(user);
    List<Integer> distinctYears =
        new ArrayList<>(
            getAllDates.stream()
                .map(LocalDate::getYear)
                .distinct()
                .sorted(Collections.reverseOrder())
                .toList());

    LocalDate today = LocalDate.now();
    if (!distinctYears.contains(today.getYear())) distinctYears.add(today.getYear());
    distinctYears.sort(Collections.reverseOrder());

    ResponseEntity<Response> responseAssets = assetService.getAssets(false);
    List<Asset> assets =
        Optional.ofNullable(responseAssets.getBody())
            .map(Response::getData)
            .map(data -> Mapper.convertObject(data, new TypeReference<List<Asset>>() {}))
            .orElse(Collections.emptyList());

    AtomicInteger yearIndex = new AtomicInteger(0);

    distinctYears.forEach(
        year -> {
          List<LocalDate> datesByYear =
              getAllDates.stream().filter(d -> d.getYear() == year).toList();
          CryptoDashboard dashboard = new CryptoDashboard();
          AtomicReference<Double> balance = new AtomicReference<>(0D);

          assets.forEach(
              asset -> {
                List<Stats> yearlyStats =
                    Optional.ofNullable(asset.getHistory())
                        .map(
                            history ->
                                history.stream()
                                    .filter(h -> h.getDate().getYear() == year)
                                    .toList())
                        .orElse(Collections.emptyList());

                if (yearIndex.get() == 0) {
                  balance.updateAndGet(b -> b + asset.getValue());
                }

                if (!yearlyStats.isEmpty()) {
                  if (yearIndex.get() != 0) {
                    CryptoMapper.updateBalance(
                        List.of(yearlyStats.getLast()), datesByYear, balance);
                  }
                }
              });

          dashboard.setBalance(MathService.round(balance.get(), 2));
          dashboardHashMap.put(String.valueOf(year), dashboard);
          yearIndex.incrementAndGet();
        });

    String message = "Data for History!";
    Response response =
        new Response(HttpStatus.OK.value(), message, TraceUtils.getSpanID(), dashboardHashMap);
    return ResponseEntity.ok(response);
  }

  private Map<String, CryptoDashboard> mapDashBoard(
      List<LocalDate> dates, List<MarketData> marketData, Boolean isResume) {
    Map<String, CryptoDashboard> response = new HashMap<>();

    List<Integer> distinctDatesByYear = dates.stream().map(LocalDate::getYear).distinct().toList();

    List<Wallet> getAllWallet = new ArrayList<>();
    ResponseEntity<Response> responseEntityWallet = walletService.getCryptoWallets(true);
    if (!ObjectToolkit.isNullOrEmpty(responseEntityWallet.getBody())
        & !ObjectToolkit.isNullOrEmpty(responseEntityWallet.getBody().getData()))
      getAllWallet =
          Mapper.convertObject(
              responseEntityWallet.getBody().getData(), new TypeReference<List<Wallet>>() {});

    AtomicInteger index = new AtomicInteger(0);
    List<Wallet> finalGetAllWallet = getAllWallet;
    distinctDatesByYear.stream()
        .sorted(Collections.reverseOrder())
        .forEach(
            year -> {
              LOG.info("Mapping Data for year {}", year);
              // Filtro le date secondo l'anno
              List<LocalDate> filterDateByYear =
                  dates.stream().filter(d -> d.getYear() == year).toList();
              CryptoDashboard dashboard = new CryptoDashboard();
              dashboard.setLastUpdate(filterDateByYear.getLast());
              dashboard.setStatsAssetsDays(filterDateByYear);
              dashboard.setCurrency(user.getSettings().getCryptoCurrency());
              dashboard.setPerformanceSince(filterDateByYear.getFirst());

              AtomicReference<Double> balance = new AtomicReference<>(0D);
              AtomicReference<Double> initialBalance = new AtomicReference<>(0D);
              AtomicReference<Double> lastBalance = new AtomicReference<>(0D);
              AtomicReference<Double> holdingBalance = new AtomicReference<>(0D);
              AtomicReference<Double> holdingLastBalance = new AtomicReference<>(0D);
              AtomicReference<Double> tradingBalance = new AtomicReference<>(0D);
              AtomicReference<Double> tradingLastBalance = new AtomicReference<>(0D);

              AtomicInteger indexWallet = new AtomicInteger(0);

              List<Wallet> filterWallet =
                  new ArrayList<>(
                      filterHistoryWallet(
                          finalGetAllWallet,
                          balance,
                          holdingBalance,
                          initialBalance,
                          lastBalance,
                          holdingLastBalance,
                          index,
                          indexWallet,
                          filterDateByYear,
                          year,
                          tradingBalance,
                          tradingLastBalance));

              // dashboard.setAssets(getCryptoAsset(filterWallet, marketData, isResume));
              dashboard.setAssets(
                  getCryptoAssetsList(isResume, year, filterWallet, marketData, dates));

              // Filtro Wallet cancellati da anni che non hanno stats
              Predicate<Wallet> walletRemovedInThePast =
                  wallet -> !ObjectToolkit.isNullOrEmpty(wallet.getDeletedDate());
              filterWallet.removeIf(walletRemovedInThePast);

              // Mi serve per mappare il passato
              if (index.get() > 0) {
                // Remove wallet that haven't any Asset stats
                Predicate<Wallet> hasEmptyStats =
                    wallet ->
                        wallet.getAssets() != null
                            && wallet.getAssets().stream()
                                .filter(asset -> !ObjectToolkit.isNullOrEmpty(asset.getHistory()))
                                .toList()
                                .isEmpty();
                filterWallet.removeIf(hasEmptyStats);
              }

              dashboard.setWallets(filterWallet);
              CryptoMapper.mapDashboardBalanceAndPerformance(
                  dashboard,
                  balance,
                  holdingBalance,
                  holdingLastBalance,
                  tradingBalance,
                  tradingLastBalance,
                  getAssetValue(marketData, BTC_SYMBOL),
                  marketData);
              index.incrementAndGet();
              response.put(String.valueOf(year), dashboard);
            });

    return response;
  }

  private List<Wallet> filterHistoryWallet(
      List<Wallet> getAllWallet,
      AtomicReference<Double> balance,
      AtomicReference<Double> holdingBalance,
      AtomicReference<Double> initialBalance,
      AtomicReference<Double> lastBalance,
      AtomicReference<Double> holdingLastBalance,
      AtomicInteger index,
      AtomicInteger indexWallet,
      List<LocalDate> filterDateByYear,
      Integer year,
      AtomicReference<Double> tradingBalance,
      AtomicReference<Double> tradingLastBalance) {
    List<Operations> operations = new ArrayList<>();
    return getAllWallet.stream()
        .map(
            wallet -> {
              Wallet wallet1 = new Wallet();
              BeanUtils.copyProperties(wallet, wallet1);
              if (wallet.getHistory() != null) {
                Stats history = wallet.getHistory().getLast();
                wallet1.setHistory(List.of(history));
              }

              if (!ObjectToolkit.isNullOrEmpty(wallet.getAssets()))
                wallet1.setAssets(
                    wallet.getAssets().stream()
                        .map(
                            asset -> {
                              Asset asset1 = new Asset();
                              BeanUtils.copyProperties(asset, asset1);
                              List<Stats> listFilter = new ArrayList<>();
                              if (!ObjectToolkit.isNullOrEmpty(asset.getHistory())) {
                                listFilter =
                                    asset.getHistory().stream()
                                        .filter(h -> h.getDate().getYear() == year)
                                        .toList();

                                asset1.setHistory(listFilter);
                              }
                              // asset1.setValue(asset.getInvested() * BTC_VALUE);

                              if (wallet.getType().equalsIgnoreCase("Holding"))
                                holdingBalance.updateAndGet(v -> v + asset1.getValue());

                              if (wallet.getType().equalsIgnoreCase("Trading")) {
                                if (asset.getOperations() != null)
                                  operations.addAll(asset.getOperations());
                                tradingBalance.updateAndGet(v -> v + asset1.getValue());
                                tradingLastBalance.updateAndGet(v -> v + asset1.getInvested());
                              }
                              if (index.get() == 0)
                                balance.updateAndGet(v -> v + asset1.getValue());
                              if (!listFilter.isEmpty()) {
                                if (index.get() != 0)
                                  CryptoMapper.updateBalance(listFilter, filterDateByYear, balance);
                                // if (index.get() == 0)
                                //  balance.updateAndGet(v -> v + asset1.getValue());
                                // else
                                //  cryptoMapper.updateBalance(listFilter, filterDateByYear,
                                // balance);
                                CryptoMapper.updateInitialBalance(
                                    listFilter, filterDateByYear, initialBalance);

                                CryptoMapper.updateLastBalance(
                                    listFilter,
                                    filterDateByYear,
                                    lastBalance,
                                    holdingLastBalance,
                                    wallet.getType().equalsIgnoreCase("Holding"));
                              }

                              checkAndMapWalletInThePast(
                                  index, listFilter, filterDateByYear, wallet1);

                              if (!ObjectToolkit.isNullOrEmpty(asset1.getOperations())) {
                                asset1.setOperations(
                                    asset1.getOperations().stream()
                                        .filter(
                                            o -> {
                                              boolean isExitDateNull =
                                                  ObjectToolkit.isNullOrEmpty(o.getExitDate());
                                              if (isExitDateNull) {
                                                return o.getEntryDate().getYear() <= year;
                                              } else {
                                                return o.getExitDate().getYear() == year;
                                              }
                                            })
                                        .toList());
                              }
                              return asset1;

                              //  if (!ObjectToolkit.isNullOrEmpty(asset1.getOperations()))
                              //  asset1.setOperations(
                              //      new ArrayList<>(asset1.getOperations())
                              //          .stream()
                              //              .filter(
                              //                  o ->
                              //                      //
                              // (ObjectToolkit.isNullOrEmpty(o.getExitDate())
                              //                      //        && o.getEntryDate().getYear() <=
                              // year)
                              //                      //    || o.getEntryDate().getYear() == year)
                              //                      // Inverted Entry to Exit date
                              //                      (ObjectToolkit.isNullOrEmpty(o.getExitDate())
                              //                              && o.getExitDate().getYear() <= year)
                              //                          || o.getExitDate().getYear() == year)
                              //              .toList());
                              // return asset1;
                            })
                        .toList());
              if (wallet.getType().equalsIgnoreCase("Trading")) {
                Predicate<Operations> isNotTradingAndNotClosed =
                    operations1 ->
                        !operations1.getType().equalsIgnoreCase("Trading")
                            || operations1.getExitDate() == null;
                operations.removeIf(isNotTradingAndNotClosed);
                Comparator<Operations> c = Comparator.comparing(Operations::getEntryDate);
                operations.sort(c);

                tradingLastBalance.updateAndGet(
                    v ->
                        ObjectUtils.isEmpty(operations) ? v : v + operations.getFirst().getTrend());
              }
              Predicate<Asset> hasEmptyStats =
                  asset -> asset.getHistory() == null || asset.getHistory().isEmpty();
              List<Asset> filterAsset =
                  !ObjectToolkit.isNullOrEmpty(wallet1.getAssets())
                      ? new ArrayList<>(wallet1.getAssets())
                      : null;
              if (filterAsset != null && index.get() > 0) {
                filterAsset.removeIf(hasEmptyStats);
                wallet1.setAssets(filterAsset);
              }
              indexWallet.incrementAndGet();
              return wallet1;
            })
        .toList();
  }

  private void checkAndMapWalletInThePast(
      AtomicInteger index,
      List<Stats> listFilter,
      List<LocalDate> filterDateByYear,
      Wallet wallet1) {
    // Mi serve per mappare il passato
    if (index.get() > 0 && !listFilter.isEmpty()) {
      // Se lo stats all'ultima posizione ha la stessa data
      // dell'ultima
      // data della lista dell'anno, il wallet non era ancora
      // cancellato
      if (listFilter.getLast().getDate().isEqual(filterDateByYear.getLast())) {
        wallet1.setDeletedDate(null);
      }
      CryptoMapper.mapWalletInThePast(wallet1);
    }
  }

  private List<Asset> getCryptoAssetsList(
      Boolean isResume,
      Integer year,
      List<Wallet> walletList,
      List<MarketData> marketData,
      List<LocalDate> getAllDates) {

    // Raccolgo tutti gli asset dai wallet
    List<Asset> assets =
        walletList.stream()
            .filter(wallet -> !ObjectToolkit.isNullOrEmpty(wallet.getAssets()))
            .flatMap(wallet -> wallet.getAssets().stream())
            .toList();

    // Mappa gli asset con marketData e getAllDates
    assets = AssetMapper.mapAssetList(assets, marketData, getAllDates);

    // Applica i filtri sugli asset
    List<Asset> filteredAssets =
        new ArrayList<>(
            assets.stream()
                .map(
                    asset -> {
                      Asset newAsset = new Asset();
                      BeanUtils.copyProperties(asset, newAsset);

                      if (asset.getHistory() != null && !asset.getHistory().isEmpty()) {
                        if (!isResume) {
                          // Se non è resume, mantieni solo l'ultima entry della history
                          Stats latestStats = asset.getHistory().getLast();
                          newAsset.setHistory(List.of(latestStats));
                        } else if (year != null) {
                          // Se è resume e un anno è specificato, filtra gli Stats per l'anno dato
                          List<Stats> filteredHistory =
                              asset.getHistory().stream()
                                  .filter(stats -> stats.getDate().getYear() == year)
                                  .toList();
                          newAsset.setHistory(filteredHistory);
                        }
                      }

                      return newAsset;
                    })
                .toList());

    // Se è resume, rimuovi gli asset senza history
    if (isResume) {
      filteredAssets.removeIf(asset -> ObjectToolkit.isNullOrEmpty(asset.getHistory()));
    }

    return filteredAssets;
  }

  private Double getAssetValue(List<MarketData> marketData, String symbol) {
    if (marketData.isEmpty() || symbol == null) {
      return 1D;
    } else {
      return MathService.round(
          marketData.stream()
              .filter(marketData1 -> marketData1.getSymbol().equalsIgnoreCase(symbol))
              .findFirst()
              .orElse(new MarketData())
              .getCurrent_price(),
          2);
    }
  }
}
