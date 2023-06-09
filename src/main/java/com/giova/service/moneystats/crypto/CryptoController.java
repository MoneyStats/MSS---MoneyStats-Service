package com.giova.service.moneystats.crypto;

import com.giova.service.moneystats.generic.Response;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Logged
@RestController
@RequestMapping("/v1/crypto")
@CrossOrigin(origins = "*")
public class CryptoController {

  @Autowired private CryptoService appService;

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
}
