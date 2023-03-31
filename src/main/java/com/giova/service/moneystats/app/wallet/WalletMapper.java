package com.giova.service.moneystats.app.wallet;

import com.giova.service.moneystats.app.attachments.ImageMapper;
import com.giova.service.moneystats.app.stats.dto.Stats;
import com.giova.service.moneystats.app.stats.entity.StatsEntity;
import com.giova.service.moneystats.app.wallet.dto.Wallet;
import com.giova.service.moneystats.app.wallet.entity.WalletEntity;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WalletMapper {

  @Autowired ImageMapper imageMapper;

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_MAPPER)
  public WalletEntity fromWalletToWalletEntity(Wallet wallet, UserEntity userEntity) {
    WalletEntity walletEntity = new WalletEntity();
    BeanUtils.copyProperties(wallet, walletEntity);
    walletEntity.setUser(userEntity);
    if (walletEntity.getHistory() != null) {
      walletEntity.setHistory(
          wallet.getHistory().stream()
              .map(
                  stats -> {
                    StatsEntity statsEntity = new StatsEntity();
                    BeanUtils.copyProperties(stats, statsEntity);
                    statsEntity.setUser(userEntity);
                    statsEntity.setWallet(walletEntity);
                    return statsEntity;
                  })
              .collect(Collectors.toList()));
    }
    return walletEntity;
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_MAPPER)
  public Wallet fromWalletEntityToWallet(WalletEntity walletEntity) throws UtilsException {
    Wallet wallet = new Wallet();
    BeanUtils.copyProperties(walletEntity, wallet);
    if (walletEntity.getHistory() != null) {
      wallet.setHistory(
          walletEntity.getHistory().stream()
              .map(
                  statsEntity -> {
                    Stats stats = new Stats();
                    BeanUtils.copyProperties(statsEntity, stats);
                    return stats;
                  })
              .collect(Collectors.toList()));
    }
    return wallet;
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_MAPPER)
  public List<Wallet> fromWalletEntitiesToWallets(List<WalletEntity> walletEntities) {
    return walletEntities.stream()
        .map(
            (walletEntity -> {
              Wallet wallet = new Wallet();
              BeanUtils.copyProperties(walletEntity, wallet);
              if (walletEntity.getHistory() != null) {
                wallet.setHistory(
                    walletEntity.getHistory().stream()
                        .map(
                            statsEntity -> {
                              Stats stats = new Stats();
                              BeanUtils.copyProperties(statsEntity, stats);
                              return stats;
                            })
                        .collect(Collectors.toList()));
              }

              return wallet;
            }))
        .collect(Collectors.toList());
  }
}
