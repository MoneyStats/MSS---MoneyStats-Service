package com.giova.service.moneystats.crypto.asset;

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

  @Query(
      "SELECT new com.giova.service.moneystats.crypto.asset.dto.AssetLivePrice(a.identifier, a.balance, a.wallet.id) FROM AssetEntity a WHERE a.wallet.id IN :walletIds")
  List<AssetLivePrice> findAssetsByWalletIds(@Param("walletIds") List<Long> walletIds);

  @Query("SELECT a FROM AssetEntity a WHERE a.wallet.id IN :walletIds ORDER BY a.rank")
  List<AssetEntity> findAllByWalletIds(@Param("walletIds") List<Long> walletIds);

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

  List<AssetEntity> findAllByUserIdOrderByRank(Long userId);
}
