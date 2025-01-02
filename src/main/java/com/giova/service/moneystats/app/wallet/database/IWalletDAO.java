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
          + "WHERE w.userIdentifier = :userId "
          + "AND (h.date IS NULL OR h.date = "
          + "(SELECT MAX(h2.date) FROM StatsEntity h2 WHERE h2.wallet.id = w.id)) "
          + "ORDER BY w.id")
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
   * @return Full Wallet data
   */
  WalletEntity findWalletEntityById(Long id);

  /**
   * Deleting all Data of the user
   *
   * @param userId of the data
   */
  void deleteAllByUserIdentifier(String userId);

  /* OLD QUERY */
  //List<WalletEntity> findAllByUserIdentifierAndCategory(String userId, String category);
}
