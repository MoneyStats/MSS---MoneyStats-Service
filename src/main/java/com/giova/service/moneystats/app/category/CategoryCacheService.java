package com.giova.service.moneystats.app.category;

import com.giova.service.moneystats.app.category.entity.CategoryEntity;
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
import org.springframework.stereotype.Component;

@Component
public class CategoryCacheService {

  private static final String CATEGORY_CACHE = "Categories-Cache";
  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  @Autowired private CacheManager cacheManager;
  @Autowired private ICategoryDAO iCategoryDAO;

  @Caching(cacheable = @Cacheable(value = CATEGORY_CACHE))
  @LogInterceptor(type = LogTimeTracker.ActionType.CACHE)
  public List<CategoryEntity> findAll() {
    LOG.info("[Caching] CategoryEntity into Database");
    return iCategoryDAO.findAll();
  }

  @Caching(evict = @CacheEvict(value = CATEGORY_CACHE))
  @LogInterceptor(type = LogTimeTracker.ActionType.CACHE)
  public void deleteWalletsCache() {
    LOG.info("[Caching] Deleting cache for {}", CATEGORY_CACHE);
    Objects.requireNonNull(cacheManager.getCache(CATEGORY_CACHE)).clear();
  }
}
