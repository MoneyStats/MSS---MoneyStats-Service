package com.giova.service.moneystats.crypto.marketData.database;

import com.giova.service.moneystats.crypto.marketData.entity.MarketDataEntity;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface IMarketDataDAO extends JpaRepository<MarketDataEntity, Long> {

  /**
   * Get all MarketData with currency
   *
   * @param currency To get MarketData
   * @return List of MarketData
   */
  List<MarketDataEntity> findAllByCurrency(String currency);

  /**
   * Delete MarketData for specific Currency
   *
   * @param currency To be deleted
   */
  void deleteMarketDataEntitiesByCurrency(String currency);

  /**
   * Select the Crypto Fiat Currency to be used deleted
   *
   * @return List of currency
   */
  @Query(value = "select distinct MARKETDATA.currency from MarketDataEntity MARKETDATA")
  List<String> selectDistinctCurrency();

  @Modifying
  @Transactional
  @Query(value = "ALTER TABLE market_data AUTO_INCREMENT = 1", nativeQuery = true)
  void resetAutoIncrement();

  // @Modifying
  // @Query(value = "ALTER TABLE MARKET_DATA AUTO_INCREMENT = 1", nativeQuery = true)
  // @Transactional(propagation = Propagation.REQUIRES_NEW)
  // void resetIdToOneMySQL();

  // @Modifying
  // @Query(value = "ALTER TABLE MARKET_DATA ALTER COLUMN ID RESTART WITH 1", nativeQuery = true)
  // @Transactional(propagation = Propagation.REQUIRES_NEW)
  // void resetIdToOneH2();
}
