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

  /**
   * Obtaining the Wallet data by ID
   *
   * @param id ID of the wallet to be searched
   * @param userId User ID used for cache
   * @return Full Wallet data
   */
  WalletEntity findWalletEntityById(Long id, Long userId);

  /**
   * Saving the Wallet
   *
   * @param walletEntity To be saved
   * @return Wallet Saved
   */
  WalletEntity save(WalletEntity walletEntity);

  /**
   * Deleting all Data of the user
   *
   * @param userId of the data
   */
  void deleteAllByUserId(Long userId);

  /**
   * Save all walletEntities
   *
   * @param walletEntities to be saved
   * @return wallet saved
   */
  List<WalletEntity> saveAll(List<WalletEntity> walletEntities);
}
