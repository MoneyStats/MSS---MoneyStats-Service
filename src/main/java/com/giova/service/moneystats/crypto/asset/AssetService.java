package com.giova.service.moneystats.crypto.asset;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giova.service.moneystats.app.stats.StatsService;
import com.giova.service.moneystats.app.wallet.WalletService;
import com.giova.service.moneystats.app.wallet.dto.Wallet;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import com.giova.service.moneystats.crypto.asset.dto.Asset;
import com.giova.service.moneystats.crypto.asset.entity.AssetEntity;
import com.giova.service.moneystats.crypto.coinGecko.MarketDataService;
import com.giova.service.moneystats.crypto.coinGecko.dto.MarketData;
import com.giova.service.moneystats.generic.Response;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.github.giovannilamarmora.utils.interceptors.correlationID.CorrelationIdUtils;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.transaction.Transactional;
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

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
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
            CorrelationIdUtils.getCorrelationId(),
            Objects.requireNonNull(walletRes));
    return ResponseEntity.ok(response);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
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
            CorrelationIdUtils.getCorrelationId(),
            Objects.requireNonNull(saveWallet.getBody()).getData());
    return ResponseEntity.ok(response);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public ResponseEntity<Response> getAssets() {

    List<AssetEntity> assetEntities = assetDAO.findAllByUserIdOrderByRank(user.getId());
    List<Asset> assets = new ArrayList<>();
    List<LocalDate> getAllDates = statsService.getCryptoDistinctDates(user);

    String message = "";
    if (assetEntities.isEmpty()) {
      message = "Asset Empty, insert new Asset to get it!";
    } else {
      List<MarketData> marketData =
          marketDataService.getMarketData(user.getSettings().getCryptoCurrency());
      assets =
          assetMapper.mapAssetList(
              assetMapper.fromAssetEntitiesToAssets(assetEntities, marketData, getAllDates),
              marketData,
              getAllDates);
      message = "Found " + assets.size() + " Assets";
    }

    Response response =
        new Response(HttpStatus.OK.value(), message, CorrelationIdUtils.getCorrelationId(), assets);
    return ResponseEntity.ok(response);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public ResponseEntity<Response> getAsset(String identifier) {

    List<Asset> assets =
        mapper.convertValue(getAssets().getBody().getData(), new TypeReference<List<Asset>>() {});
    Asset asset =
        assets.stream()
            .filter(a -> a.getIdentifier().equalsIgnoreCase(identifier))
            .findFirst()
            .orElseThrow(
                () ->
                    new AssetException(
                        "An error happen during filtering assets, asset "
                            + identifier
                            + " was not found"));

    String message = "Data for " + identifier;

    Response response =
        new Response(HttpStatus.OK.value(), message, CorrelationIdUtils.getCorrelationId(), asset);
    return ResponseEntity.ok(response);
  }
}
