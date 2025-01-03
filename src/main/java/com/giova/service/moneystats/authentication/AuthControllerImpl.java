package com.giova.service.moneystats.authentication;

import com.giova.service.moneystats.api.accessSphere.dto.shared.User;
import io.github.giovannilamarmora.utils.generic.Response;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Logged
@RestController
@RequestMapping("/v1/auth")
@CrossOrigin(origins = "*")
@Tag(name = "Authentication", description = "API for Authentication")
public class AuthControllerImpl implements AuthController {

  @Autowired private AuthService authService;

  @Override
  public Mono<ResponseEntity<Response>> signUp(User user, String invitationCode) {
    return authService.register(user, invitationCode);
  }
}
