package com.giova.service.moneystats.app.settings;

import com.giova.service.moneystats.app.model.GithubIssues;
import com.giova.service.moneystats.app.model.Support;
import com.giova.service.moneystats.app.wallet.dto.Wallet;
import io.github.giovannilamarmora.utils.exception.dto.ExceptionResponse;
import io.github.giovannilamarmora.utils.generic.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public interface SettingsController {

  /**
   * API to report a BUG
   *
   * @param githubIssues Request Data
   * @return Response
   */
  @PostMapping(
      value = "/report/bug",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "API to report a bug", summary = "Report Bug", tags = "Settings")
  @ApiResponse(
      responseCode = "400",
      description = "Error",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE /*,
              examples = @ExampleObject(value = "@unprocessable-exception.json")*/))
  Mono<ResponseEntity<Response>> bugReport(
      @RequestBody
          @Valid
          @Schema(description = "Github Issues body", implementation = GithubIssues.class)
          GithubIssues githubIssues);

  /**
   * API to make the backup dell'app
   *
   * @param token Of the user
   * @return Response JSON
   */
  @GetMapping(
      value = "/backup",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "API to Backup Wallet and Stats", summary = "Backup", tags = "Settings")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE /*,
              examples = @ExampleObject(value = "@backup.json")*/))
  @ApiResponse(
      responseCode = "401",
      description = "Invalid Token",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE /*,
              examples = @ExampleObject(value = "@invalid-token-exception.json")*/))
  ResponseEntity<Response> backupData(
      @RequestHeader(HttpHeaders.AUTHORIZATION)
          @Valid
          @Schema(description = "Authorization Token", example = "Bearer eykihugUiOj6bihiguu...")
          String token);

  /**
   * API to Restore Data
   *
   * @param wallets To be restored
   * @param token of the User
   * @return Confirmation Response
   */
  @PostMapping(
      value = "/restore",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      description = "API to restore Wallets and Stats",
      summary = "Restore",
      tags = "Settings")
  @ApiResponse(
      responseCode = "401",
      description = "Invalid Token",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE /*,
              examples = @ExampleObject(value = "@invalid-token-exception.json")*/))
  ResponseEntity<Response> restoreData(
      @RequestBody @Valid @Schema(description = "Wallets to restore", implementation = Wallet.class)
          List<Wallet> wallets,
      @RequestHeader(HttpHeaders.AUTHORIZATION)
          @Valid
          @Schema(description = "Authorization Token", example = "Bearer eykihugUiOj6bihiguu...")
          String token);

  /**
   * API to contact support
   *
   * @param support body to be sent
   * @return Response
   */
  @PostMapping(
      value = "/contact",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "API to contact us", summary = "Contact US", tags = "Settings")
  Mono<ResponseEntity<Response>> contactUs(
      @RequestBody @Valid @Schema(description = "Support Body", implementation = Support.class)
          Support support);

  /**
   * API to clean the cache of the app
   *
   * @param token ADMIN token
   * @return Cache cleaned
   */
  @PatchMapping(value = "/cache/clean", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "API to clean the cache", summary = "Cache Clean", tags = "Settings")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE /*,
              examples = @ExampleObject(value = "@cache-clean.json")*/))
  @ApiResponse(
      responseCode = "401",
      description = "Invalid Token",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE /*,
              examples = @ExampleObject(value = "@invalid-token-exception.json")*/))
  ResponseEntity<Response> cleanCache(
      ServerWebExchange exchange,
      @RequestParam(value = "isUserInfo", required = false, defaultValue = "false")
          @Schema(description = "Use to delete the cache of the user session", example = "true")
          Boolean isUserInfo,
      @RequestHeader(HttpHeaders.AUTHORIZATION)
          @Valid
          @Schema(description = "Authorization Token", example = "Bearer eykihugUiOj6bihiguu...")
          String token);
}
