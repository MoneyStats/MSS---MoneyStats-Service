package com.giova.service.moneystats.crypto.asset;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.giova.service.moneystats.app.wallet.dto.Wallet;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.exception.dto.ExceptionResponse;
import io.github.giovannilamarmora.utils.generic.Response;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
  public ResponseEntity<Response> getAllAssets(String token) throws UtilsException {
    return assetService.getAssets();
  }

  @PostMapping(value = "/addOrUpdate", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      description = "API to add or update Crypto Asset",
      summary = "Add or Update Crypto Asset",
      tags = "Asset")
  @ApiResponse(
      responseCode = "401",
      description = "Invalid JWE",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@invalid-jwe.json")))
  @ApiResponse(
      responseCode = "401",
      description = "Expired JWE",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@expired-jwe.json")))
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public ResponseEntity<Response> saveOrUpdateCryptoAsset(
      @RequestHeader(HttpHeaders.AUTHORIZATION) @Valid @Schema(description = "Authorization Token")
          String authToken,
      @RequestBody @Valid @Schema(description = "Wallet to add or Update with Crypto Assets")
          Wallet wallet)
      throws UtilsException, JsonProcessingException {
    return assetService.insertOrUpdateAsset(wallet);
  }

  @PostMapping(value = "/list/addOrUpdate", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      description = "API to add or update Crypto Assets",
      summary = "Add or Update Crypto Assets",
      tags = "Asset")
  @ApiResponse(
      responseCode = "401",
      description = "Invalid JWE",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@invalid-jwe.json")))
  @ApiResponse(
      responseCode = "401",
      description = "Expired JWE",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@expired-jwe.json")))
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public ResponseEntity<Response> saveOrUpdateCryptoAssets(
      @RequestHeader(HttpHeaders.AUTHORIZATION) @Valid @Schema(description = "Authorization Token")
          String authToken,
      @RequestBody @Valid @Schema(description = "Wallets with Crypto Assets to update or save")
          List<Wallet> wallets)
      throws UtilsException, JsonProcessingException {
    return assetService.insertOrUpdateAssets(wallets);
  }
}
