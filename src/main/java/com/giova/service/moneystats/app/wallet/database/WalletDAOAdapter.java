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
}
