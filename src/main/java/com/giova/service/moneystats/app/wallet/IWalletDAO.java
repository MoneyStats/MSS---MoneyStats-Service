package com.giova.service.moneystats.app.wallet;

import com.giova.service.moneystats.app.wallet.entity.WalletEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface IWalletDAO extends JpaRepository<WalletEntity, Long> {

  /**
   * Obtain Wallet without Stats and Assets
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

  // Per ottenere i Wallet con tutti gli asset e senza history
  @Query("SELECT w FROM WalletEntity w " + "LEFT JOIN FETCH w.assets a WHERE w.user.id = :userId")
  List<WalletEntity> findAllByUserIdWithAssets(Long userId);

  // Per ottenere i Wallet con tutti gli asset e solo l'ultima history
  @Query(
      "SELECT w FROM WalletEntity w "
          + "LEFT JOIN FETCH w.assets a "
          + "LEFT JOIN FETCH w.history h "
          + "WHERE w.user.id = :userId AND h.date = "
          + "(SELECT MAX(h2.date) FROM StatsEntity h2 WHERE h2.wallet.id = w.id)")
  List<WalletEntity> findAllByUserIdWithAssetsAndLastHistory(Long userId);

  /* OLD QUERY */
  List<WalletEntity> findAllByUserId(Long userId);

  List<WalletEntity> findAllByUserIdAndCategory(Long userId, String category);

  WalletEntity findWalletEntityById(Long id);

  void deleteAllByUserId(Long userId);
}
