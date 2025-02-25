package com.giova.service.moneystats.crypto.marketData.database;

import com.giova.service.moneystats.crypto.marketData.entity.MarketDataEntity;
import java.util.List;
import org.springframework.data.jpa.repository.Query;

public interface MarketDataRepository {

  /**
   * Get all MarketData with currency
   *
   * @param currency To get MarketData
   * @return List of MarketData
   */
  List<MarketDataEntity> findAllByCurrency(String currency);

  /**
   * Get all MarketData
   *
   * @return List of MarketData
   */
  List<MarketDataEntity> findAll();

  /**
   * Delete MarketData for specific Currency
   *
   * @param currency To be deleted
   */
  void deleteMarketDataEntitiesByCurrency(String currency);

  /** Reset MarketData id on 1 */
  void resetAutoIncrement();

  /**
   * Select the Crypto Fiat Currency to be used deleted
   *
   * @return List of currency
   */
  @Query(value = "select distinct MARKETDATA.currency from MarketDataEntity MARKETDATA")
  List<String> selectDistinctCurrency();

  /**
   * Save all MarketData
   *
   * @param marketDataEntities To be saved
   * @return MarketData
   */
  List<MarketDataEntity> saveAll(List<MarketDataEntity> marketDataEntities, String currency);
}
