package com.giova.service.moneystats.crypto.asset;

import com.giova.service.moneystats.app.wallet.dto.Wallet;
import io.github.giovannilamarmora.utils.exception.dto.ExceptionResponse;
import io.github.giovannilamarmora.utils.generic.Response;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

public interface AssetController {

  /**
   * API to get the full list of the Asset
   *
   * @param token Access token of the user
   * @return Asset List
   */
  @GetMapping(value = "/assets", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      description = "API to get all Crypto Assets",
      summary = "Crypto Assets",
      tags = "Asset")
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
      description = "Invalid Token",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@invalid-token-exception.json")))
  ResponseEntity<Response> getAllAssets(
      @RequestHeader(HttpHeaders.AUTHORIZATION)
          @Valid
          @Schema(description = "Authorization Token", example = "Bearer eykihugUiOj6bihiguu...")
          String token);

  /**
   * API to get an Asset by his identifier
   *
   * @param token Access token of the user
   * @param identifier to be searched
   * @return Asset
   */
  @GetMapping(value = "/assets/{identifier}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      description = "API to get Crypto Asset by identifier",
      summary = "Crypto Asset By Identifier",
      tags = "Asset")
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
      description = "Invalid Token",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@invalid-token-exception.json")))
  ResponseEntity<Response> getAssetByIdentifier(
      @RequestHeader(HttpHeaders.AUTHORIZATION)
          @Valid
          @Schema(description = "Authorization Token", example = "Bearer eykihugUiOj6bihiguu...")
          String token,
      @PathVariable(value = "identifier")
          @Valid
          @NotBlank(message = "Insert a valid identifier")
          @Schema(description = "Identifier", example = "bitcoin")
          String identifier);

  /**
   * API To add a crypto asset
   *
   * @param token Of the user
   * @param wallet Used to update data of the wallets
   * @return Wallet Updated
   */
  @PostMapping(value = "/assets", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "API to add Crypto Asset", summary = "Add Crypto Asset", tags = "Asset")
  @ApiResponse(
      responseCode = "401",
      description = "Invalid Token",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@invalid-token-exception.json")))
  ResponseEntity<Response> addCryptoAsset(
      @RequestHeader(HttpHeaders.AUTHORIZATION)
          @Valid
          @Schema(description = "Authorization Token", example = "Bearer eykihugUiOj6bihiguu...")
          String token,
      @RequestBody
          @Valid
          @Schema(
              description =
                  "Wallet to add a Crypto Assets, used because it needs to update the data of the Wallet")
          Wallet wallet);

  /**
   * API To update a crypto asset
   *
   * @param token Of the user
   * @param wallet Used to update data of the wallets
   * @return Wallet Updated
   */
  @PutMapping(value = "/assets", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      description = "API to update Crypto Asset",
      summary = "Update Crypto Asset",
      tags = "Asset")
  @ApiResponse(
      responseCode = "401",
      description = "Invalid Token",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@invalid-token-exception.json")))
  ResponseEntity<Response> updateCryptoAsset(
      @RequestHeader(HttpHeaders.AUTHORIZATION)
          @Valid
          @Schema(description = "Authorization Token", example = "Bearer eykihugUiOj6bihiguu...")
          String token,
      @RequestBody
          @Valid
          @Schema(
              description =
                  "Wallet to Update a Crypto Assets, used because it needs to update the data of the Wallet")
          Wallet wallet);

  /**
   * API to add a list of crypto assets
   *
   * @param token Of the user
   * @param wallets Used to update data of the wallets
   * @return Wallet Updated
   */
  @PostMapping(value = "/assets/list", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      description = "API to add Crypto Assets",
      summary = "Add Crypto Assets",
      tags = "Asset")
  @ApiResponse(
      responseCode = "401",
      description = "Invalid Token",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@invalid-token-exception.json")))
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  ResponseEntity<Response> addCryptoAssets(
      @RequestHeader(HttpHeaders.AUTHORIZATION)
          @Valid
          @Schema(description = "Authorization Token", example = "Bearer eykihugUiOj6bihiguu...")
          String token,
      @RequestBody @Valid @Schema(description = "Wallets with Crypto Assets to be saved")
          List<Wallet> wallets);

  /**
   * API to update a list of crypto assets
   *
   * @param token Of the user
   * @param wallets Used to update data of the wallets
   * @return Wallet Updated
   */
  @PutMapping(value = "/assets/list", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      description = "API to add or update Crypto Assets",
      summary = "Add or Update Crypto Assets",
      tags = "Asset")
  @ApiResponse(
      responseCode = "401",
      description = "Invalid Token",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@invalid-token-exception.json")))
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  ResponseEntity<Response> updateCryptoAssets(
      @RequestHeader(HttpHeaders.AUTHORIZATION)
          @Valid
          @Schema(description = "Authorization Token", example = "Bearer eykihugUiOj6bihiguu...")
          String token,
      @RequestBody @Valid @Schema(description = "Wallets with Crypto Assets to update")
          List<Wallet> wallets);
}
