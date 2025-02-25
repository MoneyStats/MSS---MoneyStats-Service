package com.giova.service.moneystats.crypto.marketData;

import io.github.giovannilamarmora.utils.exception.dto.ExceptionResponse;
import io.github.giovannilamarmora.utils.generic.Response;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

public interface MarketDataController {

  @GetMapping(value = "{currency}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      description = "API to get all Market Data for specific Currency",
      summary = "Get MarketData By Currency",
      tags = "Market Data")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE /*,
              examples = @ExampleObject(value = "@market-data.json")*/))
  @ApiResponse(
      responseCode = "401",
      description = "Invalid Token",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE /*,
              examples = @ExampleObject(value = "@invalid-token-exception.json")*/))
  ResponseEntity<Response> getMarketDataByCurrency(
      @RequestHeader(HttpHeaders.AUTHORIZATION)
          @Valid
          @Schema(description = "Authorization Token", example = "Bearer eykihugUiOj6bihiguu...")
          String token,
      @PathVariable(value = "currency")
          @Schema(description = "Currency to be searched", example = "USD")
          @Valid
          @NotNull(message = "Currency is a required field")
          String currency);

  @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      description = "API to get all Market Data",
      summary = "Get MarketData",
      tags = "Market Data")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE /*,
              examples = @ExampleObject(value = "@market-data.json")*/))
  @ApiResponse(
      responseCode = "401",
      description = "Invalid Token",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE /*,
              examples = @ExampleObject(value = "@invalid-token-exception.json")*/))
  ResponseEntity<Response> getMarketData(
      @RequestHeader(HttpHeaders.AUTHORIZATION)
          @Valid
          @Schema(description = "Authorization Token", example = "Bearer eykihugUiOj6bihiguu...")
          String token);

  @DeleteMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      description = "API to remove all Market Data",
      summary = "Delete MarketData",
      tags = "Market Data")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE))
  @ApiResponse(
      responseCode = "401",
      description = "Invalid Token",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE /*,
              examples = @ExampleObject(value = "@invalid-token-exception.json")*/))
  ResponseEntity<Response> deleteMarketData(
      @RequestHeader(HttpHeaders.AUTHORIZATION)
          @Valid
          @Schema(description = "Authorization Token", example = "Bearer eykihugUiOj6bihiguu...")
          String token);

  /**
   * API to start the import of the marketData
   *
   * @param token of the User
   * @return Successfully Response
   */
  @PatchMapping(value = "/import", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "API to import MarketData", tags = "Market Data")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE /*,
              examples = @ExampleObject(value = "@import-marketData.json")*/))
  @ApiResponse(
      responseCode = "401",
      description = "Invalid Token",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE/*,
              examples = @ExampleObject(value = "@invalid-token-exception.json")*/))
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  ResponseEntity<Response> importMarketData(
      @RequestHeader(HttpHeaders.AUTHORIZATION)
          @Valid
          @Schema(description = "Authorization Token", example = "Bearer eykihugUiOj6bihiguu...")
          String token);
}
