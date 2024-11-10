package com.giova.service.moneystats.crypto.asset;

import com.giova.service.moneystats.app.wallet.dto.Wallet;
import io.github.giovannilamarmora.utils.generic.Response;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Logged
@RestController
@RequestMapping("/v1/crypto")
@CrossOrigin(origins = "*")
@Tag(name = "Asset", description = "API Crypto Asset")
public class AssetControllerImpl implements AssetController {
  @Autowired private AssetService assetService;

  /**
   * API to get an Asset by his identifier
   *
   * @param token Access token of the user
   * @param identifier to be searched
   * @return Asset
   */
  @Override
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public ResponseEntity<Response> getAssetByIdentifier(String token, String identifier) {
    return assetService.getAssetByIdentifier(identifier);
  }

  /**
   * API to get the full list of the Asset
   *
   * @param token Access token of the user
   * @return Asset List
   */
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public ResponseEntity<Response> getAllAssets(String token, Boolean includeOperations) {
    return assetService.getAssets(includeOperations);
  }

  /**
   * API To add a crypto asset
   *
   * @param token Of the user
   * @param wallet Used to update data of the wallets
   * @return Wallet Updated
   */
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public ResponseEntity<Response> addCryptoAsset(String token, Wallet wallet) {
    return assetService.addAsset(wallet);
  }

  /**
   * API To update a crypto asset
   *
   * @param token Of the user
   * @param wallet Used to update data of the wallets
   * @return Wallet Updated
   */
  @Override
  public ResponseEntity<Response> updateCryptoAsset(String token, Wallet wallet) {
    return assetService.updateAsset(wallet);
  }

  /**
   * API to add a list of crypto assets
   *
   * @param token Of the user
   * @param wallets Used to update data of the wallets
   * @return Wallet Updated
   */
  @Override
  public ResponseEntity<Response> addCryptoAssets(String token, List<Wallet> wallets) {
    return assetService.addAssets(wallets);
  }

  /**
   * API to update a list of crypto assets
   *
   * @param token Of the user
   * @param wallets Used to update data of the wallets
   * @return Wallet Updated
   */
  @Override
  public ResponseEntity<Response> updateCryptoAssets(String token, List<Wallet> wallets) {
    return assetService.updateAssets(wallets);
  }
}
