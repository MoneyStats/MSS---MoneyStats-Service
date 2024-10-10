package com.giova.service.moneystats.app.wallet.database;

import com.giova.service.moneystats.app.wallet.entity.WalletEntity;

import java.util.List;

public interface WalletRepository {
  List<WalletEntity> findAllByUserIdWithoutAssetsAndHistory(Long userId);
}
