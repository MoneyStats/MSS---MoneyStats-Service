package com.giova.service.moneystats.crypto;

import com.giova.service.moneystats.generic.Response;
import com.giova.service.moneystats.scheduler.CronCachingReset;
import com.giova.service.moneystats.scheduler.CronMarketData;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.github.giovannilamarmora.utils.interceptors.correlationID.CorrelationIdUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Logged
@RestController
@RequestMapping("/v1/crypto")
@CrossOrigin(origins = "*")
@Tag(name = "Crypto", description = "APIs for Crypto")
public class CryptoController {

  @Autowired private CryptoService appService;
  @Autowired private CronMarketData cronMarketData;
  @Autowired private CronCachingReset cronCachingReset;

  @GetMapping(value = "/dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "API to get Crypto Dashboard", tags = "Crypto")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@crypto-dashboard.json")))
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public ResponseEntity<Response> getCryptoDashboard(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authToken) {
    return appService.getCryptoDashboardData();
  }

  @GetMapping(value = "/resume", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "API to get Crypto Resume", tags = "Crypto")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@crypto-resume.json")))
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public ResponseEntity<Response> getCryptoResume(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authToken) {
    return appService.getCryptoResumeData();
  }

  @PatchMapping(value = "/marketData/import", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "API to get Crypto Resume", tags = "Crypto")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@import-marketData.json")))
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public ResponseEntity<Response> importMarketData(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authToken) {
    cronMarketData.scheduleAllCryptoAsset();
    Response response =
        new Response(
            HttpStatus.OK.value(),
            "MarketData Successfully imported",
            CorrelationIdUtils.getCorrelationId(),
            null);
    return ResponseEntity.ok(response);
  }

  @PatchMapping(value = "/cache/clean", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "API to get Crypto Resume", tags = "Crypto")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@cache-clean.json")))
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public ResponseEntity<Response> cleanCache(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authToken) {
    cronCachingReset.scheduleCleanCache();
    Response response =
        new Response(
            HttpStatus.OK.value(),
            "Cache cleaned successfully",
            CorrelationIdUtils.getCorrelationId(),
            null);
    return ResponseEntity.ok(response);
  }
}
