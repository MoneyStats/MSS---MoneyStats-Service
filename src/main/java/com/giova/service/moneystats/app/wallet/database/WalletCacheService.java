package com.giova.service.moneystats.app.wallet.database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.giova.service.moneystats.app.wallet.entity.WalletEntity;
import com.giova.service.moneystats.config.cache.CacheDataConfig;
import com.giova.service.moneystats.config.cache.CacheUtils;
import com.giova.service.moneystats.config.cache.RedisCacheConfig;
import com.giova.service.moneystats.crypto.asset.database.AssetCacheService;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.utilities.Mapper;
import io.github.giovannilamarmora.utils.utilities.ObjectToolkit;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
public class WalletCacheService extends CacheDataConfig implements WalletRepository {

  @Autowired private IWalletDAO walletDAO;
  @Autowired private AssetCacheService assetCacheService;
  @Autowired private RedisTemplate<String, String> walletEntitiesTemplate;
  @Autowired private RedisTemplate<String, WalletEntity> walletEntityTemplate;

  // @Autowired private ReactiveRedisTemplate<String, WalletEntity> walletEntityTemplate;

  /**
   * Obtain Wallet without Stats and Assets, You Just Got the last Stats as Default
   *
   * @param userId User of the Wallet
   * @return Wallet with only the last Stats
   */
  @Override
  @LogInterceptor(type = LogTimeTracker.ActionType.CACHE)
  public List<WalletEntity> findAllByUserIdentifierWithoutAssetsAndHistory(String userId) {
    String cacheKey = application_name + SPACE + userId + CACHE_WALLETS_WITHOUT_DATA;
    try {
      return Optional.ofNullable(walletEntitiesTemplate.opsForValue().get(cacheKey))
          .map(s -> Mapper.readObject(s, new TypeReference<List<WalletEntity>>() {}))
          .map(cache -> logCache(cache, cacheKey))
          .orElseGet(
              () -> {
                LOG.info("[Caching] Wallet into Database for userId {}", userId);
                List<WalletEntity> wallets =
                    walletDAO.findAllByUserIdentifierWithoutAssetsAndHistory(userId);

                if (!ObjectUtils.isEmpty(wallets) && !wallets.isEmpty()) {
                  String json = Mapper.writeObjectToString(wallets);
                  walletEntitiesTemplate.opsForValue().set(cacheKey, json);
                }
                return wallets;
              });
    } catch (Exception e) {
      LOG.error(RedisCacheConfig.REDIS_ERROR_LOG, e.getMessage());
      return walletDAO.findAllByUserIdentifierWithoutAssetsAndHistory(userId);
    }
  }

  /**
   * Obtaining the full wallet list with all data
   *
   * @param userId User of the Wallet
   * @return Full Wallet list
   */
  @Override
  @LogInterceptor(type = LogTimeTracker.ActionType.CACHE)
  public List<WalletEntity> findAllByUserIdentifier(String userId) {
    String cacheKey = application_name + SPACE + userId + CACHE_FULL_WALLET_LIST;
    try {
      return Optional.ofNullable(walletEntitiesTemplate.opsForValue().get(cacheKey))
          .map(s -> Mapper.readObject(s, new TypeReference<List<WalletEntity>>() {}))
          .map(cache -> logCache(cache, cacheKey))
          .orElseGet(
              () -> {
                LOG.info("[Caching] Full Wallet into Database for userId {}", userId);
                List<WalletEntity> wallets = walletDAO.findAllByUserIdentifier(userId);

                if (!ObjectUtils.isEmpty(wallets) && !wallets.isEmpty()) {
                  String json = Mapper.writeObjectToString(wallets);
                  walletEntitiesTemplate.opsForValue().set(cacheKey, json);
                }
                return wallets;
              });
    } catch (Exception e) {
      LOG.error(RedisCacheConfig.REDIS_ERROR_LOG, e.getMessage());
      return walletDAO.findAllByUserIdentifier(userId);
    }
  }

