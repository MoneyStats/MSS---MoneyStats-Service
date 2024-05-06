package com.giova.service.moneystats.authentication;

import com.giova.service.moneystats.authentication.dto.User;
import com.nimbusds.jose.JOSEException;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.generic.Response;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Logged
@RestController
@RequestMapping("/v1/auth")
@CrossOrigin(origins = "*")
@Tag(name = "Authentication", description = "API for Authentication")
public class AuthControllerImpl implements AuthController {

  @Autowired private AuthService authService;

  @Override
  public ResponseEntity<Response> signUp(User user, String invitationCode) throws UtilsException {
    return authService.register(user, invitationCode);
  }

  public ResponseEntity<Response> login(String basic) throws UtilsException, JOSEException {
    return authService.login(basic);
  }

  public ResponseEntity<Response> forgotPassword(String email) throws UtilsException {
    return authService.forgotPassword(email);
  }

  public ResponseEntity<Response> resetPassword(String password, String token)
      throws UtilsException {
    return authService.resetPassword(password, token);
  }

  public ResponseEntity<Response> updateUser(String authToken, User user) {
    return authService.updateUserData(authToken, user);
  }
}
