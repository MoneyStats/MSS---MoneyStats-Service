package com.giova.service.moneystats.config.cache;

import io.github.giovannilamarmora.utils.utilities.ObjectToolkit;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;

public class CacheUtils {

  private static final Logger LOG = LoggerFactory.getLogger(CacheUtils.class);

  /** Helper method to clear cache for a given Redis template and data type. */
  public static void clearCache(RedisTemplate<String, ?> template, String dataType) {
    if (template.getConnectionFactory() != null) {
      LOG.debug("Redis connection factory is available for {}.", dataType);
      RedisConnection connection = template.getConnectionFactory().getConnection();

      if (!ObjectToolkit.isNullOrEmpty(connection)) {
        LOG.debug("Redis connection established successfully for {}.", dataType);
        Set<String> keys = template.keys("*");

        if (keys != null && !keys.isEmpty()) {
          LOG.info("Found {} keys in the {} cache to delete.", keys.size(), dataType);
          template.delete(keys);
          LOG.info("Successfully deleted all keys from the {} cache.", dataType);
        } else {
          LOG.info("No keys found in the {} cache to delete.", dataType);
        }
      } else {
        LOG.warn(
            "Redis connection could not be established for {}. Cache clearing aborted.", dataType);
      }
    } else {
      LOG.warn("Redis connection factory is unavailable for {}. Cache clearing aborted.", dataType);
    }
  }
}
