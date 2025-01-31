package com.giova.service.moneystats.config.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CacheDataConfig {

  /* Cache Wallets */
  public static final String CACHE_WALLETS_WITHOUT_DATA = "_wallets_without_assets_and_history";
  public static final String CACHE_FULL_CRYPTO_WALLET_LIST = "_full_crypto_wallets_list";
  public static final String CACHE_FULL_WALLET_LIST = "_full_wallets_list";
  public static final String CACHE_WALLET_BY_ID = "_wallet_";

  /* Cache Market Data */
  public static final String CACHE_MARKET_DATA = "market_data_";
  public static final String CACHE_MARKET_DATA_FULL = "market_data_full";

  /* Other Config */
  public static final String CACHE_HIT = "Cache hit for key: {}";
  public static final String SPACE = "_";

  /* Cache Forex */
  public static final String CACHE_FOREX_DATA_BY_CURRENCY = "forex_data_";
  public static final String CACHE_FOREX_DATA = "forex_data_all";

  /* Cache Assets */
  public static final String CACHE_ASSETS_BY_IDENTIFIER = "_assets_identifier_";
  public static final String CACHE_ALL_ASSETS_BY_USER = "_assets_by_user";
  public static final String CACHE_ASSETS_LIVE_PRICE_LIST = "_assets_live_price_list";
  public static final String CACHE_ASSETS_FULL_LIST_BY_WALLETS_IDS =
      "_assets_full_list_by_wallet_ids";
  public static final String CACHE_ASSETS_WITHOUT_OPERATIONS_LIST =
      "_assets_without_operation_list";

  public final Logger LOG = LoggerFactory.getLogger(this.getClass());

  @Value("${spring.application.name}")
  public String application_name;

  @Value("${spring.application.name}")
  public String cache_duration;

  public <T> T logCache(T cached, String cacheKey) {
    LOG.info(CACHE_HIT, cacheKey);
    return cached;
  }
}
