package com.giova.service.moneystats.app.wallet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.giova.service.moneystats.app.attachments.ImageService;
import com.giova.service.moneystats.app.attachments.dto.Image;
import com.giova.service.moneystats.app.stats.StatsService;
import com.giova.service.moneystats.app.wallet.database.IWalletDAO;
import com.giova.service.moneystats.app.wallet.database.WalletCacheService;
import com.giova.service.moneystats.app.wallet.dto.Wallet;
import com.giova.service.moneystats.app.wallet.entity.WalletEntity;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import com.giova.service.moneystats.settings.dto.Status;
import io.github.giovannilamarmora.utils.context.TraceUtils;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.generic.Response;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
@Logged
@AllArgsConstructor
public class WalletService {
  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  private final UserEntity user;
  @Autowired private WalletCacheService walletCacheService;
  @Autowired private IWalletDAO walletDAO;
  @Autowired private WalletMapper walletMapper;
  @Autowired private ImageService imageService;
  @Autowired private StatsService statsService;

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public ResponseEntity<Response> getAllWallets(
      Boolean live, Boolean includeHistory, Boolean includeAssets) throws UtilsException {
    /**
     * We give the priority to the param "Boolean live", if the param is null we check the User
     * Setting if it has the live wallet status ACTIVE. For the FrontEnd is Recommended to use this
     * value as Null
     */
    Boolean isLiveWallet =
        !ObjectUtils.isEmpty(live)
            ? live
            : !ObjectUtils.isEmpty(user.getSettings().getLiveWallets())
                && user.getSettings().getLiveWallets().equalsIgnoreCase(Status.ACTIVE.toString());

    List<WalletEntity> walletEntity;
    // TODO: Collega includeHistory
    if (!includeHistory)
      walletEntity = walletDAO.findAllByUserIdWithoutAssetsAndHistory(user.getId());
    else walletEntity = walletCacheService.findAllByUserId(user.getId());

    // TODO: findAllByUserId Non ritorna gli Asset per la nuova impostazione ma bisogna controllare
    // in caso di Cache Attiva come si comporta
    // TODO: Collegare Redis Cache e rimuovere l'attuale cache
    // List<WalletEntity> walletEntity = walletDAO.findAllByUserId(user.getId());

    // List<WalletEntity> walletEntity = walletCacheService.findAllByUserId(user.getId());

    String message = "";
    if (walletEntity.isEmpty()) {
      message = "Wallet Empty, insert new Wallet to get it!";
    } else {
      message = "Found " + walletEntity.size() + " Wallets";
    }

    List<LocalDate> getAllCryptoDates = statsService.getCryptoDistinctDates(user);
    List<Wallet> walletToReturn =
        walletMapper.fromWalletEntitiesToWalletList(
            walletEntity, isLiveWallet, includeAssets, getAllCryptoDates);

    Response response =
        new Response(HttpStatus.OK.value(), message, TraceUtils.getSpanID(), walletToReturn);
    return ResponseEntity.ok(response);
  }

