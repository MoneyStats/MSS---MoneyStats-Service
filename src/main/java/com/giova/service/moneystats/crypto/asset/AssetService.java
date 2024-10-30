package com.giova.service.moneystats.crypto.asset;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giova.service.moneystats.app.stats.StatsService;
import com.giova.service.moneystats.app.wallet.WalletService;
import com.giova.service.moneystats.app.wallet.dto.Wallet;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import com.giova.service.moneystats.crypto.asset.database.AssetRepository;
import com.giova.service.moneystats.crypto.asset.database.IAssetDAO;
import com.giova.service.moneystats.crypto.asset.dto.Asset;
import com.giova.service.moneystats.crypto.asset.entity.AssetEntity;
import com.giova.service.moneystats.crypto.marketData.MarketDataService;
import com.giova.service.moneystats.crypto.marketData.dto.MarketData;
import io.github.giovannilamarmora.utils.context.TraceUtils;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.generic.Response;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
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

@Service
@Logged
@AllArgsConstructor
public class AssetService {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  private final UserEntity user;
  private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
  @Autowired private WalletService walletService;
  @Autowired private IAssetDAO assetDAO;
  @Autowired private AssetMapper assetMapper;
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

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = Exception.class)
  public ResponseEntity<Response> insertOrUpdateAssets(List<Wallet> wallets)
      throws UtilsException, JsonProcessingException {
    StringBuilder message = new StringBuilder("Assets ");
    List<Wallet> walletRes = new ArrayList<>();

    wallets.forEach(
        wallet -> {
          try {
            ResponseEntity<Response> saveWallet = walletService.insertOrUpdateWallet(wallet);
            message
                .append(wallet.getAssets().get(wallet.getAssets().size() - 1).getName())
                .append(" ");
            walletRes.add((Wallet) Objects.requireNonNull(saveWallet.getBody()).getData());
          } catch (JsonProcessingException e) {
            LOG.error("An Error happen during saving asset, message is {}", e.getMessage());
            throw new AssetException(e.getMessage());
          }
        });
    message.append("Successfully saved!");

    Response response =
        new Response(
            HttpStatus.OK.value(),
            message.toString(),
            TraceUtils.getSpanID(),
            Objects.requireNonNull(walletRes));
    return ResponseEntity.ok(response);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = Exception.class)
  public ResponseEntity<Response> insertOrUpdateAsset(Wallet wallet)
      throws UtilsException, JsonProcessingException {

    ResponseEntity<Response> saveWallet = walletService.insertOrUpdateWallet(wallet);

    String message =
        "Asset "
            + wallet.getAssets().get(wallet.getAssets().size() - 1).getName()
            + " Successfully saved!";

    Response response =
        new Response(
            HttpStatus.OK.value(),
            message,
            TraceUtils.getSpanID(),
            Objects.requireNonNull(saveWallet.getBody()).getData());
    return ResponseEntity.ok(response);
  }
}
