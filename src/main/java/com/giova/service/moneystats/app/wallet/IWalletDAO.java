package com.giova.service.moneystats.app.wallet;

import com.giova.service.moneystats.app.wallet.entity.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IWalletDAO extends JpaRepository<WalletEntity, Long> {

    List<WalletEntity> findAllByUserId(Long userId);
}
