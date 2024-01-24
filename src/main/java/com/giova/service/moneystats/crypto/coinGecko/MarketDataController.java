package com.giova.service.moneystats.crypto.coinGecko;

import com.giova.service.moneystats.generic.Response;
import io.github.giovannilamarmora.utils.exception.UtilsException;
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
@RequestMapping("/v1/market-data")
@CrossOrigin(origins = "*")
@Tag(name = "Market Data", description = "API to get Market Data")
public class MarketDataController {
  @Autowired private MarketDataService marketDataService;

  @GetMapping(value = "/get", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "API to get Market Data", tags = "Market Data")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@market-data.json")))
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public ResponseEntity<Response> getMarketData(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authToken, @RequestParam String currency)
      throws UtilsException {
    return ResponseEntity.ok(
        new Response(
            HttpStatus.OK.value(),
            "Market Datas",
            CorrelationIdUtils.getCorrelationId(),
            marketDataService.getMarketData(currency)));
  }
}
