package com.giova.service.moneystats.authentication;

import com.giova.service.moneystats.api.emailSender.EmailSenderService;
import com.giova.service.moneystats.api.emailSender.dto.EmailContent;
import com.giova.service.moneystats.api.emailSender.dto.EmailResponse;
import com.giova.service.moneystats.app.attachments.ImageService;
import com.giova.service.moneystats.app.attachments.dto.Image;
import com.giova.service.moneystats.authentication.dto.User;
import com.giova.service.moneystats.authentication.dto.UserRole;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import com.giova.service.moneystats.authentication.token.TokenService;
import com.giova.service.moneystats.authentication.token.dto.AuthToken;
import com.giova.service.moneystats.generic.Response;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.github.giovannilamarmora.utils.interceptors.correlationID.CorrelationIdUtils;
import java.time.LocalDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Logged
@RequiredArgsConstructor
public class AuthService {

  final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  private final UserEntity user;

  @Value(value = "${app.invitationCode}")
  private String registerToken;

  @Value(value = "${app.fe.url}")
  private String feUrl;

  @Autowired private IAuthDAO iAuthDAO;
  @Autowired private AuthMapper authMapper;
  @Autowired private TokenService tokenService;
  @Autowired private EmailSenderService emailSenderService;
  @Autowired private ImageService imageService;

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public ResponseEntity<Response> register(User user, String invitationCode) throws UtilsException {
    user.setRole(UserRole.USER);

    if (!registerToken.equalsIgnoreCase(invitationCode)) {
      LOG.error("Invitation code: {}, is wrong", invitationCode);
      throw new UtilsException(
          AuthException.ERR_AUTH_MSS_005, AuthException.ERR_AUTH_MSS_005.getMessage());
    }

    UserEntity userEntity = authMapper.mapUserToUserEntity(user);
    userEntity.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
    user.setPassword(null);

    UserEntity saved = iAuthDAO.save(userEntity);
    saved.setPassword(null);

    String message = "User: " + user.getUsername() + " Successfully registered!";

    Response response =
        new Response(
            HttpStatus.OK.value(),
            message,
            CorrelationIdUtils.getCorrelationId(),
            authMapper.mapUserEntityToUser(saved));

    return ResponseEntity.ok(response);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public ResponseEntity<Response> login(String username, String password) throws UtilsException {

    String email = username.contains("@") ? username : null;
    username = email != null ? null : username;

    UserEntity userEntity = iAuthDAO.findUserEntityByUsernameOrEmail(username, email);
    if (userEntity == null) {
      LOG.error("User not found");
      throw new UtilsException(
          AuthException.ERR_AUTH_MSS_003, AuthException.ERR_AUTH_MSS_003.getMessage());
    }
    boolean matches = bCryptPasswordEncoder.matches(password, userEntity.getPassword());
    if (!matches) {
      LOG.error("User not found");
      throw new UtilsException(
          AuthException.ERR_AUTH_MSS_003, AuthException.ERR_AUTH_MSS_003.getMessage());
    }

    User user = authMapper.mapUserEntityToUser(userEntity);
    user.setPassword(null);
    user.setAuthToken(tokenService.generateToken(user));

    String message = "Login Successfully! Welcome back " + user.getUsername() + "!";

    Response response =
        new Response(HttpStatus.OK.value(), message, CorrelationIdUtils.getCorrelationId(), user);

    return ResponseEntity.ok(response);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public ResponseEntity<Response> forgotPassword(String email) throws UtilsException {
    UserEntity userEntity = iAuthDAO.findUserEntityByEmail(email);
    if (userEntity == null) {
      LOG.error("User not found");
      throw new UtilsException(
          AuthException.ERR_AUTH_MSS_006, AuthException.ERR_AUTH_MSS_006.getMessage());
    }
    String token = UUID.randomUUID().toString();
    userEntity.setTokenReset(token);
    iAuthDAO.save(userEntity);

    // Send Email
    EmailContent emailContent =
        EmailContent.builder()
            .subject("MoneyStats - Reset your Password")
            .to(email)
            .sentDate(new Date())
            .build();
    Map<String, String> param = new HashMap<>();
    param.put("{{RESET_URL}}", feUrl + "/auth/resetPassword/token/" + token);
    EmailResponse responseEm =
        emailSenderService.sendEmail(EmailContent.RESET_TEMPLATE, param, emailContent);
    responseEm.setToken(token);

    String message = "Email Sent! Check your email address!";

    Response response =
        new Response(
            HttpStatus.OK.value(), message, CorrelationIdUtils.getCorrelationId(), responseEm);

    return ResponseEntity.ok(response);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public ResponseEntity<Response> resetPassword(String password, String token)
      throws UtilsException {
    UserEntity userEntity = iAuthDAO.findUserEntityByTokenReset(token);
    if (userEntity == null) {
      LOG.error("User not found");
      throw new UtilsException(
          AuthException.ERR_AUTH_MSS_005, AuthException.ERR_AUTH_MSS_005.getMessage());
    }
    if (userEntity.getUpdateDate().plusDays(1).isBefore(LocalDateTime.now())) {
      LOG.error("Token Expired");
      throw new UtilsException(
          AuthException.ERR_AUTH_MSS_005, AuthException.ERR_AUTH_MSS_005.getMessage());
    }
    userEntity.setPassword(bCryptPasswordEncoder.encode(password));
    userEntity.setTokenReset(null);

    UserEntity saved = iAuthDAO.save(userEntity);
    userEntity.setPassword(null);

    String message = "Password Updated!";

    Response response =
        new Response(
            HttpStatus.OK.value(),
            message,
            CorrelationIdUtils.getCorrelationId(),
            authMapper.mapUserEntityToUser(saved));

    return ResponseEntity.ok(response);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public ResponseEntity<Response> checkLoginFE(String authToken) {

    User userDTO = authMapper.mapUserEntityToUser(user);
    userDTO.setPassword(null);

    String message = "Welcome back " + user.getUsername() + "!";

    Response response =
        new Response(
            HttpStatus.OK.value(), message, CorrelationIdUtils.getCorrelationId(), userDTO);

    return ResponseEntity.ok(response);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public UserEntity checkLogin(String authToken) throws UtilsException {
    AuthToken token = new AuthToken();
    token.setAccessToken(authToken);
    User user = new User();
    try {
      user = tokenService.parseToken(token);
    } catch (UtilsException e) {
      throw new UtilsException(AuthException.ERR_AUTH_MSS_004, e.getMessage());
    }
    UserEntity userEntity =
        iAuthDAO.findUserEntityByUsernameOrEmail(user.getUsername(), user.getEmail());
    // userEntity.setPassword(null);

    if (userEntity == null) {
      LOG.error("User not found");
      throw new UtilsException(
          AuthException.ERR_AUTH_MSS_003, AuthException.ERR_AUTH_MSS_003.getMessage());
    }
    return userEntity;
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public AuthToken regenerateToken(UserEntity userEntity) throws UtilsException {
    User user = authMapper.mapUserEntityToUser(userEntity);
    AuthToken authToken = tokenService.generateToken(user);

    if (authToken == null) {
      LOG.error("Token not found");
      throw new UtilsException(
          AuthException.ERR_AUTH_MSS_003, AuthException.ERR_AUTH_MSS_003.getMessage());
    }
    return authToken;
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public ResponseEntity<Response> updateUserData(String authToken, User userToUpdate) {

    UserEntity userEntity = authMapper.mapUserToUserEntity(userToUpdate);
    if (userToUpdate.getPassword() != null && !userToUpdate.getPassword().isBlank()) {
      userEntity.setPassword(bCryptPasswordEncoder.encode(userToUpdate.getPassword()));
    } else {
      userEntity.setPassword(user.getPassword());
    }

    if (userToUpdate.getImgName() != null && !userToUpdate.getImgName().isEmpty()) {
      LOG.info("Building attachment with filename {}", userToUpdate.getImgName());
      Image image = imageService.getAttachment(userToUpdate.getImgName());
      imageService.removeAttachment(userToUpdate.getImgName());
      userEntity.setProfilePhoto(
              "data:"
                      + image.getContentType()
                      + ";base64,"
                      + Base64.getEncoder().encodeToString(image.getBody()));
    }

    UserEntity saved = iAuthDAO.save(userEntity);
    saved.setPassword(null);

    String message = "User updated!";

    Response response =
        new Response(HttpStatus.OK.value(), message, CorrelationIdUtils.getCorrelationId(), saved);

    return ResponseEntity.ok(response);
  }
}
