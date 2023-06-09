package com.giova.service.moneystats.crypto.asset;

import com.fasterxml.jackson.core.JsonProcessingException;
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

  @Autowired private WalletService walletService;
  @Autowired private IAssetDAO assetDAO;
  @Autowired private AssetMapper assetMapper;
  @Autowired private MarketDataService marketDataService;

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

    List<AssetEntity> assetEntities = assetDAO.findAllByUserId(user.getId());
    List<Asset> assets = new ArrayList<>();

    String message = "";
    if (assetEntities.isEmpty()) {
      message = "Asset Empty, insert new Asset to get it!";
    } else {
      List<MarketData> marketData = marketDataService.getMarketData(user.getCryptoCurrency());
      assets =
          assetMapper.mapAssetList(
              assetMapper.fromAssetEntitiesToAssets(assetEntities, marketData), marketData);
      message = "Found " + assets.size() + " Assets";
    }

    Response response =
        new Response(HttpStatus.OK.value(), message, CorrelationIdUtils.getCorrelationId(), assets);
    return ResponseEntity.ok(response);
  }
}
