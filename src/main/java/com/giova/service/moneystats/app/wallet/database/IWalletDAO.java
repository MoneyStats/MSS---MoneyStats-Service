package com.giova.service.moneystats.app.wallet.database;

import com.giova.service.moneystats.app.wallet.entity.WalletEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface IWalletDAO extends JpaRepository<WalletEntity, Long> {

  /**
   * Obtain Wallet without Stats and Assets, You Just Got the last Stats as Default
   *
   * @param userId User of the Wallet
   * @return Wallet with only the last Stats
   */
  @Query(
      "SELECT w FROM WalletEntity w "
          + "LEFT JOIN FETCH w.history h "
          + "WHERE w.user.id = :userId "
          + "AND (h.date IS NULL OR h.date = "
          + "(SELECT MAX(h2.date) FROM StatsEntity h2 WHERE h2.wallet.id = w.id)) "
          + "ORDER BY w.id")
  List<WalletEntity> findAllByUserIdWithoutAssetsAndHistory(Long userId);

  /**
   * Obtaining the full wallet list with all data
   *
   * @param userId User of the Wallet
   * @return Full Wallet list
   */
  List<WalletEntity> findAllByUserId(Long userId);

  /* OLD QUERY */
  List<WalletEntity> findAllByUserIdAndCategory(Long userId, String category);

  WalletEntity findWalletEntityById(Long id);

  void deleteAllByUserId(Long userId);
}
