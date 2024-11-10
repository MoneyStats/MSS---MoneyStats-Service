package com.giova.service.moneystats.app.wallet.database;

import com.giova.service.moneystats.app.wallet.entity.WalletEntity;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

public class WalletDAOAdapter implements WalletRepository {
  @Autowired private IWalletDAO iWalletDAO;

  /**
   * Obtain Wallet without Stats and Assets, You Just Got the last Stats as Default
   *
   * @param userId User of the Wallet
   * @return Wallet with only the last Stats
   */
  @Override
  public List<WalletEntity> findAllByUserIdWithoutAssetsAndHistory(Long userId) {
    return iWalletDAO.findAllByUserIdWithoutAssetsAndHistory(userId);
  }

  /**
   * Obtain the Full Wallet list
   *
   * @param userId User of the Wallet
   * @return Wallet with the full data
   */
  @Override
  public List<WalletEntity> findAllByUserId(Long userId) {
    return iWalletDAO.findAllByUserId(userId);
  }

  /**
   * Obtaining the Wallet data by ID
   *
   * @param id ID of the wallet to be searched
   * @param userId User ID used for cache
   * @return Full Wallet data
   */
  @Override
  public WalletEntity findWalletEntityById(Long id, Long userId) {
    return iWalletDAO.findWalletEntityById(id);
  }

  /**
   * Saving the Wallet
   *
   * @param walletEntity To be saved
   * @return Wallet Saved
   */
  @Override
  public WalletEntity save(WalletEntity walletEntity) {
    return iWalletDAO.save(walletEntity);
  }

  /**
   * Deleting all Data of the user
   *
   * @param userId of the data
   */
  @Override
  public void deleteAllByUserId(Long userId) {
    iWalletDAO.deleteAllByUserId(userId);
  }

  /**
   * Save all walletEntities
   *
   * @param walletEntities to be saved
   * @return wallet saved
   */
  @Override
  public List<WalletEntity> saveAll(List<WalletEntity> walletEntities) {
    return iWalletDAO.saveAll(walletEntities);
  }

  /**
   * Find all Wallet Crypto
   *
   * @param userId of the wallet
   * @param category Crypto category default
   * @return Wallet founded
   */
  @Override
  public List<WalletEntity> findAllByUserIdAndCategory(Long userId, String category) {
    return iWalletDAO.findAllByUserIdAndCategory(userId, category);
  }
}
