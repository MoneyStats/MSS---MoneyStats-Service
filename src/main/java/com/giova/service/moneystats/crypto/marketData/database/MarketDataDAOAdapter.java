package com.giova.service.moneystats.crypto.marketData.database;

import com.giova.service.moneystats.crypto.marketData.entity.MarketDataEntity;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

public class MarketDataDAOAdapter implements MarketDataRepository {
  @Autowired private IMarketDataDAO iMarketDataDAO;

  /**
   * Get all MarketData with currency
   *
   * @param currency To get MarketData
   * @return List of MarketData
   */
  @Override
  public List<MarketDataEntity> findAllByCurrency(String currency) {
    return iMarketDataDAO.findAllByCurrency(currency);
  }

  /**
   * Get all MarketData
   *
   * @return List of MarketData
   */
  @Override
  public List<MarketDataEntity> findAll() {
    return iMarketDataDAO.findAll();
  }

  /**
   * Delete MarketData for specific Currency
   *
   * @param currency To be deleted
   */
  @Override
  public void deleteMarketDataEntitiesByCurrency(String currency) {
    iMarketDataDAO.deleteMarketDataEntitiesByCurrency(currency);
  }

  /**
   * Select the Crypto Fiat Currency to be used deleted
   *
   * @return List of currency
   */
  @Override
  public List<String> selectDistinctCurrency() {
    return iMarketDataDAO.selectDistinctCurrency();
  }

  /**
   * Save all MarketData
   *
   * @param marketDataEntities To be saved
   * @return MarketData
   */
  @Override
  public List<MarketDataEntity> saveAll(List<MarketDataEntity> marketDataEntities) {
    return iMarketDataDAO.saveAll(marketDataEntities);
  }
}
