package com.giova.service.moneystats.crypto.marketData;

import com.giova.service.moneystats.scheduler.CronMarketData;
import io.github.giovannilamarmora.utils.context.TraceUtils;
import io.github.giovannilamarmora.utils.generic.Response;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Logged
@RestController
@RequestMapping("/v1/market-data")
@CrossOrigin(origins = "*")
@Tag(name = "Market Data", description = "API to get Market Data")
public class MarketDataControllerImpl implements MarketDataController {
  @Autowired private MarketDataService marketDataService;
  @Autowired private CronMarketData cronMarketData;

  @Override
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public ResponseEntity<Response> getMarketDataByCurrency(String token, String currency) {
    return ResponseEntity.ok(
        new Response(
            HttpStatus.OK.value(),
            "Market Data for " + currency,
            TraceUtils.getSpanID(),
            marketDataService.getMarketData(currency)));
  }

  @Override
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public ResponseEntity<Response> getMarketData(String token) {
    return ResponseEntity.ok(
        new Response(
            HttpStatus.OK.value(),
            "Market Data",
            TraceUtils.getSpanID(),
            marketDataService.getAllMarketData()));
  }

  @Override
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public ResponseEntity<Response> deleteMarketData(String token) {
    marketDataService.deleteMarketData();
    return ResponseEntity.ok(
        new Response(
            HttpStatus.OK.value(),
            "Market Data successfully removed!",
            TraceUtils.getSpanID(),
            null));
  }

  /**
   * API to start the import of the marketData
   *
   * @param token of the User
   * @return Successfully Response
   */
  @Override
  public ResponseEntity<Response> importMarketData(String token) {
    cronMarketData.scheduleAllCryptoAsset();
    Response response =
        new Response(
            HttpStatus.OK.value(),
            "MarketData Successfully imported",
            TraceUtils.getSpanID(),
            null);
    return ResponseEntity.ok(response);
  }
}
