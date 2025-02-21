package com.giova.service.moneystats.crypto.forex.database;

import com.giova.service.moneystats.crypto.forex.entity.ForexDataEntity;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

public class ForexDataDAOAdapter implements ForexDataRepository {
  @Autowired private IForexDAO iForexDAO;

  /**
   * Get Forex Data By Currency
   *
   * @param currency To be searched
   * @return ForexDataEntity
   */
  @Override
  public ForexDataEntity findByCurrency(String currency) {
    return iForexDAO.findByCurrency(currency);
  }

  /**
   * Get All Forex Data
   *
   * @return List of ForexDataEntity
   */
  @Override
  public List<ForexDataEntity> findAll() {
    return iForexDAO.findAll();
  }

  /**
   * Save Forex Data
   *
   * @param forexDataEntity To be saved
   * @return ForexDataEntity
   */
  @Override
  public ForexDataEntity save(ForexDataEntity forexDataEntity, String currency) {
    return iForexDAO.save(forexDataEntity);
  }

  /**
   * Select the Crypto Fiat Currency to be used deleted
   *
   * @return List of currency
   */
  @Override
  public List<String> selectDistinctCurrency() {
    return iForexDAO.selectDistinctCurrency();
  }

  /**
   * Delete Forex Data for currency
   *
   * @param currency To be deleted
   */
  @Override
  public void deleteForexDataEntitiesByCurrency(String currency) {
    iForexDAO.deleteForexDataEntitiesByCurrency(currency);
  }
}
