package com.giova.service.moneystats.crypto;

import com.giova.service.moneystats.scheduler.CronCachingReset;
import io.github.giovannilamarmora.utils.context.TraceUtils;
import io.github.giovannilamarmora.utils.exception.dto.ExceptionResponse;
import io.github.giovannilamarmora.utils.generic.Response;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
  @Autowired private CronCachingReset cronCachingReset;

  @GetMapping(value = "/dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      description = "API to get Crypto Dashboard",
      summary = "Crypto Dashboard",
      tags = "Crypto")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@crypto-dashboard.json")))
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
  public ResponseEntity<Response> getCryptoDashboard(
      @RequestHeader(HttpHeaders.AUTHORIZATION) @Valid @Schema(description = "Authorization Token")
          String authToken) {
    return appService.getCryptoDashboardData();
  }

  @GetMapping(value = "/resume", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "API to get Crypto Resume", summary = "Crypto Resume", tags = "Crypto")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@crypto-resume.json")))
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
  public ResponseEntity<Response> getCryptoResume(
      @RequestHeader(HttpHeaders.AUTHORIZATION) @Valid @Schema(description = "Authorization Token")
          String authToken) {
    return appService.getCryptoResumeData();
  }

  @PatchMapping(value = "/cache/clean", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "API to get Crypto Resume", summary = "Cache Clean", tags = "Crypto")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@cache-clean.json")))
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
  public ResponseEntity<Response> cleanCache(
      @RequestHeader(HttpHeaders.AUTHORIZATION) @Valid @Schema(description = "Authorization Token")
          String authToken) {
    cronCachingReset.scheduleCleanCache();
    Response response =
        new Response(
            HttpStatus.OK.value(), "Cache cleaned successfully", TraceUtils.getSpanID(), null);
    return ResponseEntity.ok(response);
  }
}
