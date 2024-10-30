package com.giova.service.moneystats.crypto.asset.database;

import com.giova.service.moneystats.crypto.asset.dto.AssetLivePrice;
import com.giova.service.moneystats.crypto.asset.dto.AssetWithoutOpAndStats;
import com.giova.service.moneystats.crypto.asset.entity.AssetEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IAssetDAO extends JpaRepository<AssetEntity, Long> {

  /**
   * Getting only the identifier, balance and wallet id, used to get the live price of the wallet
   *
   * @param walletIds list of the wallet id
   * @return Assets with only the identifier, balance and wallet id
   */
  @Query(
      "SELECT new com.giova.service.moneystats.crypto.asset.dto.AssetLivePrice(a.identifier, a.balance, a.wallet.id) FROM AssetEntity a WHERE a.wallet.id IN :walletIds")
  List<AssetLivePrice> findAssetsByWalletIds(@Param("walletIds") List<Long> walletIds);

  /**
   * Used to get the full asset list, included with operations and histories
   *
   * @param walletIds param of the wallet id to be searched
   * @return Full asset list
   */
  @Query("SELECT a FROM AssetEntity a WHERE a.wallet.id IN :walletIds ORDER BY a.rank")
  List<AssetEntity> findAllByWalletIds(@Param("walletIds") List<Long> walletIds);

  /**
   * Used to get the Asset list without operation and history
   *
   * @param walletIds param of the wallet id to be searched
   * @return Assets list without operations and histories
   */
  @Query(
      "SELECT new com.giova.service.moneystats.crypto.asset.dto.AssetWithoutOpAndStats("
          + "a.id, a.creationDate, a.updateDate, a.deletedDate, "
          + "a.identifier, a.name, a.category, a.symbol, a.rank, a.icon, "
          + "a.balance, a.invested, a.lastUpdate, a.performance, "
          + "a.trend, a.wallet.id) "
          + "FROM AssetEntity a "
          + "WHERE a.wallet.id IN :walletIds "
          + "ORDER BY a.rank")
  List<AssetWithoutOpAndStats> findAllAssetsByWalletIds(@Param("walletIds") List<Long> walletIds);

  /**
   * Query to find an asset by his identifier and the user id
   *
   * @param identifier to be searched
   * @param userId o the user
   * @return AssetEntities
   */
  List<AssetEntity> findAllByIdentifierAndUserId(String identifier, Long userId);

  /**
   * Query to find all asset
   *
   * @param userId o the user
   * @return AssetEntities
   */
  List<AssetEntity> findAllByUserIdOrderByRank(Long userId);
}
