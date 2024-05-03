package com.giova.service.moneystats.config;

import static io.github.giovannilamarmora.utils.exception.UtilsException.getExceptionResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.giova.service.moneystats.authentication.AuthException;
import com.giova.service.moneystats.authentication.AuthService;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import com.giova.service.moneystats.authentication.token.dto.AuthToken;
import com.giova.service.moneystats.exception.ExceptionMap;
import io.github.giovannilamarmora.utils.exception.dto.ExceptionResponse;
import io.github.giovannilamarmora.utils.interceptors.correlationID.CorrelationIdUtils;
import io.github.giovannilamarmora.utils.utilities.Utilities;
import java.io.IOException;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.PatternMatchUtils;
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
      // filterChain.doFilter(request, response);
      return;
    }
    if (isEmpty(authToken)) {
      LOG.error("Auth-Token not found");
      errorResponse(request, response, exceptionResponse);
      return;
    }
    UserEntity checkUser = new UserEntity();
    AuthToken generateToken = new AuthToken();
    try {
      checkUser = authService.checkLogin(authToken);
      generateToken = authService.regenerateToken(checkUser);
    } catch (Exception e) {
      LOG.error(
          "Auth-Token error on checking user or regenerate token, message: {}", e.getMessage());
      errorResponse(request, response, exceptionResponse);
      return;
    }
    setUserInContext(checkUser);
    // setTokenInCookie(generateToken, response);
    response.setHeader(HttpHeaders.AUTHORIZATION, generateToken.getAccessToken());
    LOG.debug("Ending Filter Authentication");
    filterChain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    if (shouldNotFilter.stream()
        .noneMatch(endpoint -> PatternMatchUtils.simpleMatch(endpoint, path)))
      LOG.debug("Filtering Authentication on {}", path);
    return shouldNotFilter.stream()
        .anyMatch(endpoint -> PatternMatchUtils.simpleMatch(endpoint, path));
  }

  private void setUserInContext(UserEntity user) {
    BeanUtils.copyProperties(user, this.user);
  }

  private void errorResponse(
      HttpServletRequest request, HttpServletResponse response, ExceptionResponse exceptionResponse)
      throws IOException {
    exceptionResponse =
        getExceptionResponse(
            new AuthException(
                ExceptionMap.ERR_AUTH_MSS_008, ExceptionMap.ERR_AUTH_MSS_008.getMessage()),
            request,
            ExceptionMap.ERR_AUTH_MSS_008);
    exceptionResponse.setCorrelationId(CorrelationIdUtils.getCorrelationId());
    exceptionResponse.getError().setStackTrace(null);
    response.setStatus(HttpStatus.FORBIDDEN.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.getWriter().write(Utilities.convertObjectToJson(exceptionResponse));
    // response.reset();
  }
}
