package com.giova.service.moneystats.app.wallet;

import com.giova.service.moneystats.app.wallet.entity.WalletEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IWalletDAO extends JpaRepository<WalletEntity, Long> {

    List<WalletEntity> findAllByUserId(Long userId);

    List<WalletEntity> findAllByUserIdAndCategory(Long userId, String category);

    void deleteAllByUserId(Long userId);
}
