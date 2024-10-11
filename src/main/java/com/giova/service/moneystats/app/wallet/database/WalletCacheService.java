package com.giova.service.moneystats.app.wallet.database;

import com.giova.service.moneystats.app.wallet.entity.WalletEntity;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class WalletCacheService implements WalletRepository {
  /* OLD DATA */
  private static final String WALLET_CACHE = "Wallets-Cache";
  private static final String CRYPTO_WALLET_CACHE = "Crypto-Wallets-Cache";
  private static final String DETAILS_WALLET = "Details-Wallet-Cache";
  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  @Autowired private CacheManager cacheManager;
  @Autowired private IWalletDAO walletDAO;
  /* END OLD DATA */

  @Autowired private ReactiveRedisTemplate<String, WalletEntity> walletEntityTemplate;

  /**
   * Obtain Wallet without Stats and Assets, You Just Got the last Stats as Default
   *
   * @param userId User of the Wallet
   * @return Wallet with only the last Stats
   */
  @Override
  public List<WalletEntity> findAllByUserIdWithoutAssetsAndHistory(Long userId) {
    return List.of();
  }

  /* OLD DATA */
  @Caching(
      cacheable = @Cacheable(value = WALLET_CACHE, key = "#userId", condition = "#userId!=null"))
  @LogInterceptor(type = LogTimeTracker.ActionType.CACHE)
  public List<WalletEntity> findAllByUserId(Long userId) {
    LOG.info("[Caching] WalletEntity into Database by userId {}", userId);
    return walletDAO.findAllByUserId(userId);
  }

  @Caching(
      cacheable =
          @Cacheable(value = CRYPTO_WALLET_CACHE, key = "#userId", condition = "#userId!=null"))
  @LogInterceptor(type = LogTimeTracker.ActionType.CACHE)
  public List<WalletEntity> findAllByUserIdAndCategory(Long userId, String category) {
    LOG.info("[Caching] WalletEntity into Database by userId {} and category {}", userId, category);
    return walletDAO.findAllByUserIdAndCategory(userId, category);
  }

  @Caching(
      evict = {
        @CacheEvict(value = WALLET_CACHE),
        @CacheEvict(value = CRYPTO_WALLET_CACHE),
        @CacheEvict(value = DETAILS_WALLET)
      })
  @LogInterceptor(type = LogTimeTracker.ActionType.CACHE)
  public void deleteWalletsCache() {
    LOG.info("[Caching] Deleting cache for {} and {}", WALLET_CACHE, CRYPTO_WALLET_CACHE);
    Objects.requireNonNull(cacheManager.getCache(WALLET_CACHE)).clear();
    Objects.requireNonNull(cacheManager.getCache(CRYPTO_WALLET_CACHE)).clear();
    Objects.requireNonNull(cacheManager.getCache(DETAILS_WALLET)).clear();
  }

  public WalletEntity save(WalletEntity wallet) {
    deleteWalletsCache();
    return walletDAO.save(wallet);
  }

  public List<WalletEntity> saveAll(List<WalletEntity> walletEntities) {
    deleteWalletsCache();
    return walletDAO.saveAll(walletEntities);
  }

  public void deleteAllByUserId(Long userId) {
    deleteWalletsCache();
    walletDAO.deleteAllByUserId(userId);
  }

  @Caching(cacheable = @Cacheable(value = DETAILS_WALLET, key = "#id"))
  public WalletEntity findWalletEntityById(Long id) {
    LOG.info("[Caching] WalletEntity from Database by and id {}", id);
    return walletDAO.findWalletEntityById(id);
  }
}
