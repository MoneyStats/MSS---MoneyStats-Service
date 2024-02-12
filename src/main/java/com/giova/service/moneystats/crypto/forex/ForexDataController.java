package com.giova.service.moneystats.crypto.forex;

import com.giova.service.moneystats.crypto.forex.dto.ForexData;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.exception.dto.ExceptionResponse;
import io.github.giovannilamarmora.utils.generic.Response;
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
import java.util.List;
import javax.validation.Valid;
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
@Tag(name = "Forex", description = "API to handle Forex Data")
public class ForexDataController {
  @Autowired private ForexDataService forexDataService;

  @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "API to get All Forex Data", summary = "Forex List", tags = "Forex")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@forex-list.json")))
  @ApiResponse(
      responseCode = "401",
      description = "Invalid JWE",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@invalid-jwe.json")))
  @ApiResponse(
      responseCode = "401",
      description = "Expired JWE",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@expired-jwe.json")))
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public ResponseEntity<Response> getForexDataList(
      @RequestHeader(HttpHeaders.AUTHORIZATION) @Valid @Schema(description = "Authorization Token")
          String authToken)
      throws UtilsException {
    List<ForexData> forex = forexDataService.getAllForexData();
    return ResponseEntity.ok(
        new Response(
            HttpStatus.OK.value(),
            "Founded " + forex.size() + " Forex Coins!",
            CorrelationIdUtils.getCorrelationId(),
            forex));
  }

  @GetMapping(value = "/get", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "API to get Forex Data", summary = "Forex", tags = "Forex")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@forex.json")))
  @ApiResponse(
      responseCode = "401",
      description = "Invalid JWE",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@invalid-jwe.json")))
  @ApiResponse(
      responseCode = "401",
      description = "Expired JWE",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@expired-jwe.json")))
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public ResponseEntity<Response> getForexData(
      @RequestHeader(HttpHeaders.AUTHORIZATION) @Valid @Schema(description = "Authorization Token")
          String authToken,
      @RequestParam @Schema(description = "Currency to search", example = "USD") String currency)
      throws UtilsException {
    return ResponseEntity.ok(
        new Response(
            HttpStatus.OK.value(),
            "Got Forex Data For " + currency,
            CorrelationIdUtils.getCorrelationId(),
            forexDataService.getForexData(currency)));
  }
}
