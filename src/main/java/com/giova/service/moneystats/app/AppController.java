package com.giova.service.moneystats.app;

import io.github.giovannilamarmora.utils.exception.dto.ExceptionResponse;
import io.github.giovannilamarmora.utils.generic.Response;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Logged
@RestController
@RequestMapping("/v1/app")
@CrossOrigin(origins = "*")
@Tag(name = "App", description = "API to Handle App")
public class AppController {

  @Autowired private AppService appService;

  @GetMapping(value = "/dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "API to get Dashboard Data", summary = "Dashboard", tags = "App")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE /*,
              examples = @ExampleObject(value = "@dashboard.json")*/))
  @ApiResponse(
      responseCode = "401",
      description = "Invalid Token",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE /*,
              examples = @ExampleObject(value = "@invalid-token-exception.json")*/))
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public ResponseEntity<Response> getDashboard(
      @RequestHeader(HttpHeaders.AUTHORIZATION)
          @Valid
          @Schema(description = "Authorization Token", example = "Bearer eykihugUiOj6bihiguu...")
          String token) {
    return appService.getDashboardData();
  }

  @GetMapping(value = "/resume/{year}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "API to resume data by years", summary = "Resume Data", tags = "App")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE /*,
              examples = @ExampleObject(value = "@resume.json")*/))
  @ApiResponse(
      responseCode = "401",
      description = "Invalid Token",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE /*,
              examples = @ExampleObject(value = "@invalid-token-exception.json")*/))
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public ResponseEntity<Response> getResume(
      @RequestHeader(HttpHeaders.AUTHORIZATION)
          @Valid
          @Schema(description = "Authorization Token", example = "Bearer eykihugUiOj6bihiguu...")
          String token,
      @PathVariable(value = "year")
          @Schema(description = "Year of the data to be searched", example = "2024")
          @Valid
          @NotNull(message = "Year is a required field")
          Long year) {
    return appService.getResumeData(year);
  }

  @GetMapping(value = "/history", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "API to history data by years", summary = "History Data", tags = "App")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE /*,
              examples = @ExampleObject(value = "@resume.json")*/))
  @ApiResponse(
      responseCode = "401",
      description = "Invalid Token",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE/*,
              examples = @ExampleObject(value = "@invalid-token-exception.json")*/))
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public ResponseEntity<Response> getHistory(
      @RequestHeader(HttpHeaders.AUTHORIZATION)
          @Valid
          @Schema(description = "Authorization Token", example = "Bearer eykihugUiOj6bihiguu...")
          String token) {
    return appService.getHistoryData();
  }
}
