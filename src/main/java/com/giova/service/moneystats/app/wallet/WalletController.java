package com.giova.service.moneystats.app.wallet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.giova.service.moneystats.app.wallet.dto.Wallet;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.generic.Response;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Logged
@RestController
@RequestMapping("/v1/wallet")
@CrossOrigin(origins = "*")
public class WalletController {

  @Autowired private WalletService walletService;

  @PostMapping(
      value = "/insert-update",
      consumes = {MediaType.APPLICATION_JSON_VALUE})
  @Tag(name = "Wallet", description = "API to insert a wallet")
  @Operation(description = "API to insert a wallet", tags = "Wallet")
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public ResponseEntity<Response> insertOrUpdateWallet(
      @RequestBody @Valid Wallet wallet, @RequestHeader(HttpHeaders.AUTHORIZATION) String authToken)
      throws UtilsException, JsonProcessingException {
    return walletService.insertOrUpdateWallet(wallet);
  }

  @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
  @Tag(name = "Wallet", description = "API to get all wallet")
  @Operation(description = "API to get all wallet", tags = "Wallet")
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public ResponseEntity<Response> listWallet(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authToken,
      @RequestParam(value = "live", required = false, defaultValue = "true") Boolean live)
      throws UtilsException {
    return walletService.getWallets();
  }

  @GetMapping(value = "/crypto/list", produces = MediaType.APPLICATION_JSON_VALUE)
  @Tag(name = "Wallet", description = "API to get all Crypto wallet")
  @Operation(description = "API to get all Crypto wallet", tags = "Wallet")
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public ResponseEntity<Response> listCryptoWallet(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authToken,
      @RequestParam(value = "live", required = false, defaultValue = "true") Boolean live)
      throws UtilsException {
    return walletService.getCryptoWallets(live);
  }
}
