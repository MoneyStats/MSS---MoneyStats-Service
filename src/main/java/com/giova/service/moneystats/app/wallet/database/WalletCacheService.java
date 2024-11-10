package com.giova.service.moneystats.app.wallet.database;

import com.giova.service.moneystats.app.wallet.entity.WalletEntity;
import com.giova.service.moneystats.config.cache.CacheUtils;
import com.giova.service.moneystats.config.cache.RedisCacheConfig;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.utilities.Utilities;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
public class WalletCacheService implements WalletRepository {
  /* OLD DATA */
  public static final String CRYPTO_WALLET_CACHE = "Crypto-Wallets-Cache";
  /* END OLD DATA */
  private static final String CACHE_WALLETS_WITHOUT_DATA = "_wallets_without_assets_and_history";
  private static final String CACHE_FULL_WALLET_LIST = "_full_wallets_list";
  private static final String CACHE_WALLET_BY_ID = "_wallet_";

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  @Autowired private IWalletDAO walletDAO;
  @Autowired private RedisTemplate<String, List<WalletEntity>> walletEntitiesTemplate;
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
  public List<WalletEntity> findAllByUserIdWithoutAssetsAndHistory(Long userId) {
    String cacheKey = userId + CACHE_WALLETS_WITHOUT_DATA;
    try {
      return Optional.ofNullable(walletEntitiesTemplate.opsForValue().get(cacheKey))
          .orElseGet(
              () -> {
                LOG.info("[Caching] Wallet into Database for userId {}", userId);
                List<WalletEntity> wallets =
                    walletDAO.findAllByUserIdWithoutAssetsAndHistory(userId);

                if (!ObjectUtils.isEmpty(wallets) && !wallets.isEmpty()) {
                  walletEntitiesTemplate.opsForValue().set(cacheKey, wallets);
                }
                return wallets;
              });
    } catch (Exception e) {
      LOG.error(RedisCacheConfig.REDIS_ERROR_LOG, e.getMessage());
      return walletDAO.findAllByUserIdWithoutAssetsAndHistory(userId);
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
  public List<WalletEntity> findAllByUserId(Long userId) {
    String cacheKey = userId + CACHE_FULL_WALLET_LIST;
    try {
      return Optional.ofNullable(walletEntitiesTemplate.opsForValue().get(cacheKey))
          .orElseGet(
              () -> {
                LOG.info("[Caching] Full Wallet into Database for userId {}", userId);
                List<WalletEntity> wallets = walletDAO.findAllByUserId(userId);

                if (!ObjectUtils.isEmpty(wallets) && !wallets.isEmpty()) {
                  walletEntitiesTemplate.opsForValue().set(cacheKey, wallets);
                }
                return wallets;
              });
    } catch (Exception e) {
      LOG.error(RedisCacheConfig.REDIS_ERROR_LOG, e.getMessage());
      return walletDAO.findAllByUserId(userId);
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
  public WalletEntity findWalletEntityById(Long id, Long userId) {
    String cacheKey = userId + CACHE_WALLET_BY_ID + id;
    try {
      return Optional.ofNullable(walletEntityTemplate.opsForValue().get(cacheKey))
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
  public void deleteAllByUserId(Long userId) {
    clearAllWalletsCache();
    walletDAO.deleteAllByUserId(userId);
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
    Long userId = wallet.getUser().getId();

    try {
      // Cache key per wallets without assets and history
      String keyWithoutAssetsAndHistory = userId + CACHE_WALLETS_WITHOUT_DATA;
      // Cache key per full wallets list
      String keyFullWallets = userId + CACHE_FULL_WALLET_LIST;

      if (!Utilities.isNullOrEmpty(
          walletEntitiesTemplate.opsForValue().get(keyWithoutAssetsAndHistory))) {
        walletEntitiesTemplate.delete(keyWithoutAssetsAndHistory);
        LOG.info("Cache evicted for key: {}", keyWithoutAssetsAndHistory);
      }

      if (!Utilities.isNullOrEmpty(walletEntitiesTemplate.opsForValue().get(keyFullWallets))) {
        walletEntitiesTemplate.delete(keyFullWallets);
        LOG.info("Cache evicted for key: {}", keyFullWallets);
      }

      String keyWalletById = userId + CACHE_WALLET_BY_ID + wallet.getId();

      if (!Utilities.isNullOrEmpty(walletEntityTemplate.opsForValue().get(keyWalletById))) {
        walletEntityTemplate.delete(keyWalletById);
        LOG.info("Cache evicted for wallet key: {}", keyWalletById);
      }
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
  }

  /* OLD DATA */
  @Caching(
      cacheable =
          @Cacheable(value = CRYPTO_WALLET_CACHE, key = "#userId", condition = "#userId!=null"))
  @LogInterceptor(type = LogTimeTracker.ActionType.CACHE)
  public List<WalletEntity> findAllByUserIdAndCategory(Long userId, String category) {
    LOG.info("[Caching] WalletEntity into Database by userId {} and category {}", userId, category);
    return walletDAO.findAllByUserIdAndCategory(userId, category);
  }
}