  /**
   * Obtaining the Wallet data by ID
   *
   * @param id ID of the wallet to be searched
   * @param userId User ID used for cache
   * @return Full Wallet data
   */
  @Override
  @LogInterceptor(type = LogTimeTracker.ActionType.CACHE)
  public WalletEntity findWalletEntityById(Long id, String userId) {
    String cacheKey = application_name + SPACE + userId + CACHE_WALLET_BY_ID + id;
    try {
      return Optional.ofNullable(walletEntityTemplate.opsForValue().get(cacheKey))
          .map(cache -> logCache(cache, cacheKey))
          .orElseGet(
              () -> {
                LOG.info("[Caching] WalletEntity from Database by id {}", id);
                WalletEntity wallet = walletDAO.findWalletEntityById(id);

                if (!ObjectUtils.isEmpty(wallet))
                  walletEntityTemplate.opsForValue().set(cacheKey, wallet);

                return wallet;
              });
    } catch (Exception e) {
      LOG.error(RedisCacheConfig.REDIS_ERROR_LOG, e.getMessage());
      return walletDAO.findWalletEntityById(id);
    }
  }

  /**
   * Saving the Wallet
   *
   * @param walletEntity To be saved
   * @return Wallet Saved
   */
  @Override
  @LogInterceptor(type = LogTimeTracker.ActionType.CACHE)
  public WalletEntity save(WalletEntity walletEntity) {
    evictAllWalletCache(walletEntity);
    return walletDAO.save(walletEntity);
  }

  /**
   * Deleting all Data of the user
   *
   * @param userId of the data
   */
  @Override
  @LogInterceptor(type = LogTimeTracker.ActionType.CACHE)
  public void deleteAllByUserIdentifier(String userId) {
    clearAllWalletsCache();
    walletDAO.deleteAllByUserIdentifier(userId);
  }

  /**
   * Save all walletEntities
   *
   * @param walletEntities to be saved
   * @return wallet saved
   */
  @Override
  @LogInterceptor(type = LogTimeTracker.ActionType.CACHE)
  public List<WalletEntity> saveAll(List<WalletEntity> walletEntities) {
    clearAllWalletsCache();
    return walletDAO.saveAll(walletEntities);
  }

  /**
   * Method to delete the cache of the wallets of the user. Even if you update just a wallet we need
   * to delete the cache of al the wallet inorder to get fresh data.
   *
   * @param wallet Wallet to be removed from the cache
   */
  public void evictAllWalletCache(WalletEntity wallet) {
    String userId = wallet.getUserIdentifier();

    try {
      // Cache key per wallets without assets and history
      String keyWithoutAssetsAndHistory =
          application_name + SPACE + userId + CACHE_WALLETS_WITHOUT_DATA;
      // Cache key per full wallets list
      String keyFullWallets = application_name + SPACE + userId + CACHE_FULL_WALLET_LIST;

      if (!ObjectToolkit.isNullOrEmpty(
          walletEntitiesTemplate.opsForValue().get(keyWithoutAssetsAndHistory))) {
        walletEntitiesTemplate.delete(keyWithoutAssetsAndHistory);
        LOG.info("Cache evicted for key: {}", keyWithoutAssetsAndHistory);
      }

      if (!ObjectToolkit.isNullOrEmpty(walletEntitiesTemplate.opsForValue().get(keyFullWallets))) {
        walletEntitiesTemplate.delete(keyFullWallets);
        LOG.info("Cache evicted for key: {}", keyFullWallets);
      }

      String keyWalletById =
          application_name + SPACE + userId + CACHE_WALLET_BY_ID + wallet.getId();

      if (!ObjectToolkit.isNullOrEmpty(walletEntityTemplate.opsForValue().get(keyWalletById))) {
        walletEntityTemplate.delete(keyWalletById);
        LOG.info("Cache evicted for wallet key: {}", keyWalletById);
      }
      assetCacheService.clearAllAssetsCache();
    } catch (Exception e) {
      LOG.error("Error while evicting cache for userId {}: {}", userId, e.getMessage());
    }
  }

  /** Method to delete all the cache of the wallet of the user. */
  @LogInterceptor(type = LogTimeTracker.ActionType.CACHE)
  public void clearAllWalletsCache() {
    LOG.info("Starting to clear all wallet data cache.");
    CacheUtils.clearCache(walletEntityTemplate, "wallet data");
    CacheUtils.clearCache(walletEntitiesTemplate, "wallet entities data");
    LOG.info("Finished clearing wallet data cache.");
    assetCacheService.clearAllAssetsCache();
  }
}
