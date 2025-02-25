package com.giova.service.moneystats.authentication;

import io.github.giovannilamarmora.utils.exception.dto.ExceptionResponse;
import io.github.giovannilamarmora.utils.generic.Response;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

public interface AuthController {

  @GetMapping(
      value = "/code",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      description = "API to register an account",
      summary = "Registration",
      tags = "Authentication")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE /*,
              examples = @ExampleObject(value = "@check-code.json")*/))
  @ApiResponse(
      responseCode = "400",
      description = "Username or Email already present",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE /*,
              examples = @ExampleObject(value = "@invalid-invitation-code.json")*/))
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  Mono<ResponseEntity<Response>> checkRegistrationToken(
      @RequestParam @Valid @Schema(description = "Invitation Code to register")
          String invitationCode);
}
