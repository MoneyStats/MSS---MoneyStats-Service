package com.giova.service.moneystats.app.wallet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.giova.service.moneystats.app.wallet.dto.Wallet;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.generic.Response;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Logged
@RestController
@RequestMapping("/v1/wallet")
@CrossOrigin(origins = "*")
@Tag(name = "Wallet", description = "API for wallet")
public class WalletControllerImpl implements WalletController {

  @Autowired private WalletService walletService;

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
  @Override
  public ResponseEntity<Response> getAllWallet(
      String token,
      Boolean live,
      Boolean includeHistory,
      Boolean includeAssets,
      Boolean includeFullAssets) {
    return walletService.getAllWallets(live, includeHistory, includeAssets, includeFullAssets);
  }

  /**
   * Api to get the wallet and relative data
   *
   * @param token User Access Token
   * @param live Getting the live price
   * @param id
   * @return List of Wallets
   */
  @Override
  public ResponseEntity<Response> getWallet(String token, Boolean live, Long id) {
    return walletService.getWalletById(live, id);
  }

  /* OLD DATA */
  @Override
  public ResponseEntity<Response> insertOrUpdateWallet(
      @RequestBody @Valid @Schema(description = "Wallet To Add or Update") Wallet wallet,
      @RequestHeader(HttpHeaders.AUTHORIZATION) @Valid @Schema(description = "Authorization Token")
          String authToken)
      throws UtilsException, JsonProcessingException {
    return walletService.insertOrUpdateWallet(wallet);
  }

  @Override
  public ResponseEntity<Response> listWallet(
      @RequestHeader(HttpHeaders.AUTHORIZATION) @Valid @Schema(description = "Authorization Token")
          String authToken,
      @RequestParam(value = "live", required = false, defaultValue = "true")
          @Schema(description = "Live Price")
          Boolean live)
      throws UtilsException {
    return walletService.getWallets();
  }

  @Override
  public ResponseEntity<Response> listCryptoWallet(
      @RequestHeader(HttpHeaders.AUTHORIZATION) @Valid @Schema(description = "Authorization Token")
          String authToken,
      @RequestParam(value = "live", required = false, defaultValue = "true")
          @Schema(description = "Live Price")
          Boolean live)
      throws UtilsException {
    return walletService.getCryptoWallets(live);
  }
}
