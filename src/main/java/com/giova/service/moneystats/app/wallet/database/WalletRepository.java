package com.giova.service.moneystats.app.wallet.database;

import com.giova.service.moneystats.app.wallet.entity.WalletEntity;
import java.util.List;

public interface WalletRepository {
  /**
   * Obtain Wallet without Stats and Assets, You Just Got the last Stats as Default
   *
   * @param userId User of the Wallet
   * @return Wallet with only the last Stats
   */
  List<WalletEntity> findAllByUserIdWithoutAssetsAndHistory(Long userId);

  /**
   * Obtaining the full wallet list with all data
   *
   * @param userId User of the Wallet
   * @return Full Wallet list
   */
  List<WalletEntity> findAllByUserId(Long userId);
}
