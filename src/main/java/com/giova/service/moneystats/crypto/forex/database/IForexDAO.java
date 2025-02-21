package com.giova.service.moneystats.crypto.forex.database;

import com.giova.service.moneystats.crypto.forex.entity.ForexDataEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface IForexDAO extends JpaRepository<ForexDataEntity, Long> {

  /**
   * Get Forex Data By Currency
   *
   * @param currency To be searched
   * @return ForexDataEntity
   */
  ForexDataEntity findByCurrency(String currency);

  /**
   * Select the Crypto Fiat Currency to be used deleted
   *
   * @return List of currency
   */
  @Query(value = "select distinct FOREX_DATA.currency from ForexDataEntity FOREX_DATA")
  List<String> selectDistinctCurrency();

  /**
   * Delete Forex Data for currency
   *
   * @param currency To be deleted
   */
  void deleteForexDataEntitiesByCurrency(String currency);
}
