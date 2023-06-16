package com.giova.service.moneystats.crypto;

import com.giova.service.moneystats.generic.Response;
import com.giova.service.moneystats.scheduler.CronCachingReset;
import com.giova.service.moneystats.scheduler.CronMarketData;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.github.giovannilamarmora.utils.interceptors.correlationID.CorrelationIdUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Logged
@RestController
@RequestMapping("/v1/crypto")
@CrossOrigin(origins = "*")
public class CryptoController {

  @Autowired private CryptoService appService;
  @Autowired private CronMarketData cronMarketData;
  @Autowired private CronCachingReset cronCachingReset;

  @GetMapping(value = "/dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
  @Tag(name = "Crypto", description = "API to get Crypto Dashboard")
  @Operation(description = "API to get Crypto Dashboard", tags = "Crypto")
  @LogInterceptor(type = LogTimeTracker.ActionType.APP_CONTROLLER)
  public ResponseEntity<Response> getCryptoDashboard(@RequestHeader("authToken") String authToken)
      throws UtilsException {
    return appService.getCryptoDashboardData();
  }

  @GetMapping(value = "/resume", produces = MediaType.APPLICATION_JSON_VALUE)
  @Tag(name = "Crypto", description = "API to get Crypto Resume")
  @Operation(description = "API to get Crypto Resume", tags = "Crypto")
  @LogInterceptor(type = LogTimeTracker.ActionType.APP_CONTROLLER)
  public ResponseEntity<Response> getCryptoResume(@RequestHeader("authToken") String authToken)
      throws UtilsException {
    return appService.getCryptoResumeData();
  }

  @PatchMapping(value = "/MarketData/import", produces = MediaType.APPLICATION_JSON_VALUE)
  @Tag(name = "Crypto", description = "API to get Crypto Resume")
  @Operation(description = "API to get Crypto Resume", tags = "Crypto")
  @LogInterceptor(type = LogTimeTracker.ActionType.APP_CONTROLLER)
  public ResponseEntity<Response> importMarketData(@RequestHeader("authToken") String authToken) throws UtilsException {
    cronMarketData.scheduleAllCryptoAsset();
    Response response = new Response(HttpStatus.OK.value(), "MarketData Successfully imported", CorrelationIdUtils.getCorrelationId(), null);
    return ResponseEntity.ok(response);
  }

  @PatchMapping(value = "/Cache/clean", produces = MediaType.APPLICATION_JSON_VALUE)
  @Tag(name = "Crypto", description = "API to get Crypto Resume")
  @Operation(description = "API to get Crypto Resume", tags = "Crypto")
  @LogInterceptor(type = LogTimeTracker.ActionType.APP_CONTROLLER)
  public ResponseEntity<Response> cleanCache(@RequestHeader("authToken") String authToken)
          throws UtilsException {
    cronCachingReset.scheduleCleanCache();
    Response response = new Response(HttpStatus.OK.value(), "Cache cleaned successfully", CorrelationIdUtils.getCorrelationId(), null);
    return ResponseEntity.ok(response);
  }
}
