package com.giova.service.moneystats.app.wallet.database;

import com.giova.service.moneystats.app.wallet.entity.WalletEntity;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
public class WalletCacheService implements WalletRepository {
  /* OLD DATA */
  private static final String WALLET_CACHE = "Wallets-Cache";
  private static final String CRYPTO_WALLET_CACHE = "Crypto-Wallets-Cache";
  private static final String DETAILS_WALLET = "Details-Wallet-Cache";
  /* END OLD DATA */
  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  @Autowired private CacheManager cacheManager; // OLD DATA
  @Autowired private IWalletDAO walletDAO;
  @Autowired private RedisTemplate<String, List<WalletEntity>> walletEntityTemplate;

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
    String cacheKey = userId + "_wallets_without_assets_and_history";
    return Optional.ofNullable(walletEntityTemplate.opsForValue().get(cacheKey))
        .orElseGet(
            () -> {
              LOG.info("[Caching] Wallet into Database for userId {}", userId);
              List<WalletEntity> wallets = walletDAO.findAllByUserIdWithoutAssetsAndHistory(userId);

              if (!ObjectUtils.isEmpty(wallets) && !wallets.isEmpty()) {
                walletEntityTemplate.opsForValue().set(cacheKey, wallets);
              }
              return wallets;
            });
  }

  @Override
  @LogInterceptor(type = LogTimeTracker.ActionType.CACHE)
  public List<WalletEntity> findAllByUserId(Long userId) {
    String cacheKey = userId + "_full_wallets_list";
    return Optional.ofNullable(walletEntityTemplate.opsForValue().get(cacheKey))
        .orElseGet(
            () -> {
              LOG.info("[Caching] Full Wallet into Database for userId {}", userId);
              List<WalletEntity> wallets = walletDAO.findAllByUserId(userId);

              if (!ObjectUtils.isEmpty(wallets) && !wallets.isEmpty()) {
                walletEntityTemplate.opsForValue().set(cacheKey, wallets);
              }
              return wallets;
            });
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
