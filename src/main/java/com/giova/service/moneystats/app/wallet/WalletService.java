package com.giova.service.moneystats.app.wallet;

import com.giova.service.moneystats.app.stats.StatsService;
import com.giova.service.moneystats.app.wallet.dto.Wallet;
import com.giova.service.moneystats.app.wallet.entity.WalletEntity;
import com.giova.service.moneystats.authentication.AuthService;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import com.giova.service.moneystats.generic.Response;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.github.giovannilamarmora.utils.interceptors.correlationID.CorrelationIdUtils;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Logged
@AllArgsConstructor
public class WalletService {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private IWalletDAO iWalletDAO;

    @Autowired
    private WalletMapper walletMapper;

    @Autowired
    private StatsService statsService;

    @Autowired
    private AuthService authService;
    private final UserEntity user;

    @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
    public ResponseEntity<Response> insertOrUpdateWallet(Wallet wallet, String authToken) throws UtilsException {
        //UserEntity user = authService.checkLogin(authToken);

        WalletEntity walletEntity = walletMapper.fromWalletToWalletEntity(wallet, user);

        WalletEntity saved = iWalletDAO.save(walletEntity);

        Wallet walletToReturn = walletMapper.fromWalletEntityToWallet(saved);
        if (wallet.getHistory() != null && !wallet.getHistory().isEmpty()) {
            walletToReturn.setHistory(statsService.saveStats(wallet.getHistory(), saved, user));
        }

        String message = "Wallet " + walletToReturn.getName() + " Successfully saved!";

        Response response = new Response(HttpStatus.OK.value(), message, CorrelationIdUtils.getCorrelationId(), walletToReturn);
        return ResponseEntity.ok(response);
    }

    @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
    public ResponseEntity<Response> getWallets(String authToken) throws UtilsException {
        //UserEntity user = authService.checkLogin(authToken);

        List<WalletEntity> walletEntity = iWalletDAO.findAllByUserId(user.getId());

        String message = "";
        if (walletEntity.isEmpty()) {
            message = "Wallet Empty, insert new Wallet to get it!";
        } else {
            message = "Found " + walletEntity.size() + " Wallets";
        }

        List<Wallet> walletToReturn = walletMapper.fromWalletEntitiesToWallets(walletEntity);

        Response response = new Response(HttpStatus.OK.value(), message, CorrelationIdUtils.getCorrelationId(), walletToReturn);
        return ResponseEntity.ok(response);
    }
}
