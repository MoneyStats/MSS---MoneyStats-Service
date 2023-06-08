package com.giova.service.moneystats.crypto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giova.service.moneystats.app.stats.StatsService;
import com.giova.service.moneystats.app.stats.dto.Stats;
import com.giova.service.moneystats.app.wallet.WalletService;
import com.giova.service.moneystats.app.wallet.dto.Wallet;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import com.giova.service.moneystats.crypto.asset.AssetMapper;
import com.giova.service.moneystats.crypto.asset.dto.Asset;
import com.giova.service.moneystats.crypto.model.CryptoDashboard;
import com.giova.service.moneystats.generic.Response;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.github.giovannilamarmora.utils.interceptors.correlationID.CorrelationIdUtils;
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

  private static final Double BTC_VALUE = 28564.50;
  private final UserEntity user;
  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
  @Autowired private WalletService walletService;
  @Autowired private StatsService statsService;
  @Autowired private CryptoMapper cryptoMapper;
  @Autowired private AssetMapper assetMapper;

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public ResponseEntity<Response> getCryptoDashboardData() throws UtilsException {

    List<LocalDate> getAllDates = statsService.getCryptoDistinctDates(user);
    List<LocalDate> filter = new ArrayList<>();
    Map<String, CryptoDashboard> getData = new HashMap<>();
    int thisYear = 0;
    if (!getAllDates.isEmpty()) {
      int currentYear = getAllDates.get(getAllDates.size() - 1).getYear();
      filter =
          getAllDates.stream().filter(d -> d.getYear() == currentYear).collect(Collectors.toList());

      thisYear = currentYear;
      getData = mapDashBoard(filter);
    } else {
      List<Wallet> getAllWallet =
          objectMapper.convertValue(
              walletService.getCryptoWallets().getBody().getData(),
              new TypeReference<List<Wallet>>() {});
      CryptoDashboard dashboard =
          cryptoMapper.mapCryptoDashboardWithoutStats(
              user.getCryptoCurrency(), getAllWallet, getCryptoAsset(getAllWallet), BTC_VALUE);
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
    List<LocalDate> getAllDates = statsService.getCryptoDistinctDates(user);
    Map<String, CryptoDashboard> getData = new HashMap<>();
    int thisYear = LocalDate.now().getYear();
    if (!getAllDates.isEmpty()) {
      getData = mapDashBoard(getAllDates);
    } else {

      List<Wallet> getAllWallet =
          objectMapper.convertValue(
              walletService.getCryptoWallets().getBody().getData(),
              new TypeReference<List<Wallet>>() {});
      CryptoDashboard dashboard =
          cryptoMapper.mapCryptoDashboardWithoutStats(
              user.getCryptoCurrency(), getAllWallet, getCryptoAsset(getAllWallet), BTC_VALUE);
      getData.put(String.valueOf(thisYear), dashboard);
    }

    String message = "Data for Crypto Resume!";

    Response response =
        new Response(
            HttpStatus.OK.value(), message, CorrelationIdUtils.getCorrelationId(), getData);
    return ResponseEntity.ok(response);
  }

  private Map<String, CryptoDashboard> mapDashBoard(List<LocalDate> dates) throws UtilsException {
    Map<String, CryptoDashboard> response = new HashMap<>();

    List<Integer> distinctDatesByYear =
        dates.stream().map(LocalDate::getYear).distinct().collect(Collectors.toList());

    // Wallet List
    List<Wallet> getAllWallet =
        objectMapper.convertValue(
            walletService.getCryptoWallets().getBody().getData(),
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

              AtomicInteger indexWallet = new AtomicInteger(0);

              List<Wallet> filterWallet =
                  getAllWallet.stream()
                      .map(
                          wallet -> {
                            Wallet wallet1 = new Wallet();
                            BeanUtils.copyProperties(wallet, wallet1);
                            wallet1.setHistory(null);
                            wallet1.setAssets(
                                wallet.getAssets().stream()
                                    .map(
                                        asset -> {
                                          Asset asset1 = new Asset();
                                          BeanUtils.copyProperties(asset, asset1);
                                          List<Stats> listFilter =
                                              asset.getHistory().stream()
                                                  .filter(h -> h.getDate().getYear() == year)
                                                  .collect(Collectors.toList());

                                          asset1.setHistory(listFilter);
                                          // asset1.setValue(asset.getInvested() * BTC_VALUE);
                                          balance.updateAndGet(v -> v + asset1.getValue());
                                          if (wallet.getType().equalsIgnoreCase("Holding"))
                                            holdingBalance.updateAndGet(v -> v + asset1.getValue());

                                          if (!listFilter.isEmpty()) {

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
                            indexWallet.incrementAndGet();
                            return wallet1;
                          })
                      .collect(Collectors.toList());

              dashboard.setAssets(getCryptoAsset(filterWallet));

              // Filtro Wallet cancellati da anni che non hanno stats
              Predicate<Wallet> walletRemovedInThePast =
                  wallet -> wallet.getHistory().isEmpty() && wallet.getDeletedDate() != null;
              filterWallet.removeIf(walletRemovedInThePast);

              // Mi serve per mappare il passato
              if (index.get() > 0) {
                // Remove wallet that haven't any stats
                Predicate<Wallet> hasEmptyStats = wallet -> wallet.getHistory().isEmpty();
                filterWallet.removeIf(hasEmptyStats);
              }

              try {
                dashboard.setWallets(filterWallet);
                cryptoMapper.mapDashboardBalanceAndPerformance(
                    dashboard, balance, lastBalance, holdingBalance, holdingLastBalance, BTC_VALUE);
              } catch (UtilsException e) {
                throw new RuntimeException(e);
              }
              index.incrementAndGet();
              response.put(String.valueOf(year), dashboard);
            })
        .collect(Collectors.toList());

    return response;
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

  private List<Asset> getCryptoAsset(List<Wallet> filterWallet) {
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
                  if (asset.getHistory() != null && !asset.getHistory().isEmpty()) {
                    Stats stats = asset.getHistory().get(asset.getHistory().size() - 1);
                    newAsset.setHistory(List.of(stats));
                  }
                  return newAsset;
                })
            .collect(Collectors.toList());
    return assetMapper.mapAssetList(filterAsset);
  }
}
