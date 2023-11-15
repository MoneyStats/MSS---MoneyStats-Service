package com.giova.service.moneystats.crypto.asset;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.giova.service.moneystats.app.wallet.dto.Wallet;
import com.giova.service.moneystats.generic.Response;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Logged
@RestController
@RequestMapping("/v1/crypto/asset")
@CrossOrigin(origins = "*")
public class AssetController {
  @Autowired private AssetService assetService;

  @PostMapping(value = "/addOrUpdate", produces = MediaType.APPLICATION_JSON_VALUE)
  @Tag(name = "Asset", description = "API to get Crypto Asset")
  @Operation(description = "API to get Crypto Asset", tags = "Asset")
  @LogInterceptor(type = LogTimeTracker.ActionType.APP_CONTROLLER)
  public ResponseEntity<Response> saveOrUpdateCryptoAsset(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authToken, @RequestBody @Valid Wallet wallet)
      throws UtilsException, JsonProcessingException {
    return assetService.insertOrUpdateAsset(wallet);
  }

  @PostMapping(value = "/list/addOrUpdate", produces = MediaType.APPLICATION_JSON_VALUE)
  @Tag(name = "Asset", description = "API to get Crypto Asset")
  @Operation(description = "API to get Crypto Asset", tags = "Asset")
  @LogInterceptor(type = LogTimeTracker.ActionType.APP_CONTROLLER)
  public ResponseEntity<Response> saveOrUpdateCryptoAssets(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authToken,
      @RequestBody @Valid List<Wallet> wallets)
      throws UtilsException, JsonProcessingException {
    return assetService.insertOrUpdateAssets(wallets);
  }

  @GetMapping(value = "/getAll", produces = MediaType.APPLICATION_JSON_VALUE)
  @Tag(name = "Asset", description = "API to get Crypto Asset")
  @Operation(description = "API to get Crypto Asset", tags = "Asset")
  @LogInterceptor(type = LogTimeTracker.ActionType.APP_CONTROLLER)
  public ResponseEntity<Response> getAllAssets(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authToken) throws UtilsException {
    return assetService.getAssets();
  }

  @GetMapping(value = "/get", produces = MediaType.APPLICATION_JSON_VALUE)
  @Tag(name = "Asset", description = "API to get Crypto Asset")
  @Operation(description = "API to get Crypto Asset", tags = "Asset")
  @LogInterceptor(type = LogTimeTracker.ActionType.APP_CONTROLLER)
  public ResponseEntity<Response> getAsset(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authToken,
      @RequestParam(value = "identifier") String identifier)
      throws UtilsException {
    return assetService.getAsset(identifier);
  }
}
