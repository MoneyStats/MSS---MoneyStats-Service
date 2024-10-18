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
import io.github.giovannilamarmora.utils.context.TraceUtils;
import io.github.giovannilamarmora.utils.exception.dto.ExceptionResponse;
import io.github.giovannilamarmora.utils.utilities.Mapper;
import java.util.List;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.PatternMatchUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
public class AppInterceptor implements WebFilter {
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
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    if (shouldNotFilter(exchange.getRequest())) return chain.filter(exchange);
    LOG.debug("Starting Filter Authentication");
    ServerHttpRequest request = exchange.getRequest();
    ServerHttpResponse response = exchange.getResponse();
    String authToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    ExceptionResponse exceptionResponse = new ExceptionResponse();

    if (isEmpty(authToken)) {
      LOG.error("Auth-Token not found");
      return errorResponse(request, response, exceptionResponse);
    }
    UserEntity checkUser;
    AuthToken generateToken;
    try {
      checkUser = authService.checkLogin(authToken);
      generateToken = authService.regenerateToken(checkUser);
    } catch (Exception e) {
      LOG.error(
          "Auth-Token error on checking user or regenerate token, message: {}", e.getMessage());
      return errorResponse(request, response, exceptionResponse);
    }
    setUserInContext(checkUser);
    // setTokenInCookie(generateToken, response);
    response.getHeaders().set(HttpHeaders.AUTHORIZATION, generateToken.getAccessToken());
    LOG.debug("Ending Filter Authentication");
    return chain.filter(exchange);
  }

  protected boolean shouldNotFilter(ServerHttpRequest request) {
    String method = request.getMethod().name();
    if (method.equalsIgnoreCase(HttpMethod.OPTIONS.name())) return true;
    String path = request.getPath().value();
    if (shouldNotFilter.stream()
        .noneMatch(endpoint -> PatternMatchUtils.simpleMatch(endpoint, path)))
      LOG.debug("Filtering Authentication on {}", path);
    return shouldNotFilter.stream()
        .anyMatch(endpoint -> PatternMatchUtils.simpleMatch(endpoint, path));
  }

  private void setUserInContext(UserEntity user) {
    BeanUtils.copyProperties(user, this.user);
  }

  private Mono<Void> errorResponse(
      ServerHttpRequest request, ServerHttpResponse response, ExceptionResponse exceptionResponse) {
    exceptionResponse =
        getExceptionResponse(
            new AuthException(
                ExceptionMap.ERR_AUTH_MSS_008, ExceptionMap.ERR_AUTH_MSS_008.getMessage()),
            request,
            ExceptionMap.ERR_AUTH_MSS_008);
    exceptionResponse.setSpanId(TraceUtils.getSpanID());
    exceptionResponse.getError().setStackTrace(null);
    response.setStatusCode(HttpStatus.FORBIDDEN);
    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
    DataBuffer responseBuffer =
        new DefaultDataBufferFactory()
            .wrap(Mapper.writeObjectToString(exceptionResponse).getBytes());
    return response.writeWith(Mono.just(responseBuffer));
    // response.reset();
  }
}
