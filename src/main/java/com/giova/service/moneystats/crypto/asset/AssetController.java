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
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Logged
@RestController
@RequestMapping("/v1/crypto/asset")
@CrossOrigin(origins = "*")
@Tag(name = "Asset", description = "API Crypto Asset")
public class AssetController {
  @Autowired private AssetService assetService;

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

  @GetMapping(value = "/getAll", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "API to get Crypto Assets", summary = "Crypto Assets", tags = "Asset")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@assets.json")))
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
  public ResponseEntity<Response> getAllAssets(
      @RequestHeader(HttpHeaders.AUTHORIZATION) @Valid @Schema(description = "Authorization Token")
          String authToken)
      throws UtilsException {
    return assetService.getAssets();
  }

  @GetMapping(value = "/get", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "API to get Crypto Asset", summary = "Crypto Asset", tags = "Asset")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@asset.json")))
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
  public ResponseEntity<Response> getAsset(
      @RequestHeader(HttpHeaders.AUTHORIZATION) @Valid @Schema(description = "Authorization Token")
          String authToken,
      @RequestParam(value = "identifier")
          @Valid
          @NotNull
          @NotEmpty
          @NotBlank
          @Schema(description = "Identifier", example = "bitcoin")
          String identifier)
      throws UtilsException {
    return assetService.getAsset(identifier);
  }
}
