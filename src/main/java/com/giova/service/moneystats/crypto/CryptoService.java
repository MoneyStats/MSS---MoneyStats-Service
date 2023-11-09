package com.giova.service.moneystats.crypto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giova.service.moneystats.app.stats.StatsMapper;
import com.giova.service.moneystats.app.stats.StatsService;
import com.giova.service.moneystats.app.stats.dto.Stats;
import com.giova.service.moneystats.app.wallet.WalletService;
import com.giova.service.moneystats.app.wallet.dto.Wallet;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import com.giova.service.moneystats.crypto.asset.AssetMapper;
import com.giova.service.moneystats.crypto.asset.dto.Asset;
import com.giova.service.moneystats.crypto.coinGecko.MarketDataService;
import com.giova.service.moneystats.crypto.coinGecko.dto.MarketData;
import com.giova.service.moneystats.crypto.model.CryptoDashboard;
import com.giova.service.moneystats.crypto.operations.dto.Operations;
import com.giova.service.moneystats.generic.Response;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.github.giovannilamarmora.utils.interceptors.correlationID.CorrelationIdUtils;
import io.github.giovannilamarmora.utils.math.MathService;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Logged
@Service
@AllArgsConstructor
public class CryptoService {

  private static final String BTC_SYMBOL = "BTC";
  private final UserEntity user;
  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
  @Autowired private WalletService walletService;
  @Autowired private StatsService statsService;
  @Autowired private CryptoMapper cryptoMapper;
  @Autowired private AssetMapper assetMapper;
  @Autowired private StatsMapper statsMapper;
  @Autowired private MarketDataService marketDataService;

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public ResponseEntity<Response> getCryptoDashboardData() throws UtilsException {
    List<MarketData> marketData = marketDataService.getMarketData(user.getCryptoCurrency());

    List<LocalDate> getAllDates = statsService.getCryptoDistinctDates(user);
    List<LocalDate> filter = new ArrayList<>();
    Map<String, CryptoDashboard> getData = new HashMap<>();
    int thisYear = 0;
    if (!getAllDates.isEmpty()) {
      int currentYear = getAllDates.get(getAllDates.size() - 1).getYear();
      filter =
          getAllDates.stream().filter(d -> d.getYear() == currentYear).collect(Collectors.toList());

      thisYear = currentYear;
      getData = mapDashBoard(filter, marketData, false);
    } else {
      List<Wallet> getAllWallet =
          objectMapper.convertValue(
              walletService.getCryptoWallets(true).getBody().getData(),
              new TypeReference<List<Wallet>>() {});
      CryptoDashboard dashboard =
          cryptoMapper.mapCryptoDashboardWithoutStats(
              user.getCryptoCurrency(),
              getAllWallet,
              getCryptoAsset(getAllWallet, marketData, false),
              getAssetValue(marketData, BTC_SYMBOL));
      getData.put(String.valueOf(thisYear), dashboard);
    }

    String message = "Data for Crypto Dashboard!";

    Response response =
        new Response(
            HttpStatus.OK.value(),
            message,
            CorrelationIdUtils.getCorrelationId(),
            getData.get(String.valueOf(thisYear)));
    return ResponseEntity.ok(response);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public ResponseEntity<Response> getCryptoResumeData() throws UtilsException {
    List<MarketData> marketData = marketDataService.getMarketData(user.getCryptoCurrency());
    List<LocalDate> getAllDates = statsService.getCryptoDistinctDates(user);
    Map<String, CryptoDashboard> getData = new HashMap<>();
    int thisYear = LocalDate.now().getYear();
    if (!getAllDates.isEmpty()) {
      getData = mapDashBoard(getAllDates, marketData, true);
    } else {

      List<Wallet> getAllWallet =
          objectMapper.convertValue(
              walletService.getCryptoWallets(false).getBody().getData(),
              new TypeReference<List<Wallet>>() {});
      CryptoDashboard dashboard =
          cryptoMapper.mapCryptoDashboardWithoutStats(
              user.getCryptoCurrency(),
              getAllWallet,
              getCryptoAsset(getAllWallet, marketData, true),
              getAssetValue(marketData, BTC_SYMBOL));
      getData.put(String.valueOf(thisYear), dashboard);
    }

    String message = "Data for Crypto Resume!";

    Response response =
        new Response(
            HttpStatus.OK.value(), message, CorrelationIdUtils.getCorrelationId(), getData);
    return ResponseEntity.ok(response);
  }

  private Map<String, CryptoDashboard> mapDashBoard(
      List<LocalDate> dates, List<MarketData> marketData, Boolean isResume) throws UtilsException {
    Map<String, CryptoDashboard> response = new HashMap<>();

    List<Integer> distinctDatesByYear =
        dates.stream().map(LocalDate::getYear).distinct().collect(Collectors.toList());

    // Wallet List
    List<Wallet> getAllWallet =
        objectMapper.convertValue(
            walletService.getCryptoWallets(true).getBody().getData(),
            new TypeReference<List<Wallet>>() {});

    AtomicInteger index = new AtomicInteger(0);
    distinctDatesByYear.stream()
        .sorted(Collections.reverseOrder())
        .peek(
            year -> {
              LOG.info("Mapping Data for year {}", year);
              // Filtro le date secondo l'anno
              List<LocalDate> filterDateByYear =
                  dates.stream().filter(d -> d.getYear() == year).collect(Collectors.toList());
              CryptoDashboard dashboard = new CryptoDashboard();
              dashboard.setLastUpdate(filterDateByYear.get(filterDateByYear.size() - 1));
              dashboard.setStatsAssetsDays(filterDateByYear);
              dashboard.setCurrency(user.getCryptoCurrency());
              dashboard.setPerformanceSince(filterDateByYear.get(0));

              AtomicReference<Double> balance = new AtomicReference<>(0D);
              AtomicReference<Double> initialBalance = new AtomicReference<>(0D);
              AtomicReference<Double> lastBalance = new AtomicReference<>(0D);
              AtomicReference<Double> holdingBalance = new AtomicReference<>(0D);
              AtomicReference<Double> holdingLastBalance = new AtomicReference<>(0D);
              AtomicReference<Double> tradingBalance = new AtomicReference<>(0D);
              AtomicReference<Double> tradingLastBalance = new AtomicReference<>(0D);

              AtomicInteger indexWallet = new AtomicInteger(0);

              List<Wallet> filterWallet =
                  filterHistoryWallet(
                      getAllWallet,
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
                      tradingLastBalance);

              dashboard.setAssets(getCryptoAsset(filterWallet, marketData, isResume));

              // Filtro Wallet cancellati da anni che non hanno stats
              Predicate<Wallet> walletRemovedInThePast = wallet -> wallet.getDeletedDate() != null;
              filterWallet.removeIf(walletRemovedInThePast);

              // Mi serve per mappare il passato
              if (index.get() > 0) {
                // Remove wallet that haven't any Asset stats
                Predicate<Wallet> hasEmptyStats =
                    wallet ->
                        wallet.getAssets() != null
                            && wallet.getAssets().stream()
                                .filter(
                                    asset ->
                                        asset.getHistory() == null || asset.getHistory().isEmpty())
                                .collect(Collectors.toList())
                                .isEmpty();
                filterWallet.removeIf(hasEmptyStats);
              }

              try {
                dashboard.setWallets(filterWallet);
                cryptoMapper.mapDashboardBalanceAndPerformance(
                    dashboard,
                    balance,
                    lastBalance,
                    holdingBalance,
                    holdingLastBalance,
                    tradingBalance,
                    tradingLastBalance,
                    getAssetValue(marketData, BTC_SYMBOL),
                    marketData);
              } catch (UtilsException e) {
                throw new RuntimeException(e);
              }
              index.incrementAndGet();
              response.put(String.valueOf(year), dashboard);
            })
        .collect(Collectors.toList());

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
                Stats history = wallet.getHistory().get(wallet.getHistory().size() - 1);
                wallet1.setHistory(List.of(history));
              }

              if (wallet.getAssets() != null)
                wallet1.setAssets(
                    wallet.getAssets().stream()
                        .map(
                            asset -> {
                              Asset asset1 = new Asset();
                              BeanUtils.copyProperties(asset, asset1);
                              List<Stats> listFilter = new ArrayList<>();
                              if (asset.getHistory() != null) {
                                listFilter =
                                    asset.getHistory().stream()
                                        .filter(h -> h.getDate().getYear() == year)
                                        .collect(Collectors.toList());

                                asset1.setHistory(listFilter);
                              }
                              // asset1.setValue(asset.getInvested() * BTC_VALUE);

                              if (wallet.getType().equalsIgnoreCase("Holding"))
                                holdingBalance.updateAndGet(v -> v + asset1.getValue());

                              if (wallet.getType().equalsIgnoreCase("Trading")) {
                                if (asset.getOperations() != null)
                                  operations.addAll(asset.getOperations());
                                tradingBalance.updateAndGet(v -> v + asset1.getValue());
                              }
                              if (index.get() == 0)
                                balance.updateAndGet(v -> v + asset1.getValue());
                              if (!listFilter.isEmpty()) {
                                if (index.get() != 0)
                                  cryptoMapper.updateBalance(listFilter, filterDateByYear, balance);
                                // if (index.get() == 0)
                                //  balance.updateAndGet(v -> v + asset1.getValue());
                                // else
                                //  cryptoMapper.updateBalance(listFilter, filterDateByYear,
                                // balance);
                                cryptoMapper.updateInitialBalance(
                                    listFilter, filterDateByYear, initialBalance);

                                cryptoMapper.updateLastBalance(
                                    listFilter,
                                    filterDateByYear,
                                    lastBalance,
                                    holdingLastBalance,
                                    wallet.getType().equalsIgnoreCase("Holding"));
                              }

                              checkAndMapWalletInThePast(
                                  index, listFilter, filterDateByYear, wallet1);
                              return asset1;
                            })
                        .collect(Collectors.toList()));
              if (indexWallet.get() == getAllWallet.size() - 1
                  && wallet.getType().equalsIgnoreCase("Trading")) {
                Predicate<Operations> isNotTradingAndNotClosed =
                    operations1 ->
                        !operations1.getType().equalsIgnoreCase("Trading")
                            || operations1.getExitDate() == null;
                operations.removeIf(isNotTradingAndNotClosed);
                Comparator<Operations> c = Comparator.comparing(Operations::getEntryDate);
                operations.sort(c);

                tradingLastBalance.updateAndGet(v -> operations.get(0).getPerformance());
              }
              Predicate<Asset> hasEmptyStats =
                  asset -> asset.getHistory() == null || asset.getHistory().isEmpty();
              List<Asset> filterAsset = wallet1.getAssets();
              if (filterAsset != null && index.get() > 0) {
                filterAsset.removeIf(hasEmptyStats);
                wallet1.setAssets(filterAsset);
              }
              indexWallet.incrementAndGet();
              return wallet1;
            })
        .collect(Collectors.toList());
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
      if (listFilter
          .get(listFilter.size() - 1)
          .getDate()
          .isEqual(filterDateByYear.get(filterDateByYear.size() - 1))) {
        wallet1.setDeletedDate(null);
      }
      try {
        cryptoMapper.mapWalletInThePast(wallet1);
      } catch (UtilsException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private List<Asset> getCryptoAsset(
      List<Wallet> filterWallet, List<MarketData> marketData, Boolean isResume) {
    List<Asset> assets = new ArrayList<>();
    filterWallet.stream()
        .peek(
            wallet -> {
              if (wallet.getAssets() != null && !wallet.getAssets().isEmpty())
                assets.addAll(wallet.getAssets());
            })
        .collect(Collectors.toList());
    List<Asset> filterAsset =
        assets.stream()
            .map(
                asset -> {
                  Asset newAsset = new Asset();
                  BeanUtils.copyProperties(asset, newAsset);
                  if (asset.getHistory() != null && !asset.getHistory().isEmpty() && !isResume) {
                    Stats stats = asset.getHistory().get(asset.getHistory().size() - 1);
                    newAsset.setHistory(List.of(stats));
                  }
                  return newAsset;
                })
            .collect(Collectors.toList());
    if (isResume) {
      Predicate<Asset> hasEmptyStats =
          asset -> asset.getHistory() == null || asset.getHistory().isEmpty();
      filterAsset.removeIf(hasEmptyStats);
    }
    return assetMapper.mapAssetList(filterAsset, marketData);
  }

  private Double getAssetValue(List<MarketData> marketData, String symbol) {
    if (marketData.isEmpty() || symbol == null) {
      return 1D;
    } else {
      return MathService.round(
          marketData.stream()
              .filter(marketData1 -> marketData1.getSymbol().equalsIgnoreCase(symbol))
              .findFirst()
              .get()
              .getCurrent_price(),
          2);
    }
  }
}
