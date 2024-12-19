package com.giova.service.moneystats.config;

import static io.github.giovannilamarmora.utils.exception.UtilsException.getExceptionResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.giova.service.moneystats.api.accessSphere.dto.UserInfoResponse;
import com.giova.service.moneystats.authentication.AuthException;
import com.giova.service.moneystats.authentication.AuthService;
import com.giova.service.moneystats.exception.config.ExceptionMap;
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
  private final UserInfoResponse userInfo;

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
    String sessionId = request.getHeaders().getFirst("Session-ID");
    ExceptionResponse exceptionResponse = new ExceptionResponse();

    if (isEmpty(authToken) || isEmpty(sessionId)) {
      LOG.error(
          "Not Found Data: Auth-Token is {} and Session ID is {}",
          isEmpty(authToken) ? "[EMPTY]" : "[NOT_EMPTY]",
          isEmpty(sessionId) ? "[EMPTY]" : "[NOT_EMPTY]");
      return errorResponse(request, response, exceptionResponse, ExceptionMap.ERR_AUTH_MSS_008);
    }

    return authService
        .authorize(authToken, sessionId)
        .flatMap(
            userInfoResponse -> {
              // Imposta i dati dell'utente nel contesto
              setUserInContext(userInfoResponse);

              // Imposta il nuovo token nell'header
              response.getHeaders().set(HttpHeaders.AUTHORIZATION, authToken);
              response.getHeaders().set("Session-ID", sessionId);
              settingTracing(request, response);

              LOG.debug("Ending Filter Authentication");
              return chain.filter(exchange);
            })
        .onErrorResume(
            throwable -> {
              LOG.error("Authorization failed: {}", throwable.getMessage());
              if (throwable instanceof AuthException error) {
                return errorResponse(
                    request, response, exceptionResponse, (ExceptionMap) error.getExceptionCode());
              }
              return errorResponse(
                  request, response, exceptionResponse, ExceptionMap.ERR_AUTH_MSS_008);
            });
  }

  private void settingTracing(ServerHttpRequest request, ServerHttpResponse response) {
    // Leggi gli header dalla richiesta
    String spanId = request.getHeaders().getFirst("Span-ID");
    String traceId = request.getHeaders().getFirst("Trace-ID");
    String parentId = request.getHeaders().getFirst("Parent-ID");

    // Imposta gli stessi header nella risposta
    if (spanId != null) {
      response.getHeaders().set("Span-ID", spanId);
    }
    if (traceId != null) {
      response.getHeaders().set("Trace-ID", traceId);
    }
    if (parentId != null) {
      response.getHeaders().set("Parent-ID", parentId);
    }
  }

  protected boolean shouldNotFilter(ServerHttpRequest request) {
    String method = request.getMethod().name();
    if (method.equalsIgnoreCase(HttpMethod.OPTIONS.name())) {
      LOG.debug("CORS Preflight request detected");
      return true; // Ignora la richiesta OPTIONS
    }
    String path = request.getPath().value();
    if (shouldNotFilter.stream()
        .noneMatch(endpoint -> PatternMatchUtils.simpleMatch(endpoint, path)))
      LOG.debug("Filtering Authentication on {}", path);
    return shouldNotFilter.stream()
        .anyMatch(endpoint -> PatternMatchUtils.simpleMatch(endpoint, path));
  }

  private void setUserInContext(UserInfoResponse user) {
    BeanUtils.copyProperties(user, this.userInfo);
  }

  private Mono<Void> errorResponse(
      ServerHttpRequest request,
      ServerHttpResponse response,
      ExceptionResponse exceptionResponse,
      ExceptionMap exception) {

    exceptionResponse =
        getExceptionResponse(
            new AuthException(exception, exception.getMessage()), request, exception);
    exceptionResponse.setSpanId(TraceUtils.getSpanID());
    exceptionResponse.getError().setStackTrace(null);
    exceptionResponse.getError().setMessage(exceptionResponse.getError().getExceptionMessage());
    exceptionResponse.getError().setExceptionMessage(null);

    // Aggiungi header CORS
    response
        .getHeaders()
        .set("Access-Control-Allow-Origin", request.getHeaders().getFirst("origin"));
    response.getHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
    response.getHeaders().set("Access-Control-Allow-Headers", "Authorization, Content-Type");
    response.getHeaders().set("Access-Control-Allow-Credentials", "true");

    response.setStatusCode(HttpStatus.FORBIDDEN);
    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

    DataBuffer responseBuffer =
        new DefaultDataBufferFactory()
            .wrap(Mapper.writeObjectToString(exceptionResponse).getBytes());
    return response.writeWith(Mono.just(responseBuffer));
  }
}
