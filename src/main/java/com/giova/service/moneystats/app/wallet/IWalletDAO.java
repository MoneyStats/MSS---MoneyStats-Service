package com.giova.service.moneystats.app.wallet;

import com.giova.service.moneystats.app.wallet.entity.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IWalletDAO extends JpaRepository<WalletEntity, Long> {
}
