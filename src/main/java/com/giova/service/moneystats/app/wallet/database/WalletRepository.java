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
  List<WalletEntity> findAllByUserIdentifierWithoutAssetsAndHistory(String userId);

  /**
   * Obtaining the full wallet list with all data
   *
   * @param userId User of the Wallet
   * @return Full Wallet list
   */
  List<WalletEntity> findAllByUserIdentifier(String userId);

  /**
   * Obtaining the Wallet data by ID
   *
   * @param id ID of the wallet to be searched
   * @param userId User ID used for cache
   * @return Full Wallet data
   */
  WalletEntity findWalletEntityById(Long id, String userId);

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
  void deleteAllByUserIdentifier(String userId);

  /**
   * Save all walletEntities
   *
   * @param walletEntities to be saved
   * @return wallet saved
   */
  List<WalletEntity> saveAll(List<WalletEntity> walletEntities);

  /**
   * Find all Wallet Crypto
   *
   * @param userId of the wallet
   * @param category Crypto category default
   * @return Wallet founded
   */
  //List<WalletEntity> findAllByUserIdentifierAndCategory(String userId, String category);
}
