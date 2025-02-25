package com.giova.service.moneystats.crypto.asset.database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.giova.service.moneystats.config.cache.CacheDataConfig;
import com.giova.service.moneystats.config.cache.CacheUtils;
import com.giova.service.moneystats.config.cache.RedisCacheConfig;
import com.giova.service.moneystats.crypto.asset.dto.AssetLivePrice;
import com.giova.service.moneystats.crypto.asset.dto.AssetWithoutOpAndStats;
import com.giova.service.moneystats.crypto.asset.entity.AssetEntity;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.utilities.Mapper;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class AssetCacheService extends CacheDataConfig implements AssetRepository {

  @Autowired private RedisTemplate<String, String> assetEntityTemplate;
  @Autowired private RedisTemplate<String, String> assetLivePriceTemplate;
  @Autowired private RedisTemplate<String, String> assetWithoutOpAndStatsTemplate;
  @Autowired private IAssetDAO assetDAO;

  /**
   * Getting only the identifier, balance and wallet id, used to get the live price of the wallet
   *
   * @param walletIds list of the wallet id
   * @param userId User ID used for cache
   * @return Assets with only the identifier, balance and wallet id
   */
  @Override
  public List<AssetLivePrice> findAssetsByWalletIds(List<Long> walletIds, String userId) {
    String cacheKey = application_name + SPACE + userId + CACHE_ASSETS_LIVE_PRICE_LIST;
    try {
      return Optional.ofNullable(assetLivePriceTemplate.opsForValue().get(cacheKey))
          .map(s -> Mapper.readObject(s, new TypeReference<List<AssetLivePrice>>() {}))
          .map(cache -> logCache(cache, cacheKey))
          .orElseGet(
              () -> {
                LOG.info("[Caching] Assets Live Price into Database for userId {}", userId);

                return Optional.ofNullable(assetDAO.findAssetsByWalletIds(walletIds))
                    .map(
                        assetEntities -> {
                          if (!assetEntities.isEmpty()) {
                            String json = Mapper.writeObjectToString(assetEntities);
                            assetLivePriceTemplate.opsForValue().set(cacheKey, json);
                          }
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
  public List<AssetEntity> findAllByWalletIds(List<Long> walletIds, String userId) {
    String cacheKey = application_name + SPACE + userId + CACHE_ASSETS_FULL_LIST_BY_WALLETS_IDS;
    try {
      return Optional.ofNullable(assetEntityTemplate.opsForValue().get(cacheKey))
          .map(s -> Mapper.readObject(s, new TypeReference<List<AssetEntity>>() {}))
          .map(cache -> logCache(cache, cacheKey))
          .orElseGet(
              () -> {
                LOG.info("[Caching] Assets into Database for userId {}", userId);

                return Optional.ofNullable(assetDAO.findAllByWalletIds(walletIds))
                    .map(
                        assetEntities -> {
                          if (!assetEntities.isEmpty()) {
                            String json = Mapper.writeObjectToString(assetEntities);
                            assetEntityTemplate.opsForValue().set(cacheKey, json);
                          }
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
  public List<AssetWithoutOpAndStats> findAllAssetsByWalletIds(
      List<Long> walletIds, String userId) {
    String cacheKey = application_name + SPACE + userId + CACHE_ASSETS_WITHOUT_OPERATIONS_LIST;
    try {
      return Optional.ofNullable(assetWithoutOpAndStatsTemplate.opsForValue().get(cacheKey))
          .map(s -> Mapper.readObject(s, new TypeReference<List<AssetWithoutOpAndStats>>() {}))
          .map(cache -> logCache(cache, cacheKey))
          .orElseGet(
              () -> {
                LOG.info("[Caching] Assets Lite into Database for userId {}", userId);

                return Optional.ofNullable(assetDAO.findAllAssetsByWalletIds(walletIds))
                    .map(
                        assetEntities -> {
                          if (!assetEntities.isEmpty()) {
                            String json = Mapper.writeObjectToString(assetEntities);
                            assetWithoutOpAndStatsTemplate.opsForValue().set(cacheKey, json);
                          }
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
  public List<AssetEntity> findAllByIdentifierAndUserId(String identifier, String userId) {
    String cacheKey = application_name + SPACE + userId + CACHE_ASSETS_BY_IDENTIFIER + identifier;
    try {
      return Optional.ofNullable(assetEntityTemplate.opsForValue().get(cacheKey))
          .map(s -> Mapper.readObject(s, new TypeReference<List<AssetEntity>>() {}))
          .map(cache -> logCache(cache, cacheKey))
          .orElseGet(
              () -> {
                LOG.info("[Caching] Asset {} into Database for userId {}", identifier, userId);

                return Optional.ofNullable(
                        assetDAO.findAllByIdentifierAndUserIdentifier(identifier, userId))
                    .map(
                        assetEntities -> {
                          if (!assetEntities.isEmpty()) {
                            String json = Mapper.writeObjectToString(assetEntities);
                            assetEntityTemplate.opsForValue().set(cacheKey, json);
                          }
                          return assetEntities;
                        })
                    .orElse(Collections.emptyList());
              });
    } catch (Exception e) {
      LOG.error(RedisCacheConfig.REDIS_ERROR_LOG, e.getMessage());
      return assetDAO.findAllByIdentifierAndUserIdentifier(identifier, userId);
    }
  }

  /**
   * Query to find all asset
   *
   * @param userId o the user
   * @return AssetEntities
   */
  @Override
  public List<AssetEntity> findAllByUserIdOrderByRank(String userId) {
    String cacheKey = application_name + SPACE + userId + CACHE_ALL_ASSETS_BY_USER;
    try {
      return Optional.ofNullable(assetEntityTemplate.opsForValue().get(cacheKey))
          .map(s -> Mapper.readObject(s, new TypeReference<List<AssetEntity>>() {}))
          .map(cache -> logCache(cache, cacheKey))
          .orElseGet(
              () -> {
                LOG.info("[Caching] Asset Full list into Database for userId {}", userId);

                return Optional.ofNullable(assetDAO.findAllByUserIdentifierOrderByRank(userId))
                    .map(
                        assetEntities -> {
                          if (!assetEntities.isEmpty()) {
                            String json = Mapper.writeObjectToString(assetEntities);
                            assetEntityTemplate.opsForValue().set(cacheKey, json);
                          }
                          return assetEntities;
                        })
                    .orElse(Collections.emptyList());
              });
    } catch (Exception e) {
      LOG.error(RedisCacheConfig.REDIS_ERROR_LOG, e.getMessage());
      return assetDAO.findAllByUserIdentifierOrderByRank(userId);
    }
  }

  /** Method to delete all the cache of the assets of the user. */
  @LogInterceptor(type = LogTimeTracker.ActionType.CACHE)
  public void clearAllAssetsCache() {
    LOG.info("Starting to clear all assets data cache.");
    CacheUtils.clearCache(assetEntityTemplate, "assets data");
    CacheUtils.clearCache(assetLivePriceTemplate, "assets live data");
    CacheUtils.clearCache(assetWithoutOpAndStatsTemplate, "assets without operation data");
    LOG.info("Finished clearing assets data cache.");
  }
}
