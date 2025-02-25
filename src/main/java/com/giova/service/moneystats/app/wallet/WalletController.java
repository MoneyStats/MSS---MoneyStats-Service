package com.giova.service.moneystats.app.wallet;

import com.giova.service.moneystats.app.wallet.dto.Wallet;
import io.github.giovannilamarmora.utils.exception.dto.ExceptionResponse;
import io.github.giovannilamarmora.utils.generic.Response;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
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
  @GetMapping(value = "/wallets", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "API to get all wallet", summary = "List of Wallet", tags = "Wallet")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE /*,
              examples = @ExampleObject(value = "@wallets-full-list_response.json")*/))
  @ApiResponse(
      responseCode = "401",
      description = "Invalid Token",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE /*,
              examples = @ExampleObject(value = "@invalid-token-exception.json")*/))
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
  @GetMapping(value = "/wallets/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "API to get the wallet by id", summary = "Wallet by Id", tags = "Wallet")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE /*,
              examples = @ExampleObject(value = "@wallet-by-id_response.json")*/))
  @ApiResponse(
      responseCode = "401",
      description = "Invalid Token",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE /*,
              examples = @ExampleObject(value = "@invalid-token-exception.json")*/))
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

  /**
   * API to add a new Wallet
   *
   * @param wallet Valid Wallet to be added
   * @param token User Access Token
   * @return The Wallet added
   */
  @PostMapping(
      value = "/wallets",
      consumes = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(description = "API to insert a wallet", summary = "Add Wallet", tags = "Wallet")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE /*,
              examples = @ExampleObject(value = "@add-wallet_response.json")*/))
  @ApiResponse(
      responseCode = "401",
      description = "Invalid Token",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE /*,
              examples = @ExampleObject(value = "@invalid-token-exception.json")*/))
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  ResponseEntity<Response> addWallet(
      @RequestBody @Valid @Schema(description = "Wallet To Add", implementation = Wallet.class)
          Wallet wallet,
      @RequestHeader(HttpHeaders.AUTHORIZATION)
          @Valid
          @Schema(description = "Authorization Token", example = "Bearer eykihugUiOj6bihiguu...")
          String token);

  /**
   * API to update a Wallet
   *
   * @param wallet Valid Wallet to be updated
   * @param token User Access Token
   * @return The Wallet update
   */
  @PutMapping(
      value = "/wallets",
      consumes = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(description = "API to update a wallet", summary = "Update Wallet", tags = "Wallet")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE /*,
              examples = @ExampleObject(value = "@update-wallet_response.json")*/))
  @ApiResponse(
      responseCode = "401",
      description = "Invalid Token",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE /*,
              examples = @ExampleObject(value = "@invalid-token-exception.json")*/))
  ResponseEntity<Response> updateWallet(
      @RequestBody @Valid @Schema(description = "Wallet To Update", implementation = Wallet.class)
          Wallet wallet,
      @RequestParam(value = "live", required = false)
          @Schema(
              description =
                  "Live Price Of the Wallets. For the FrontEnd is Recommended to use this value as Null",
              example = "true")
          Boolean live,
      @RequestHeader(HttpHeaders.AUTHORIZATION)
          @Valid
          @Schema(description = "Authorization Token", example = "Bearer eykihugUiOj6bihiguu...")
          String token);

  /**
   * API to delete a Wallet
   *
   * @param id Valid Wallet to be deleted
   * @param token User Access Token
   * @return The Wallet deleted
   */
  @DeleteMapping(
      value = "/wallets/{id}",
      consumes = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(description = "API to delete a wallet", summary = "Delete Wallet", tags = "Wallet")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE /*,
              examples = @ExampleObject(value = "@delete-wallet_response.json")*/))
  @ApiResponse(
      responseCode = "401",
      description = "Invalid Token",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE /*,
              examples = @ExampleObject(value = "@invalid-token-exception.json")*/))
  ResponseEntity<Response> deleteWallet(
      @PathVariable(value = "id")
          @Schema(description = "Id of the Wallet to be deleted", example = "1")
          @Valid
          @NotNull(message = "ID is a required field")
          Long id,
      @RequestHeader(HttpHeaders.AUTHORIZATION)
          @Valid
          @Schema(description = "Authorization Token", example = "Bearer eykihugUiOj6bihiguu...")
          String token);

  /**
   * Getting all Crypto Wallets
   *
   * @param token of the user
   * @param live price data
   * @return Wallet Response
   */
  @GetMapping(value = "/wallets/crypto", produces = MediaType.APPLICATION_JSON_VALUE)
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
              mediaType = MediaType.APPLICATION_JSON_VALUE /*,
              examples = @ExampleObject(value = "@wallets-full-list_response.json")*/))
  @ApiResponse(
      responseCode = "401",
      description = "Invalid Token",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE/*,
              examples = @ExampleObject(value = "@invalid-token-exception.json")*/))
  ResponseEntity<Response> listCryptoWallet(
      @RequestHeader(HttpHeaders.AUTHORIZATION)
          @Valid
          @Schema(description = "Authorization Token", example = "Bearer eykihugUiOj6bihiguu...")
          String token,
      @RequestParam(value = "live", required = false)
          @Schema(description = "Live Price", example = "true")
          Boolean live);
}
