package com.giova.service.moneystats.crypto.forex;

import com.giova.service.moneystats.app.wallet.dto.Wallet;
import com.giova.service.moneystats.crypto.forex.dto.ForexData;
import io.github.giovannilamarmora.utils.exception.dto.ExceptionResponse;
import io.github.giovannilamarmora.utils.generic.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

public interface ForexDataController {

  /**
   * API To get the forex data of a currency
   *
   * @param token User Access Token
   * @param currency To be searched
   * @return Forex Data
   */
  @GetMapping(value = "/{currency}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      description = "API to get Forex Data for a specified Currency",
      summary = "Forex By Currency",
      tags = "Forex")
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
      description = "Invalid Token",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@invalid-token-exception.json")))
  ResponseEntity<Response> getForexDataByCurrency(
      @RequestHeader(HttpHeaders.AUTHORIZATION)
          @Valid
          @Schema(description = "Authorization Token", example = "Bearer eykihugUiOj6bihiguu...")
          String token,
      @PathVariable(value = "currency")
          @Schema(description = "Currency to be searched", example = "USD")
          @Valid
          @NotNull(message = "Currency is a required field")
          String currency);

  /**
   * API to get the full list of Forex Data
   *
   * @param token User Access Token
   * @return Forex Data
   */
  @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
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
      description = "Invalid Token",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@invalid-token-exception.json")))
  ResponseEntity<Response> getForexDataList(
      @RequestHeader(HttpHeaders.AUTHORIZATION)
          @Valid
          @Schema(description = "Authorization Token", example = "Bearer eykihugUiOj6bihiguu...")
          String token);

  /**
   * API To Save the forex data
   *
   * @param token User Access Token
   * @return Forex Data
   */
  @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      description = "API to save Forex Data for a specified Currency",
      summary = "Save Forex By Currency",
      tags = "Forex")
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
      description = "Invalid Token",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@invalid-token-exception.json")))
  ResponseEntity<Response> saveForexDataByCurrency(
      @RequestHeader(HttpHeaders.AUTHORIZATION)
          @Valid
          @Schema(description = "Authorization Token", example = "Bearer eykihugUiOj6bihiguu...")
          String token,
      @RequestBody
          @Valid
          @Schema(description = "ForexData to Add", implementation = ForexData.class)
          ForexData forexData);
}
