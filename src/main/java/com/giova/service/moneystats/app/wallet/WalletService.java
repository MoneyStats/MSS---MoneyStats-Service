package com.giova.service.moneystats.app.wallet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.giova.service.moneystats.app.attachments.ImageService;
import com.giova.service.moneystats.app.attachments.dto.Image;
import com.giova.service.moneystats.app.stats.StatsService;
import com.giova.service.moneystats.app.wallet.database.WalletCacheService;
import com.giova.service.moneystats.app.wallet.database.WalletRepository;
import com.giova.service.moneystats.app.wallet.dto.Wallet;
import com.giova.service.moneystats.app.wallet.entity.WalletEntity;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import com.giova.service.moneystats.crypto.asset.AssetMapper;
import com.giova.service.moneystats.crypto.asset.database.AssetRepository;
import com.giova.service.moneystats.crypto.asset.dto.AssetLivePrice;
import com.giova.service.moneystats.crypto.asset.dto.AssetWithoutOpAndStats;
import com.giova.service.moneystats.crypto.asset.entity.AssetEntity;
import com.giova.service.moneystats.crypto.forex.ForexDataService;
import com.giova.service.moneystats.crypto.forex.dto.ForexData;
import com.giova.service.moneystats.crypto.marketData.MarketDataService;
import com.giova.service.moneystats.crypto.marketData.dto.MarketData;
import com.giova.service.moneystats.settings.dto.Status;
import io.github.giovannilamarmora.utils.context.TraceUtils;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.generic.Response;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.github.giovannilamarmora.utils.utilities.Utilities;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
@Logged
@AllArgsConstructor
public class WalletService {
  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  private final UserEntity user;
  @Autowired private WalletCacheService walletCacheService;
  @Autowired private WalletMapper walletMapper;
  @Autowired private ImageService imageService;
  @Autowired private StatsService statsService;
  @Autowired private ForexDataService forexDataService;
  @Autowired private MarketDataService marketDataService;
  @Autowired private WalletRepository walletRepository;
  @Autowired private AssetRepository assetRepository;

  /**
   * Method that Return the list of Wallets
   *
   * @param live Used to Get the live price of the wallet
   * @param includeHistory Used to get the full History of the Wallet
   * @param includeAssets Used to get only the Asset without Operation and History
   * @param includeFullAssets Used to get the full Assets included with Operation and History
   * @return The list of the Wallets
   */
  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public ResponseEntity<Response> getAllWallets(
      Boolean live, Boolean includeHistory, Boolean includeAssets, Boolean includeFullAssets) {
    /**
     * We give the priority to the param "Boolean live", if the param is null we check the User
     * Setting if it has the live wallet status ACTIVE. For the FrontEnd is Recommended to use this
     * value as Null
     */
    Boolean isLiveWallet = isLiveWallet(live);

    List<WalletEntity> walletEntity;
    /* If you have the live included you can get the Forex Data, otherwise we do not need it */
    ForexData forexData =
        isLiveWallet
            ? forexDataService.getForexDataByCurrency(user.getSettings().getCryptoCurrency())
            : null;
    List<MarketData> marketData =
        (isLiveWallet || includeAssets || includeFullAssets)
            ? marketDataService.getMarketDataOLD(user.getSettings().getCryptoCurrency())
            : null;
    List<LocalDate> getAllCryptoDates =
        includeAssets ? statsService.getCryptoDistinctDates(user) : null;
    if (!isLiveWallet && !includeHistory && !includeAssets && !includeFullAssets)
      walletEntity = walletRepository.findAllByUserIdWithoutAssetsAndHistory(user.getId());
    else if (isLiveWallet && !includeHistory && !includeAssets) {
      walletEntity = walletRepository.findAllByUserIdWithoutAssetsAndHistory(user.getId());
      List<Long> walletIds = walletEntity.stream().map(WalletEntity::getId).toList();
      List<AssetLivePrice> livePrices =
          assetRepository.findAssetsByWalletIds(walletIds, user.getId());
      walletEntity =
          walletEntity.stream()
              .peek(
                  walletEntity1 ->
                      walletEntity1.setAssets(
                          AssetMapper.fromAssetLivePricesToAssetEntities(
                              livePrices, walletEntity1.getId())))
              .toList();
    } else if (!includeHistory && !includeFullAssets) {
      walletEntity = walletRepository.findAllByUserIdWithoutAssetsAndHistory(user.getId());
      List<Long> walletIds = walletEntity.stream().map(WalletEntity::getId).toList();
      List<AssetWithoutOpAndStats> assetFulls =
          assetRepository.findAllAssetsByWalletIds(walletIds, user.getId());
      walletEntity =
          walletEntity.stream()
              .peek(
                  walletEntity1 ->
                      walletEntity1.setAssets(
                          AssetMapper.fromAssetToAssetEntities(assetFulls, walletEntity1.getId())))
              .toList();
    } else if (!includeHistory) {
      walletEntity = walletRepository.findAllByUserIdWithoutAssetsAndHistory(user.getId());
      List<Long> walletIds = walletEntity.stream().map(WalletEntity::getId).toList();
      List<AssetEntity> assetFulls = assetRepository.findAllByWalletIds(walletIds, user.getId());
      walletEntity =
          walletEntity.stream()
              .peek(
                  walletEntity1 ->
                      walletEntity1.setAssets(
                          assetFulls.stream()
                              .filter(
                                  assetEntity ->
                                      assetEntity.getWallet().getId().equals(walletEntity1.getId()))
                              .toList()))
              .toList();
    } else walletEntity = walletRepository.findAllByUserId(user.getId());

    String message = "";
    if (walletEntity.isEmpty()) {
      message = "Wallet Empty, insert new Wallet to get it!";
    } else {
      message = "Found " + walletEntity.size() + " Wallets";
    }

    List<Wallet> walletToReturn =
        WalletMapper.mapFromWalletEntitiesToWalletList(
            walletEntity,
            isLiveWallet,
            includeAssets,
            includeFullAssets,
            getAllCryptoDates,
            forexData,
            marketData,
            user.getSettings().getCurrency());

    Response response =
        new Response(HttpStatus.OK.value(), message, TraceUtils.getSpanID(), walletToReturn);
    return ResponseEntity.ok(response);
  }

