package com.giova.service.moneystats.crypto.asset;

import com.giova.service.moneystats.app.stats.StatsService;
import com.giova.service.moneystats.app.wallet.WalletService;
import com.giova.service.moneystats.app.wallet.dto.Wallet;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import com.giova.service.moneystats.crypto.asset.database.AssetRepository;
import com.giova.service.moneystats.crypto.asset.dto.Asset;
import com.giova.service.moneystats.crypto.asset.entity.AssetEntity;
import com.giova.service.moneystats.crypto.marketData.MarketDataService;
import com.giova.service.moneystats.crypto.marketData.dto.MarketData;
import com.giova.service.moneystats.utilities.Utils;
import io.github.giovannilamarmora.utils.context.TraceUtils;
import io.github.giovannilamarmora.utils.generic.Response;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.github.giovannilamarmora.utils.utilities.Utilities;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
public class AssetService {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  private final UserEntity user;
  @Autowired private WalletService walletService;
  @Autowired private MarketDataService marketDataService;
  @Autowired private StatsService statsService;

  @Autowired private AssetRepository assetRepository;

  /**
   * Get Asset by his identifier
   *
   * @param identifier to be searched
   * @return Asset
   */
  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public ResponseEntity<Response> getAssetByIdentifier(String identifier) {
    LOG.info("Getting asset {}", identifier);
    List<AssetEntity> assetEntities =
        assetRepository.findAllByIdentifierAndUserId(identifier, user.getId());
    List<Asset> assets;
    Asset asset = null;

    String message;

    if (assetEntities.isEmpty()) {
      message = "Asset " + identifier + " not found, insert new Asset to get it!";
    } else {
      List<LocalDate> getAllDates = statsService.getCryptoDistinctDates(user);

      List<MarketData> marketData =
          marketDataService.getMarketData(user.getSettings().getCryptoCurrency());
      assets =
          AssetMapper.mapAssetList(
              AssetMapper.fromAssetEntitiesToAssets(assetEntities, marketData, getAllDates),
              marketData,
              getAllDates);
      asset = assets.getFirst();
      message = "Data for " + identifier;
    }

    Response response = new Response(HttpStatus.OK.value(), message, TraceUtils.getSpanID(), asset);
    return ResponseEntity.ok(response);
  }

  /**
   * Get All Asset
   *
   * @return Asset
   */
  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public ResponseEntity<Response> getAssets() {
    LOG.info("Getting all assets");
    List<AssetEntity> assetEntities = assetRepository.findAllByUserIdOrderByRank(user.getId());
    List<Asset> assets = new ArrayList<>();

    String message = "";
    if (assetEntities.isEmpty()) {
      message = "Asset Empty, insert new Asset to get it!";
    } else {
      List<MarketData> marketData =
          marketDataService.getMarketData(user.getSettings().getCryptoCurrency());

      List<LocalDate> getAllDates = statsService.getCryptoDistinctDates(user);
      assets =
          AssetMapper.mapAssetList(
              AssetMapper.fromAssetEntitiesToAssets(assetEntities, marketData, getAllDates),
              marketData,
              getAllDates);
      message = "Found " + assets.size() + " Assets";
    }

    Response response =
        new Response(HttpStatus.OK.value(), message, TraceUtils.getSpanID(), assets);
    return ResponseEntity.ok(response);
  }

  /**
   * Add a new Crypto Asset
   *
   * @param wallet to be updated
   * @return Asset
   */
  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = Exception.class)
  public ResponseEntity<Response> addAsset(Wallet wallet) {
    LOG.info(
        "Add asset {} for wallet {}", wallet.getAssets().getLast().getName(), wallet.getName());

    Boolean isLiveWallet = Utils.isLiveWallet(null, user);

    String message = "Asset " + wallet.getAssets().getLast().getName() + " Successfully saved!";

    ResponseEntity<Response> saveWallet =
        walletService.addOrUpdateWallet(wallet, isLiveWallet, message);

    if (!ObjectUtils.isEmpty(saveWallet.getBody())) {
      assetRepository.clearAllWalletsCache();
    }

    Response response =
        new Response(
            HttpStatus.OK.value(), message, TraceUtils.getSpanID(), saveWallet.getBody().getData());
    return ResponseEntity.ok(response);
  }

  /**
   * Update a Crypto Asset
   *
   * @param wallet to be updated
   * @return Asset
   */
  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = Exception.class)
  public ResponseEntity<Response> updateAsset(Wallet wallet) {
    LOG.info(
        "Update asset {} for wallet {}", wallet.getAssets().getLast().getName(), wallet.getName());

    Boolean isLiveWallet = Utils.isLiveWallet(null, user);

    String message = "Asset " + wallet.getAssets().getLast().getName() + " Successfully updated!";

    ResponseEntity<Response> saveWallet =
        walletService.addOrUpdateWallet(wallet, isLiveWallet, message);

    if (!ObjectUtils.isEmpty(saveWallet.getBody())) {
      assetRepository.clearAllWalletsCache();
    }

    Response response =
        new Response(
            HttpStatus.OK.value(), message, TraceUtils.getSpanID(), saveWallet.getBody().getData());
    return ResponseEntity.ok(response);
  }

  /**
   * Add a new Crypto Asset
   *
   * @param wallets to be updated
   * @return Asset
   */
  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = Exception.class)
  public ResponseEntity<Response> addAssets(List<Wallet> wallets) {
    LOG.info("Add assets list started");
    StringBuilder message = new StringBuilder("Assets ");
    List<Wallet> walletRes = new ArrayList<>();

    Boolean isLiveWallet = Utils.isLiveWallet(null, user);

    wallets.forEach(
        wallet -> {
          ResponseEntity<Response> saveWallet =
              walletService.addOrUpdateWallet(wallet, isLiveWallet, null);
          message.append(wallet.getAssets().getLast().getName()).append(" ");
          if (!Utilities.isNullOrEmpty(saveWallet.getBody()))
            walletRes.add((Wallet) Objects.requireNonNull(saveWallet.getBody()).getData());
        });
    message.append("Successfully saved!");

    if (!Utilities.isNullOrEmpty(walletRes)) {
      assetRepository.clearAllWalletsCache();
    }

    Response response =
        new Response(HttpStatus.OK.value(), message.toString(), TraceUtils.getSpanID(), walletRes);
    return ResponseEntity.ok(response);
  }

  /**
   * Update a Crypto Assets
   *
   * @param wallets to be updated
   * @return Asset
   */
  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = Exception.class)
  public ResponseEntity<Response> updateAssets(List<Wallet> wallets) {
    LOG.info("Update assets list started");
    StringBuilder message = new StringBuilder("Assets ");
    List<Wallet> walletRes = new ArrayList<>();

    Boolean isLiveWallet = Utils.isLiveWallet(null, user);

    wallets.forEach(
        wallet -> {
          ResponseEntity<Response> saveWallet =
              walletService.addOrUpdateWallet(wallet, isLiveWallet, null);
          message.append(wallet.getAssets().getLast().getName()).append(" ");
          if (!Utilities.isNullOrEmpty(saveWallet.getBody()))
            walletRes.add((Wallet) Objects.requireNonNull(saveWallet.getBody()).getData());
        });
    message.append("Successfully updated!");

    if (!Utilities.isNullOrEmpty(walletRes)) {
      assetRepository.clearAllWalletsCache();
    }

    Response response =
        new Response(HttpStatus.OK.value(), message.toString(), TraceUtils.getSpanID(), walletRes);
    return ResponseEntity.ok(response);
  }
}
