package com.giova.service.moneystats.app.stats;

import com.giova.service.moneystats.app.wallet.dto.Wallet;
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
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Logged
@RestController
@RequestMapping("/v1")
@CrossOrigin(origins = "*")
@Tag(name = "Stats", description = "API for stats")
public class StatsController {

  @Autowired private StatsService statsService;

  /**
   * API to add a new Stats
   *
   * @param wallets Valid Wallet to be updated
   * @param token User Access Token
   * @return The Wallet added
   */
  @PostMapping(
      value = "/stats",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "API to add a new Stats", summary = "Add Stats", tags = "Stats")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@wallets-full-list_response.json")))
  @ApiResponse(
      responseCode = "401",
      description = "Invalid Token",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@invalid-token-exception.json")))
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public ResponseEntity<Response> addStats(
      @RequestBody
          @Valid
          @Schema(description = "List of wallet with stats added", implementation = Wallet.class)
          List<Wallet> wallets,
      @RequestHeader(HttpHeaders.AUTHORIZATION)
          @Valid
          @Schema(description = "Authorization Token", example = "Bearer eykihugUiOj6bihiguu...")
          String token) {
    return statsService.addStats(wallets);
  }
}
