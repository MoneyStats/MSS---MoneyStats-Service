package com.giova.service.moneystats.crypto.asset.database;

import com.giova.service.moneystats.crypto.asset.dto.AssetLivePrice;
import com.giova.service.moneystats.crypto.asset.dto.AssetWithoutOpAndStats;
import com.giova.service.moneystats.crypto.asset.entity.AssetEntity;

import java.util.List;

public class AssetCacheService implements AssetRepository {
  /**
   * Getting only the identifier, balance and wallet id, used to get the live price of the wallet
   *
   * @param walletIds list of the wallet id
   * @return Assets with only the identifier, balance and wallet id
   */
  @Override
  public List<AssetLivePrice> findAssetsByWalletIds(List<Long> walletIds) {
    return List.of();
  }

  /**
   * Used to get the full asset list, included with operations and histories
   *
   * @param walletIds param of the wallet id to be searched
   * @return Full asset list
   */
  @Override
  public List<AssetEntity> findAllByWalletIds(List<Long> walletIds) {
    return List.of();
  }

  /**
   * Used to get the Asset list without operation and history
   *
   * @param walletIds param of the wallet id to be searched
   * @return Assets list without operations and histories
   */
  @Override
  public List<AssetWithoutOpAndStats> findAllAssetsByWalletIds(List<Long> walletIds) {
    return List.of();
  }
}
