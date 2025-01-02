package com.giova.service.moneystats.app.stats;

import com.giova.service.moneystats.app.stats.dto.Stats;
import com.giova.service.moneystats.app.stats.entity.StatsEntity;
import com.giova.service.moneystats.app.wallet.entity.WalletEntity;
import com.giova.service.moneystats.authentication.dto.UserData;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Logged
@Service
public class StatsComponent {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  @Autowired private IStatsDAO iStatsDAO;

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public List<LocalDate> getDistinctDates(UserData user) {
    return iStatsDAO.selectAppDistinctDate(user.getIdentifier());
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public List<LocalDate> getCryptoDistinctDates(UserData user) {
    return iStatsDAO.selectCryptoDistinctDate(user.getIdentifier());
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public List<Stats> saveStats(List<Stats> stats, WalletEntity wallet, UserData user) {
    List<StatsEntity> statsEntities = StatsMapper.fromStatsToEntity(stats, wallet, user);

    List<StatsEntity> saved = iStatsDAO.saveAll(statsEntities);

    return StatsMapper.fromEntityToStats(saved);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public List<Stats> getStatsByWallet(Long walletId) {

    List<StatsEntity> stats = iStatsDAO.findStatsEntitiesByWalletId(walletId);
    List<Stats> response = new ArrayList<>();
    if (!stats.isEmpty()) {
      response = StatsMapper.fromEntityToStats(stats);
    }

    return response;
  }
}
