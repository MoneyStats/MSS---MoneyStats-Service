package com.giova.service.moneystats.crypto.forex;

import com.giova.service.moneystats.crypto.forex.entity.ForexDataEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface IForexDataDAO extends JpaRepository<ForexDataEntity, Long> {

  ForexDataEntity findByCurrency(String currency);

  void deleteForexDataEntitiesByCurrency(String currency);

  /**
   * Select the Crypto Fiat Currency to be used deleted
   *
   * @return
   */
  @Query(value = "select distinct FOREX_DATA.currency from ForexDataEntity FOREX_DATA")
  List<String> selectDistinctCurrency();

  // @Modifying
  // @Query(value = "ALTER TABLE MARKET_DATA AUTO_INCREMENT = 1", nativeQuery = true)
  // @Transactional(propagation = Propagation.REQUIRES_NEW)
  // void resetIdToOneMySQL();

  // @Modifying
  // @Query(value = "ALTER TABLE MARKET_DATA ALTER COLUMN ID RESTART WITH 1", nativeQuery = true)
  // @Transactional(propagation = Propagation.REQUIRES_NEW)
  // void resetIdToOneH2();
}
