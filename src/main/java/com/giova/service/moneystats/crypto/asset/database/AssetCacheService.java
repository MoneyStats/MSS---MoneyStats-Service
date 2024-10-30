package com.giova.service.moneystats.crypto.asset.database;

import com.giova.service.moneystats.config.cache.RedisCacheConfig;
import com.giova.service.moneystats.crypto.asset.dto.AssetLivePrice;
import com.giova.service.moneystats.crypto.asset.dto.AssetWithoutOpAndStats;
import com.giova.service.moneystats.crypto.asset.entity.AssetEntity;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class AssetCacheService implements AssetRepository {

  private static final String CACHE_ASSETS_BY_IDENTIFIER = "_assets_identifier_";
  private static final String CACHE_ALL_ASSETS_BY_USER = "_assets_by_user";
  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  @Autowired private RedisTemplate<String, List<AssetEntity>> assetEntityTemplate;
  @Autowired private RedisTemplate<String, List<AssetLivePrice>> assetLivePriceTemplate;

  @Autowired
  private RedisTemplate<String, List<AssetWithoutOpAndStats>> assetWithoutOpAndStatsTemplate;

  @Autowired private IAssetDAO assetDAO;

  /**
   * Getting only the identifier, balance and wallet id, used to get the live price of the wallet
   *
   * @param walletIds list of the wallet id
   * @param userId User ID used for cache
   * @return Assets with only the identifier, balance and wallet id
   */
  @Override
  public List<AssetLivePrice> findAssetsByWalletIds(List<Long> walletIds, Long userId) {
    String cacheKey = userId + "_assets_live_price_list";
    try {
      return Optional.ofNullable(assetLivePriceTemplate.opsForValue().get(cacheKey))
          .orElseGet(
              () -> {
                LOG.info("[Caching] Assets Live Price into Database for userId {}", userId);

                return Optional.ofNullable(assetDAO.findAssetsByWalletIds(walletIds))
                    .map(
                        assetEntities -> {
                          if (!assetEntities.isEmpty())
                            assetLivePriceTemplate.opsForValue().set(cacheKey, assetEntities);
                          return assetEntities;
                        })
                    .orElse(Collections.emptyList());
              });
    } catch (Exception e) {
      LOG.error(RedisCacheConfig.REDIS_ERROR_LOG, e.getMessage());
      return assetDAO.findAssetsByWalletIds(walletIds);
    }
  }

  /**
   * Used to get the full asset list, included with operations and histories
   *
   * @param walletIds param of the wallet id to be searched
   * @param userId User ID used for cache
   * @return Full asset list
   */
  @Override
  public List<AssetEntity> findAllByWalletIds(List<Long> walletIds, Long userId) {
    String cacheKey = userId + "_assets_full_list_by_wallet_ids";
    try {
      return Optional.ofNullable(assetEntityTemplate.opsForValue().get(cacheKey))
          .orElseGet(
              () -> {
                LOG.info("[Caching] Assets into Database for userId {}", userId);

                return Optional.ofNullable(assetDAO.findAllByWalletIds(walletIds))
                    .map(
                        assetEntities -> {
                          if (!assetEntities.isEmpty())
                            assetEntityTemplate.opsForValue().set(cacheKey, assetEntities);
                          return assetEntities;
                        })
                    .orElse(Collections.emptyList());
              });
    } catch (Exception e) {
      LOG.error(RedisCacheConfig.REDIS_ERROR_LOG, e.getMessage());
      return assetDAO.findAllByWalletIds(walletIds);
    }
  }

  /**
   * Used to get the Asset list without operation and history
   *
   * @param walletIds param of the wallet id to be searched
   * @param userId User ID used for cache
   * @return Assets list without operations and histories
   */
  @Override
  public List<AssetWithoutOpAndStats> findAllAssetsByWalletIds(List<Long> walletIds, Long userId) {
    String cacheKey = userId + "_assets_without_operation_list";
    try {
      return Optional.ofNullable(assetWithoutOpAndStatsTemplate.opsForValue().get(cacheKey))
          .orElseGet(
              () -> {
                LOG.info("[Caching] Assets Lite into Database for userId {}", userId);

                return Optional.ofNullable(assetDAO.findAllAssetsByWalletIds(walletIds))
                    .map(
                        assetEntities -> {
                          if (!assetEntities.isEmpty())
                            assetWithoutOpAndStatsTemplate
                                .opsForValue()
                                .set(cacheKey, assetEntities);
                          return assetEntities;
                        })
                    .orElse(Collections.emptyList());
              });
    } catch (Exception e) {
      LOG.error(RedisCacheConfig.REDIS_ERROR_LOG, e.getMessage());
      return assetDAO.findAllAssetsByWalletIds(walletIds);
    }
  }

  /**
   * Query to find an asset by his identifier and the user id
   *
   * @param identifier to be searched
   * @param userId o the user
   * @return AssetEntities
   */
  @Override
  public List<AssetEntity> findAllByIdentifierAndUserId(String identifier, Long userId) {
    String cacheKey = userId + CACHE_ASSETS_BY_IDENTIFIER + identifier;
    try {
      return Optional.ofNullable(assetEntityTemplate.opsForValue().get(cacheKey))
          .orElseGet(
              () -> {
                LOG.info("[Caching] Asset {} into Database for userId {}", identifier, userId);

                return Optional.ofNullable(
                        assetDAO.findAllByIdentifierAndUserId(identifier, userId))
                    .map(
                        assetEntities -> {
                          if (!assetEntities.isEmpty())
                            assetEntityTemplate.opsForValue().set(cacheKey, assetEntities);
                          return assetEntities;
                        })
                    .orElse(Collections.emptyList());
              });
    } catch (Exception e) {
      LOG.error(RedisCacheConfig.REDIS_ERROR_LOG, e.getMessage());
      return assetDAO.findAllByIdentifierAndUserId(identifier, userId);
    }
  }

  /**
   * Query to find all asset
   *
   * @param userId o the user
   * @return AssetEntities
   */
  @Override
  public List<AssetEntity> findAllByUserIdOrderByRank(Long userId) {
    String cacheKey = userId + CACHE_ALL_ASSETS_BY_USER;
    try {
      return Optional.ofNullable(assetEntityTemplate.opsForValue().get(cacheKey))
          .orElseGet(
              () -> {
                LOG.info("[Caching] Asset Full list into Database for userId {}", userId);

                return Optional.ofNullable(assetDAO.findAllByUserIdOrderByRank(userId))
                    .map(
                        assetEntities -> {
                          if (!assetEntities.isEmpty())
                            assetEntityTemplate.opsForValue().set(cacheKey, assetEntities);
                          return assetEntities;
                        })
                    .orElse(Collections.emptyList());
              });
    } catch (Exception e) {
      LOG.error(RedisCacheConfig.REDIS_ERROR_LOG, e.getMessage());
      return assetDAO.findAllByUserIdOrderByRank(userId);
    }
  }
}
