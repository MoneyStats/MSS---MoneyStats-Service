package com.giova.service.moneystats.app.stats;

import com.giova.service.moneystats.app.stats.dto.Stats;
import com.giova.service.moneystats.app.wallet.WalletService;
import com.giova.service.moneystats.app.wallet.dto.Wallet;
import io.github.giovannilamarmora.utils.context.TraceUtils;
import io.github.giovannilamarmora.utils.generic.Response;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.github.giovannilamarmora.utils.utilities.Mapper;
import io.github.giovannilamarmora.utils.utilities.Utilities;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Logged
@Service
public class StatsService {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  @Autowired private IStatsDAO iStatsDAO;

  @Autowired private WalletService walletService;
  @Autowired private StatsComponent statsComponent;

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = Exception.class)
  public ResponseEntity<Response> addStats(List<Wallet> wallets) {
    LOG.info("Adding new Stats");
    wallets =
        wallets.stream()
            .peek(
                wallet -> {
                  // Response res = walletService.addOrUpdateWallet(wallet, false, null).getBody();
                  Response res = null;
                  List<Stats> statsList = new ArrayList<>();
                  if (!Utilities.isNullOrEmpty(res) && !Utilities.isNullOrEmpty(res.getData())) {
                    Wallet w = Mapper.convertObject(res.getData(), Wallet.class);
                    statsList = statsComponent.getStatsByWallet(w.getId());
                  }
                  wallet.setHistory(statsList);
                })
            .toList();

    String message = "Stats Added Successfully!";

    Response response =
        new Response(HttpStatus.OK.value(), message, TraceUtils.getSpanID(), wallets);
    return ResponseEntity.ok(response);
  }
}
