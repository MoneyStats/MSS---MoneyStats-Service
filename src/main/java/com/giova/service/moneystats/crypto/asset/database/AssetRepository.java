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
   * @return Assets with only the identifier, balance and wallet id
   */
  List<AssetLivePrice> findAssetsByWalletIds(List<Long> walletIds);

  /**
   * Used to get the full asset list, included with operations and histories
   *
   * @param walletIds param of the wallet id to be searched
   * @return Full asset list
   */
  List<AssetEntity> findAllByWalletIds(List<Long> walletIds);

  /**
   * Used to get the Asset list without operation and history
   *
   * @param walletIds param of the wallet id to be searched
   * @return Assets list without operations and histories
   */
  List<AssetWithoutOpAndStats> findAllAssetsByWalletIds(List<Long> walletIds);
}
