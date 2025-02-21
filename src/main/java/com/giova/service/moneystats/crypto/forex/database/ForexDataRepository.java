package com.giova.service.moneystats.crypto.forex.database;

import com.giova.service.moneystats.crypto.forex.entity.ForexDataEntity;
import java.util.List;

public interface ForexDataRepository {

  /**
   * Get Forex Data By Currency
   *
   * @param currency To be searched
   * @return ForexDataEntity
   */
  ForexDataEntity findByCurrency(String currency);

  /**
   * Get All Forex Data
   *
   * @return List of ForexDataEntity
   */
  List<ForexDataEntity> findAll();

  /**
   * Save Forex Data
   *
   * @param forexDataEntity To be saved
   * @return ForexDataEntity
   */
  ForexDataEntity save(ForexDataEntity forexDataEntity, String currency);

  /**
   * Select the Crypto Fiat Currency to be used deleted
   *
   * @return List of currency
   */
  List<String> selectDistinctCurrency();

  /**
   * Delete Forex Data for currency
   *
   * @param currency To be deleted
   */
  void deleteForexDataEntitiesByCurrency(String currency);
}