  /* OLD DATA */
  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = Exception.class)
  public ResponseEntity<Response> insertOrUpdateWallet(Wallet wallet)
      throws UtilsException, JsonProcessingException {
    Boolean isLiveWallet =
        user.getSettings().getLiveWallets() != null
            && user.getSettings().getLiveWallets().equalsIgnoreCase(Status.ACTIVE.toString());
    return insertOrUpdate(wallet, isLiveWallet);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = Exception.class)
  public ResponseEntity<Response> notFilterInsertOrUpdateWallet(Wallet wallet)
      throws UtilsException, JsonProcessingException {
    return insertOrUpdate(wallet, false);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = Exception.class)
  public ResponseEntity<Response> insertOrUpdate(Wallet wallet, Boolean isWalletLive)
      throws UtilsException, JsonProcessingException {
    WalletEntity walletEntity = walletMapper.fromWalletToWalletEntity(wallet, user);

    if (wallet.getId() != null && isWalletLive) {
      WalletEntity getFromDB = walletCacheService.findWalletEntityById(wallet.getId());
      if (getFromDB != null) {
        walletEntity.setBalance(getFromDB.getBalance());
        walletEntity.setPerformanceLastStats(getFromDB.getPerformanceLastStats());
        walletEntity.setDifferenceLastStats(getFromDB.getDifferenceLastStats());
        walletEntity.setHighPrice(getFromDB.getHighPrice());
        walletEntity.setHighPriceDate(getFromDB.getHighPriceDate());
        walletEntity.setLowPrice(getFromDB.getLowPrice());
        walletEntity.setLowPriceDate(getFromDB.getLowPriceDate());
        walletEntity.setAllTimeHigh(getFromDB.getAllTimeHigh());
        walletEntity.setAllTimeHighDate(getFromDB.getAllTimeHighDate());
      }
    }

    if (wallet.getImgName() != null && !wallet.getImgName().isEmpty()) {
      LOG.info("Building attachment with filename {}", wallet.getImgName());
      Image image = imageService.getAttachment(wallet.getImgName());
      imageService.removeAttachment(wallet.getImgName());
      walletEntity.setImg(
          "data:"
              + image.getContentType()
              + ";base64,"
              + Base64.getEncoder().encodeToString(image.getBody()));
    }

    WalletEntity saved = walletCacheService.save(walletEntity);

    // List<LocalDate> getAllCryptoDates = statsService.getCryptoDistinctDates(user);
    Wallet walletToReturn = walletMapper.fromWalletEntityToWallet(saved, null);
    // if (wallet.getHistory() != null && !wallet.getHistory().isEmpty()) {
    //  walletToReturn.setHistory(statsService.saveStats(wallet.getHistory(), saved, user));
    // }

    String message = "Wallet " + walletToReturn.getName() + " Successfully saved!";

    Response response =
        new Response(HttpStatus.OK.value(), message, TraceUtils.getSpanID(), walletToReturn);
    return ResponseEntity.ok(response);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public ResponseEntity<Response> getWallets() throws UtilsException {
    List<WalletEntity> walletEntity = walletCacheService.findAllByUserId(user.getId());

    String message = "";
    if (walletEntity.isEmpty()) {
      message = "Wallet Empty, insert new Wallet to get it!";
    } else {
      message = "Found " + walletEntity.size() + " Wallets";
    }
    Boolean isLiveWallet =
        user.getSettings().getLiveWallets() != null
            && user.getSettings().getLiveWallets().equalsIgnoreCase(Status.ACTIVE.toString());

    List<LocalDate> getAllCryptoDates = statsService.getCryptoDistinctDates(user);
    List<Wallet> walletToReturn =
        walletMapper.fromWalletEntitiesToWallets(walletEntity, isLiveWallet, getAllCryptoDates);

    Response response =
        new Response(HttpStatus.OK.value(), message, TraceUtils.getSpanID(), walletToReturn);
    return ResponseEntity.ok(response);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public ResponseEntity<Response> getCryptoWallets(Boolean live) throws UtilsException {
    // UserEntity user = authService.checkLogin(authToken);
    String CRYPTO = "Crypto";

    List<WalletEntity> walletEntity =
        walletCacheService.findAllByUserIdAndCategory(user.getId(), CRYPTO);

    String message = "";
    if (walletEntity.isEmpty()) {
      message = "Crypto Wallet Empty, insert new Crypto Wallet to get it!";
    } else {
      message = "Found " + walletEntity.size() + " Crypto Wallets";
    }

    List<LocalDate> getAllCryptoDates = statsService.getCryptoDistinctDates(user);
    List<Wallet> walletToReturn =
        walletMapper.fromWalletEntitiesToWallets(walletEntity, live, getAllCryptoDates);

    Response response =
        new Response(HttpStatus.OK.value(), message, TraceUtils.getSpanID(), walletToReturn);
    return ResponseEntity.ok(response);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public List<Wallet> deleteWalletIds(List<Wallet> wallets) {
    return walletMapper.deleteWalletIds(wallets);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public List<Wallet> saveWalletEntities(List<Wallet> wallets) {

    List<WalletEntity> walletEntities =
        wallets.stream()
            .map(
                wallet -> {
                  WalletEntity walletEntity = walletMapper.fromWalletToWalletEntity(wallet, user);

                  if (wallet.getImgName() != null && !wallet.getImgName().isEmpty()) {
                    LOG.info("Building attachment with filename {}", wallet.getImgName());
                    Image image = imageService.getAttachment(wallet.getImgName());
                    imageService.removeAttachment(wallet.getImgName());
                    walletEntity.setImg(
                        "data:"
                            + image.getContentType()
                            + ";base64,"
                            + Base64.getEncoder().encodeToString(image.getBody()));
                  }
                  return walletEntity;
                })
            .collect(Collectors.toList());

    List<WalletEntity> saved = walletCacheService.saveAll(walletEntities);

    return saved.stream()
        .map(
            walletEntity -> {
              Wallet wallet = new Wallet();
              try {
                walletMapper.fromWalletEntityToWallet(walletEntity, null);
              } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
              }

              return wallet;
            })
        .collect(Collectors.toList());
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public void deleteWalletEntities() {
    walletCacheService.deleteAllByUserId(user.getId());
  }
}
