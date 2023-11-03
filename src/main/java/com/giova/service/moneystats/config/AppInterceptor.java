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
  @Autowired private AuthService authService;

  private static boolean isEmpty(String value) {
    return value == null || value.isBlank();
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String authToken = request.getHeader(HttpHeaders.AUTHORIZATION);
    ExceptionResponse exceptionResponse = new ExceptionResponse();
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
      exceptionResponse =
          getExceptionResponse(e, request, e.getExceptionCode(), e.getExceptionCode().getStatus());
      exceptionResponse.setCorrelationId(CorrelationIdUtils.getCorrelationId());
      response.setStatus(e.getExceptionCode().getStatus().value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.getWriter().write(convertObjectToJson(exceptionResponse));
      return;
    }
    setUserInContext(checkUser);
    response.setHeader(HttpHeaders.AUTHORIZATION, generateToken.getAccessToken());
    filterChain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    List<String> notFiltering =
        List.of(
            "/v1/auth/sign-up",
            "/v1/auth/login",
            "/v1/auth/forgot-password",
            "/v1/auth/reset-password",
            "/v1/app/report/bug",
            "/v1/app/contact",
            "/v1/upload/attachment");
    return notFiltering.contains(path);
  }

  public String convertObjectToJson(Object object) throws JsonProcessingException {
    if (object == null) {
      return null;
    }
    return objectMapper.writeValueAsString(object);
  }

  private void setUserInContext(UserEntity user) {
    BeanUtils.copyProperties(user, this.user);
  }
}
