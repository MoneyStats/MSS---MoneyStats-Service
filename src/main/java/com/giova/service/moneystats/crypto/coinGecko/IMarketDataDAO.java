package com.giova.service.moneystats.crypto.coinGecko;

import com.giova.service.moneystats.crypto.coinGecko.entity.MarketDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IMarketDataDAO extends JpaRepository<MarketDataEntity, Long> {}
