package com.giova.service.moneystats.crypto.forex;

import com.giova.service.moneystats.crypto.forex.dto.ForexData;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.generic.Response;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.github.giovannilamarmora.utils.interceptors.correlationID.CorrelationIdUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Logged
@RestController
@RequestMapping("/v1/forex")
@CrossOrigin(origins = "*")
public class ForexDataController {
  @Autowired private ForexDataService forexDataService;

  @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
  @Tag(name = "Forex", description = "API to get All Forex Data")
  @Operation(description = "API to get All Forex Data", tags = "Forex")
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public ResponseEntity<Response> getForexDataList(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authToken) throws UtilsException {
    List<ForexData> forex = forexDataService.getAllForexData();
    return ResponseEntity.ok(
        new Response(
            HttpStatus.OK.value(),
            "Founded " + forex.size() + " Forex Coins!",
            CorrelationIdUtils.getCorrelationId(),
            forex));
  }

  @GetMapping(value = "/get", produces = MediaType.APPLICATION_JSON_VALUE)
  @Tag(name = "Forex", description = "API to get All Forex Data")
  @Operation(description = "API to get All Forex Data", tags = "Forex")
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public ResponseEntity<Response> getForexData(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authToken, @RequestParam String currency)
      throws UtilsException {
    return ResponseEntity.ok(
        new Response(
            HttpStatus.OK.value(),
            "Got Forex Data For " + currency,
            CorrelationIdUtils.getCorrelationId(),
            forexDataService.getForexData(currency)));
  }
}
