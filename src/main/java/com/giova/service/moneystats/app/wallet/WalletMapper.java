package com.giova.service.moneystats.app.wallet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giova.service.moneystats.app.attachments.ImageMapper;
import com.giova.service.moneystats.app.stats.dto.Stats;
import com.giova.service.moneystats.app.stats.entity.StatsEntity;
import com.giova.service.moneystats.app.wallet.dto.Wallet;
import com.giova.service.moneystats.app.wallet.entity.WalletEntity;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WalletMapper {

  private final ObjectMapper mapper = new ObjectMapper();
  @Autowired ImageMapper imageMapper;

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_MAPPER)
  public WalletEntity fromWalletToWalletEntity(Wallet wallet, UserEntity userEntity) {
    WalletEntity walletEntity = new WalletEntity();
    BeanUtils.copyProperties(wallet, walletEntity);
    walletEntity.setUser(userEntity);

    // if (wallet.getInfoString() != null && !wallet.getInfoString().isEmpty()) {
    walletEntity.setInfo(wallet.getInfoString());
    // } else
    if (wallet.getInfo() != null) {
      walletEntity.setInfo(convertWithStream(wallet.getInfo()));
    }
    if (wallet.getHistory() != null) {
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
  public Wallet fromWalletEntityToWallet(WalletEntity walletEntity) throws JsonProcessingException {
    Wallet wallet = new Wallet();
    BeanUtils.copyProperties(walletEntity, wallet);

    if (walletEntity.getInfo() != null) {
      wallet.setInfo(
          mapper.readValue(walletEntity.getInfo(), new TypeReference<Map<String, String>>() {}));
    }
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
              if (walletEntity.getInfo() != null) {
                try {
                  wallet.setInfo(
                      mapper.readValue(
                          walletEntity.getInfo(), new TypeReference<Map<String, String>>() {}));
                } catch (JsonProcessingException e) {
                  throw new RuntimeException(e);
                }
              }
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

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_MAPPER)
  public List<Wallet> deleteWalletIds(List<Wallet> wallets) {
    return wallets.stream()
        .map(
            (walletToEdit -> {
              Wallet wallet = new Wallet();
              walletToEdit.setId(null);
              BeanUtils.copyProperties(walletToEdit, wallet);

              if (walletToEdit.getHistory() != null) {
                wallet.setHistory(
                    walletToEdit.getHistory().stream()
                        .map(
                            statsEntity -> {
                              Stats stats = new Stats();
                              statsEntity.setId(null);
                              BeanUtils.copyProperties(statsEntity, stats);
                              return stats;
                            })
                        .collect(Collectors.toList()));
              }

              return wallet;
            }))
        .collect(Collectors.toList());
  }

  private String convertWithStream(Map<String, ?> map) {
    String mapAsString =
        map.keySet().stream()
            .map(key -> "\"" + key + "\"" + ": \"" + map.get(key) + "\"")
            .collect(Collectors.joining(", ", "{", "}"));
    return mapAsString;
  }
}
