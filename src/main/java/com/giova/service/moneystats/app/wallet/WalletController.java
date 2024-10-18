package com.giova.service.moneystats.app.wallet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.giova.service.moneystats.app.wallet.dto.Wallet;
import io.github.giovannilamarmora.utils.exception.UtilsException;
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
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

public interface WalletController {

  /**
   * Api to get all the wallets and relative data
   *
   * @param token User Access Token
   * @param live Getting the live price
   * @param includeHistory Include Full History Stats into data, otherwise only the last stats
   * @param includeAssets Include all Assets into the Wallets, without Operations and History
   * @param includeFullAssets Include all Assets into the Wallets
   * @return List of Wallets
   */
  @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "API to get all wallet", summary = "List of Wallet", tags = "Wallet")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@wallets-full-list.json")))
  @ApiResponse(
      responseCode = "401",
      description = "Invalid Token",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@invalid-token-exception.json")))
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  ResponseEntity<Response> getAllWallet(
      @RequestHeader(HttpHeaders.AUTHORIZATION)
          @Valid
          @Schema(description = "Authorization Token", example = "Bearer eykihugUiOj6bihiguu...")
          String token,
      @RequestParam(value = "live", required = false)
          @Schema(
              description =
                  "Live Price Of the Wallets. For the FrontEnd is Recommended to use this value as Null",
              example = "true")
          Boolean live,
      @RequestParam(value = "includeHistory", required = false, defaultValue = "false")
          @Schema(
              description = "Param to get the full History, if false you get only the last element",
              example = "true")
          Boolean includeHistory,
      @RequestParam(value = "includeAssets", required = false, defaultValue = "false")
          @Schema(
              description = "Param to get the Assets without operation and history",
              example = "true")
          Boolean includeAssets,
      @RequestParam(value = "includeFullAssets", required = false, defaultValue = "false")
          @Schema(
              description = "Param to get the Full Assets with operation and history",
              example = "true")
          Boolean includeFullAssets);

  /**
   * Api to get the wallet and relative data
   *
   * @param token User Access Token
   * @param live Getting the live price
   * @return List of Wallets
   */
  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "API to get the wallet by id", summary = "Wallet by Id", tags = "Wallet")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@wallet-by-id.json")))
  @ApiResponse(
      responseCode = "401",
      description = "Invalid Token",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@invalid-token-exception.json")))
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  ResponseEntity<Response> getWallet(
      @RequestHeader(HttpHeaders.AUTHORIZATION)
          @Valid
          @Schema(description = "Authorization Token", example = "Bearer eykihugUiOj6bihiguu...")
          String token,
      @RequestParam(value = "live", required = false)
          @Schema(
              description =
                  "Live Price Of the Wallets. For the FrontEnd is Recommended to use this value as Null",
              example = "true")
          Boolean live,
      @PathVariable(value = "id")
          @Schema(description = "Id of the Wallet to be searched", example = "1")
          @Valid
          @NotNull(message = "ID is a required field")
          Long id);

  /* OLD DATA */
  @PostMapping(
      value = "/insert-update",
      consumes = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(
      description = "API to insert a wallet",
      summary = "Add or Update Wallet",
      tags = "Wallet")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@add-update-wallet.json")))
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
  ResponseEntity<Response> insertOrUpdateWallet(
      @RequestBody @Valid @Schema(description = "Wallet To Add or Update") Wallet wallet,
      @RequestHeader(HttpHeaders.AUTHORIZATION) @Valid @Schema(description = "Authorization Token")
          String authToken)
      throws UtilsException, JsonProcessingException;

  @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "API to get all wallet", summary = "List of Wallet", tags = "Wallet")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@wallets-full-list.json")))
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
  public ResponseEntity<Response> listWallet(
      @RequestHeader(HttpHeaders.AUTHORIZATION) @Valid @Schema(description = "Authorization Token")
          String authToken,
      @RequestParam(value = "live", required = false, defaultValue = "true")
          @Schema(description = "Live Price")
          Boolean live)
      throws UtilsException;

  @GetMapping(value = "/crypto/list", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      description = "API to get all Crypto wallet",
      summary = "List of Crypto Wallet",
      tags = "Wallet")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@wallets-full-list.json")))
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
  public ResponseEntity<Response> listCryptoWallet(
      @RequestHeader(HttpHeaders.AUTHORIZATION) @Valid @Schema(description = "Authorization Token")
          String authToken,
      @RequestParam(value = "live", required = false, defaultValue = "true")
          @Schema(description = "Live Price")
          Boolean live)
      throws UtilsException;
}
