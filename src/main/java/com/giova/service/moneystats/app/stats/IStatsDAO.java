package com.giova.service.moneystats.app.stats;

import com.giova.service.moneystats.app.stats.entity.StatsEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface IStatsDAO extends JpaRepository<StatsEntity, Long> {

  /**
   * Used on {@link StatsEntity}, to get all the Date ordered and just one time
   *
   * @param userId
   * @return a List od unique and not duplicate date
   */
  @Query(
      value =
          "select distinct STATS.date from StatsEntity STATS where STATS.user.id = :userId order by STATS.date")
  List<LocalDate> selectDistinctDate(Long userId);

  /**
   * Used on {@link StatsEntity}, to get all the Date ordered and just one time
   *
   * @param userId
   * @return a List od unique and not duplicate date
   */
  @Query(
      value =
          "select distinct STATS.date from StatsEntity STATS where STATS.user.id = :userId and STATS.wallet.id is not null order by STATS.date")
  List<LocalDate> selectAppDistinctDate(Long userId);

  /**
   * Used on {@link com.giova.service.moneystats.crypto.CryptoService}, to get all the Date ordered
   * and just one time
   *
   * @param userId
   * @return a List od unique and not duplicate date
   */
  @Query(
      value =
          "select distinct STATS.date from StatsEntity STATS where STATS.user.id = :userId and STATS.asset.id is not null order by STATS.date")
  List<LocalDate> selectCryptoDistinctDate(Long userId);

  List<StatsEntity> findStatsEntitiesByWalletId(Long walletId);
}
