package com.giova.service.moneystats.authentication;

import com.giova.service.moneystats.authentication.dto.User;
import com.nimbusds.jose.JOSEException;
import io.github.giovannilamarmora.utils.exception.UtilsException;
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
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Logged
@RestController
@RequestMapping("/v1/auth")
@CrossOrigin(origins = "*")
@Tag(name = "Authentication", description = "API for Authentication")
public class AuthController {

  @Autowired private AuthService authService;

  @PostMapping(
      value = "/sign-up",
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
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@register.json")))
  @ApiResponse(
      responseCode = "400",
      description = "Username or Email already present",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@costraint-exception.json")))
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public ResponseEntity<Response> signUp(
      @RequestBody @Valid @Schema(description = "User Body of request", implementation = User.class)
          User user,
      @RequestParam @Valid @Schema(description = "Invitation Code to register")
          String invitationCode)
      throws UtilsException {
    return authService.register(user, invitationCode);
  }

  @PostMapping(
      value = "/login",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "API to login an account", summary = "Login", tags = "Authentication")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@login.json")))
  @ApiResponse(
      responseCode = "401",
      description = "Wrong Credential",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@wrong-credential.json")))
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public ResponseEntity<Response> login(
      @RequestParam @Schema(description = "Username of the user") String username,
      @RequestParam @Schema(description = "Password of the user") String password)
      throws UtilsException, JOSEException {
    return authService.login(username, password);
  }

  @PostMapping(value = "/forgot-password", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      description = "API to reset a password",
      summary = "Reset Password",
      tags = "Authentication")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@forgot.json")))
  @ApiResponse(
      responseCode = "400",
      description = "Invalid Email",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@invalid-email.json")))
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public ResponseEntity<Response> forgotPassword(
      @RequestParam @Schema(description = "Email Address", example = "email@email.com")
          String email)
      throws UtilsException {
    return authService.forgotPassword(email);
  }

  @PostMapping(value = "/reset-password", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      description = "API to reset a password",
      summary = "Reset Password",
      tags = "Authentication")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@reset.json")))
  @ApiResponse(
      responseCode = "401",
      description = "Invalid Token",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@invalid-token.json")))
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public ResponseEntity<Response> resetPassword(
      @RequestParam @Valid @Schema(description = "New Password") String password,
      @RequestParam @Valid @Schema(description = "Token Received via Email") String token)
      throws UtilsException {
    return authService.resetPassword(password, token);
  }

  @GetMapping(value = "/check-login", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      description = "API to check an account",
      summary = "FE Check Account",
      tags = "Authentication")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@check-login.json")))
  @ApiResponse(
      responseCode = "401",
      description = "Wrong credential",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@wrong-credential.json")))
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public ResponseEntity<Response> checkLogin(
      @RequestHeader(HttpHeaders.AUTHORIZATION) @Valid @Schema(description = "Authorization Token")
          String authToken)
      throws UtilsException, JOSEException {
    return authService.checkLoginFE(authToken);
  }

  @PostMapping(
      value = "/update/user",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      description = "API to update an account",
      summary = "Update User",
      tags = "Authentication")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@register.json")))
  @ApiResponse(
      responseCode = "400",
      description = "Existing User",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@costraint-exception.json")))
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
  public ResponseEntity<Response> updateUser(
      @RequestHeader(HttpHeaders.AUTHORIZATION) @Valid @Schema(description = "Authorization Token")
          String authToken,
      @RequestBody @Valid @Schema(description = "User updated") User user) {
    return authService.updateUserData(authToken, user);
  }
}