  /**
   * Method that Return the list of Wallets
   *
   * @param live Used to Get the live price of the wallet
   * @param id Wallet ID to be searched
   * @return The list of the Wallets
   */
  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public ResponseEntity<Response> getWalletById(Boolean live, Long id) {
    /**
     * We give the priority to the param "Boolean live", if the param is null we check the User
     * Setting if it has the live wallet status ACTIVE. For the FrontEnd is Recommended to use this
     * value as Null
     */
    Boolean isLiveWallet = isLiveWallet(live);

    /* If you have the live included you can get the Forex Data, otherwise we do not need it */
    ForexData forexData =
        isLiveWallet
            ? forexDataService.getForexDataByCurrency(user.getSettings().getCryptoCurrency())
            : null;
    List<MarketData> marketData =
        marketDataService.getMarketDataOLD(user.getSettings().getCryptoCurrency());
    List<LocalDate> getAllCryptoDates = statsService.getCryptoDistinctDates(user);
    WalletEntity walletEntity = walletRepository.findWalletEntityById(id, user.getId());

    Wallet walletToReturn = null;

    String message = "";
    if (Utilities.isNullOrEmpty(walletEntity)) {
      message = "No Wallet found, insert new Wallet to get it!";
    } else {
      message = "Found " + walletEntity.getName() + " Wallet";
      List<Wallet> walletsToReturn =
          WalletMapper.mapFromWalletEntitiesToWalletList(
              List.of(walletEntity),
              isLiveWallet,
              true,
              true,
              getAllCryptoDates,
              forexData,
              marketData,
              user.getSettings().getCurrency());
      walletToReturn = walletsToReturn == null ? null : walletsToReturn.getFirst();
    }

    Response response =
        new Response(HttpStatus.OK.value(), message, TraceUtils.getSpanID(), walletToReturn);
    return ResponseEntity.ok(response);
  }

