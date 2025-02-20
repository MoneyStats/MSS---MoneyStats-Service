package com.giova.service.moneystats.crypto.asset.database;

import com.giova.service.moneystats.crypto.asset.dto.AssetLivePrice;
import com.giova.service.moneystats.crypto.asset.dto.AssetWithoutOpAndStats;
import com.giova.service.moneystats.crypto.asset.entity.AssetEntity;
import java.util.List;

public interface AssetRepository {
  /**
   * Getting only the identifier, balance and wallet id, used to get the live price of the wallet
   *
   * @param walletIds list of the wallet id
   * @param userId User ID used for cache
   * @return Assets with only the identifier, balance and wallet id
   */
  List<AssetLivePrice> findAssetsByWalletIds(List<Long> walletIds, String userId);

  /**
   * Used to get the full asset list, included with operations and histories
   *
   * @param walletIds param of the wallet id to be searched
   * @param userId User ID used for cache
   * @return Full asset list
   */
  List<AssetEntity> findAllByWalletIds(List<Long> walletIds, String userId);

  /**
   * Used to get the Asset list without operation and history
   *
   * @param walletIds param of the wallet id to be searched
   * @param userId User ID used for cache
   * @return Assets list without operations and histories
   */
  List<AssetWithoutOpAndStats> findAllAssetsByWalletIds(List<Long> walletIds, String userId);

  /**
   * Query to find an asset by his identifier and the user id
   *
   * @param identifier to be searched
   * @param userId o the user
   * @return AssetEntities
   */
  List<AssetEntity> findAllByIdentifierAndUserId(String identifier, String userId);

  /**
   * Query to find all asset
   *
   * @param userId o the user
   * @return AssetEntities
   */
  List<AssetEntity> findAllByUserIdOrderByRank(String userId);

  /** Delete All Cache */
  void clearAllAssetsCache();
}
