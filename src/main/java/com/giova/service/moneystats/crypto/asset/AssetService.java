package com.giova.service.moneystats.crypto.asset;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.giova.service.moneystats.app.wallet.WalletService;
import com.giova.service.moneystats.app.wallet.dto.Wallet;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import com.giova.service.moneystats.generic.Response;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.github.giovannilamarmora.utils.interceptors.correlationID.CorrelationIdUtils;
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
}