  /**
   * Api to Add Wallet
   *
   * @param wallet to be added
   * @return wallet saved
   */
  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = Exception.class)
  public ResponseEntity<Response> addWallet(Wallet wallet) {
    String message = "Wallet " + wallet.getName() + " Successfully saved!";
    return addOrUpdateWallet(wallet, false, message);
  }

  /**
   * Api to Update Wallet
   *
   * @param wallet to be updated
   * @return wallet updated
   */
  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = Exception.class)
  public ResponseEntity<Response> updateWallet(Wallet wallet, Boolean live) {
    String message = "Wallet " + wallet.getName() + " Successfully updated!";
    return addOrUpdateWallet(wallet, isLiveWallet(live), message);
  }

  /**
   * Delete a Wallet
   *
   * @param id Wallet to be deleted
   * @return wallet deleted
   */
  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = Exception.class)
  public ResponseEntity<Response> deleteWallet(Long id) {
    WalletEntity walletEntity = walletRepository.findWalletEntityById(id, user.getId());
    walletEntity.setDeletedDate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

    WalletEntity saved = walletRepository.save(walletEntity);
    List<MarketData> marketData = Collections.emptyList();
    if (!Utilities.isNullOrEmpty(saved.getAssets()))
      marketData = marketDataService.getMarketDataOLD(user.getSettings().getCryptoCurrency());

    Wallet walletToReturn = WalletMapper.fromWalletEntityToWallet(saved, null, marketData);

    String message = "Wallet " + walletToReturn.getName() + " Successfully deleted!";

    Response response =
        new Response(HttpStatus.OK.value(), message, TraceUtils.getSpanID(), walletToReturn);
    return ResponseEntity.ok(response);
  }

  /**
   * Add, Update or Delete a Wallet
   *
   * @param wallet to be edited
   * @param live param for live data
   * @return wallet edited
   */
  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = Exception.class)
  private ResponseEntity<Response> addOrUpdateWallet(Wallet wallet, Boolean live, String message) {
    WalletEntity walletEntity = WalletMapper.fromWalletToWalletEntity(wallet, user);

    /**
     * I need to check if is an Update Wallet, if the wallet is live I need to prevent the data to
     * return the wrong balance, caused because of the live wallet
     */
    if (!Utilities.isNullOrEmpty(wallet.getId()) && live) {
      WalletEntity getFromDB = walletRepository.findWalletEntityById(wallet.getId(), user.getId());
      WalletMapper.mapWalletEntityToBeSaved(walletEntity, getFromDB);
    }

    /** If the wallet is to delete I need to get the full wallet to be deleted from the database */
    if (!Utilities.isNullOrEmpty(wallet.getImgName())) {
      LOG.debug("Building image with filename {}", wallet.getImgName());
      Image image = imageService.getAttachment(wallet.getImgName());
      imageService.removeAttachment(wallet.getImgName());
      walletEntity.setImg(
          "data:"
              + image.getContentType()
              + ";base64,"
              + Base64.getEncoder().encodeToString(image.getBody()));
    }

    WalletEntity saved = walletRepository.save(walletEntity);
    List<MarketData> marketData = Collections.emptyList();
    if (!Utilities.isNullOrEmpty(saved.getAssets()))
      marketData = marketDataService.getMarketData(user.getSettings().getCryptoCurrency());

    Wallet walletToReturn = WalletMapper.fromWalletEntityToWallet(saved, null, marketData);

    Response response =
        new Response(HttpStatus.OK.value(), message, TraceUtils.getSpanID(), walletToReturn);
    return ResponseEntity.ok(response);
  }

  private Boolean isLiveWallet(Boolean live) {
    /**
     * We give the priority to the param "Boolean live", if the param is null we check the User
     * Setting if it has the live wallet status ACTIVE. For the FrontEnd is Recommended to use this
     * value as Null
     */
    return !ObjectUtils.isEmpty(live)
        ? live
        : !ObjectUtils.isEmpty(user.getSettings().getLiveWallets())
            && user.getSettings().getLiveWallets().equalsIgnoreCase(Status.ACTIVE.toString());
  }

  /* OLD DATA */
  @Deprecated
  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = Exception.class)
  public ResponseEntity<Response> insertOrUpdateWallet(Wallet wallet)
      throws UtilsException, JsonProcessingException {
    Boolean isLiveWallet =
        user.getSettings().getLiveWallets() != null
            && user.getSettings().getLiveWallets().equalsIgnoreCase(Status.ACTIVE.toString());
    return insertOrUpdate(wallet, isLiveWallet);
  }

  @Deprecated
  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = Exception.class)
  public ResponseEntity<Response> notFilterInsertOrUpdateWallet(Wallet wallet)
      throws UtilsException, JsonProcessingException {
    return insertOrUpdate(wallet, false);
  }

  @Deprecated
  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = Exception.class)
  public ResponseEntity<Response> insertOrUpdate(Wallet wallet, Boolean isWalletLive)
      throws UtilsException, JsonProcessingException {
    WalletEntity walletEntity = walletMapper.fromWalletToWalletEntity(wallet, user);

    if (wallet.getId() != null && isWalletLive) {
      WalletEntity getFromDB =
          walletCacheService.findWalletEntityById(wallet.getId(), user.getId());
      if (getFromDB != null) {
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
    }

    if (wallet.getImgName() != null && !wallet.getImgName().isEmpty()) {
      LOG.info("Building attachment with filename {}", wallet.getImgName());
      Image image = imageService.getAttachment(wallet.getImgName());
      imageService.removeAttachment(wallet.getImgName());
      walletEntity.setImg(
          "data:"
              + image.getContentType()
              + ";base64,"
              + Base64.getEncoder().encodeToString(image.getBody()));
    }

    WalletEntity saved = walletCacheService.save(walletEntity);

    List<MarketData> marketData = Collections.emptyList();
    if (!Utilities.isNullOrEmpty(saved.getAssets()))
      marketData = marketDataService.getMarketDataOLD(user.getSettings().getCryptoCurrency());

    // List<LocalDate> getAllCryptoDates = statsService.getCryptoDistinctDates(user);
    Wallet walletToReturn = walletMapper.fromWalletEntityToWallet(saved, null, marketData);
    // if (wallet.getHistory() != null && !wallet.getHistory().isEmpty()) {
    //  walletToReturn.setHistory(statsService.saveStats(wallet.getHistory(), saved, user));
    // }

    String message = "Wallet " + walletToReturn.getName() + " Successfully saved!";

    Response response =
        new Response(HttpStatus.OK.value(), message, TraceUtils.getSpanID(), walletToReturn);
    return ResponseEntity.ok(response);
  }

  @Deprecated
  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public ResponseEntity<Response> getWallets() throws UtilsException {
    List<WalletEntity> walletEntity = walletCacheService.findAllByUserId(user.getId());

    String message = "";
    if (walletEntity.isEmpty()) {
      message = "Wallet Empty, insert new Wallet to get it!";
    } else {
      message = "Found " + walletEntity.size() + " Wallets";
    }
    Boolean isLiveWallet =
        user.getSettings().getLiveWallets() != null
            && user.getSettings().getLiveWallets().equalsIgnoreCase(Status.ACTIVE.toString());

    List<LocalDate> getAllCryptoDates = statsService.getCryptoDistinctDates(user);
    List<Wallet> walletToReturn =
        walletMapper.fromWalletEntitiesToWallets(walletEntity, isLiveWallet, getAllCryptoDates);

    Response response =
        new Response(HttpStatus.OK.value(), message, TraceUtils.getSpanID(), walletToReturn);
    return ResponseEntity.ok(response);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public ResponseEntity<Response> getCryptoWallets(Boolean live) throws UtilsException {
    // UserEntity user = authService.checkLogin(authToken);
    String CRYPTO = "Crypto";

    List<WalletEntity> walletEntity =
        walletCacheService.findAllByUserIdAndCategory(user.getId(), CRYPTO);

    String message = "";
    if (walletEntity.isEmpty()) {
      message = "Crypto Wallet Empty, insert new Crypto Wallet to get it!";
    } else {
      message = "Found " + walletEntity.size() + " Crypto Wallets";
    }

    List<LocalDate> getAllCryptoDates = statsService.getCryptoDistinctDates(user);
    List<Wallet> walletToReturn =
        walletMapper.fromWalletEntitiesToWallets(walletEntity, live, getAllCryptoDates);

    Response response =
        new Response(HttpStatus.OK.value(), message, TraceUtils.getSpanID(), walletToReturn);
    return ResponseEntity.ok(response);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public List<Wallet> deleteWalletIds(List<Wallet> wallets) {
    return walletMapper.deleteWalletIds(wallets);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public List<Wallet> saveWalletEntities(List<Wallet> wallets) {

    List<WalletEntity> walletEntities =
        wallets.stream()
            .map(
                wallet -> {
                  WalletEntity walletEntity = walletMapper.fromWalletToWalletEntity(wallet, user);

                  if (wallet.getImgName() != null && !wallet.getImgName().isEmpty()) {
                    LOG.info("Building attachment with filename {}", wallet.getImgName());
                    Image image = imageService.getAttachment(wallet.getImgName());
                    imageService.removeAttachment(wallet.getImgName());
                    walletEntity.setImg(
                        "data:"
                            + image.getContentType()
                            + ";base64,"
                            + Base64.getEncoder().encodeToString(image.getBody()));
                  }
                  return walletEntity;
                })
            .collect(Collectors.toList());

    List<WalletEntity> saved = walletCacheService.saveAll(walletEntities);

    return saved.stream()
        .map(
            walletEntity -> {
              Wallet wallet = new Wallet();
              walletMapper.fromWalletEntityToWallet(walletEntity, null, null);

              return wallet;
            })
        .collect(Collectors.toList());
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public void deleteWalletEntities() {
    walletCacheService.deleteAllByUserId(user.getId());
  }
}
