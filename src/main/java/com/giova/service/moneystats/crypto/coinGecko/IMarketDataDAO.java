package com.giova.service.moneystats.crypto.coinGecko;

import com.giova.service.moneystats.crypto.coinGecko.entity.MarketDataEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface IMarketDataDAO extends JpaRepository<MarketDataEntity, Long> {

  List<MarketDataEntity> findAllByCurrency(String currency);

  void deleteMarketDataEntitiesByCurrency(String currency);

  /**
   * Select the Crypto Fiat Currency to be used deleted
   *
   * @return
   */
  @Query(value = "select distinct MARKETDATA.currency from MarketDataEntity MARKETDATA")
  List<String> selectDistinctCurrency();

  //@Modifying
  //@Query(value = "ALTER TABLE MARKET_DATA AUTO_INCREMENT = 1", nativeQuery = true)
  //@Transactional(propagation = Propagation.REQUIRES_NEW)
  //void resetIdToOneMySQL();

  //@Modifying
  //@Query(value = "ALTER TABLE MARKET_DATA ALTER COLUMN ID RESTART WITH 1", nativeQuery = true)
  //@Transactional(propagation = Propagation.REQUIRES_NEW)
  //void resetIdToOneH2();
}
