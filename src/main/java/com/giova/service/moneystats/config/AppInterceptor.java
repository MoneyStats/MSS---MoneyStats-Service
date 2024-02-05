package com.giova.service.moneystats.config;

import static io.github.giovannilamarmora.utils.exception.UtilsException.getExceptionResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.giova.service.moneystats.authentication.AuthService;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import com.giova.service.moneystats.authentication.token.dto.AuthToken;
import com.nimbusds.jose.JOSEException;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.exception.dto.ExceptionResponse;
import io.github.giovannilamarmora.utils.interceptors.correlationID.CorrelationIdUtils;
import io.github.giovannilamarmora.utils.utilities.FilesUtils;
import io.github.giovannilamarmora.utils.utilities.Utilities;
import io.github.giovannilamarmora.utils.web.CookieManager;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@AllArgsConstructor
public class AppInterceptor extends OncePerRequestFilter {
  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  private final ObjectMapper objectMapper =
      new ObjectMapper()
          .registerModule(new JavaTimeModule())
          .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  private final UserEntity user;

  @Value(value = "${app.shouldNotFilter}")
  private List<String> shouldNotFilter;

  @Autowired private AuthService authService;

  private static boolean isEmpty(String value) {
    return value == null || value.isBlank();
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    LOG.debug("Starting Filter Authentication");
    String authToken = request.getHeader(HttpHeaders.AUTHORIZATION);
    ExceptionResponse exceptionResponse = new ExceptionResponse();
    if (shouldNotFilter(request)) {
      filterChain.doFilter(request, response);
      return;
    }
    if (isEmpty(authToken)) {
      LOG.error("Auth-Token not found");
      response.reset();
      filterChain.doFilter(request, response);
      return;
    }
    UserEntity checkUser = new UserEntity();
    AuthToken generateToken = new AuthToken();
    try {
      checkUser = authService.checkLogin(authToken);
      generateToken = authService.regenerateToken(checkUser);
    } catch (JOSEException e) {
      throw new UtilsException();
    } catch (UtilsException e) {
      LOG.error(
          "Auth-Token error on checking user or regenerate token, message: {}", e.getMessage());
      exceptionResponse = getExceptionResponse(e, request, e.getExceptionCode());
      exceptionResponse.setCorrelationId(CorrelationIdUtils.getCorrelationId());
      response.setStatus(e.getExceptionCode().getStatus().value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.getWriter().write(Utilities.convertObjectToJson(exceptionResponse));
      return;
    }
    setUserInContext(checkUser);
    // setUserAndTokenInCookie(checkUser, generateToken, response);
    response.setHeader(HttpHeaders.AUTHORIZATION, generateToken.getAccessToken());
    LOG.debug("Ending Filter Authentication");
    filterChain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    if (shouldNotFilter.stream().noneMatch(endpoint -> FilesUtils.matchPath(path, endpoint)))
      LOG.debug("Filtering Authentication on {}", path);
    // return shouldNotFilter.contains(path);
    return shouldNotFilter.stream().anyMatch(endpoint -> FilesUtils.matchPath(path, endpoint));
  }

  private void setUserInContext(UserEntity user) {
    BeanUtils.copyProperties(user, this.user);
  }

  private void setUserAndTokenInCookie(
      UserEntity user, AuthToken token, HttpServletResponse response)
      throws JsonProcessingException {
    String userAsString =
        Base64.getEncoder().encodeToString(objectMapper.writeValueAsString(user).getBytes());
    CookieManager.setCookieInResponse(UserEntity.USER_COOKIE, userAsString, response);
    CookieManager.setCookieInResponse(AuthToken.JWE_TOKEN_COOKIE, token.getAccessToken(), response);
  }
}
