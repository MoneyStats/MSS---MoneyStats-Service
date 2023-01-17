package com.giova.service.moneystats.app.wallet;

import com.giova.service.moneystats.app.wallet.dto.Wallet;
import com.giova.service.moneystats.app.wallet.entity.WalletEntity;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class WalletMapper {

    @LogInterceptor(type = LogTimeTracker.ActionType.APP_MAPPER)
    public WalletEntity fromWalletToWalletEntity(Wallet wallet) {
        WalletEntity walletEntity = new WalletEntity();
        BeanUtils.copyProperties(wallet, walletEntity);
        return walletEntity;
    }

    @LogInterceptor(type = LogTimeTracker.ActionType.APP_MAPPER)
    public Wallet fromWalletEntityToWallet(WalletEntity walletEntity) {
        Wallet wallet = new Wallet();
        BeanUtils.copyProperties(walletEntity, wallet);
        return wallet;
    }
}
