package com.giova.service.moneystats.api.forex.anyApi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.giova.service.moneystats.api.forex.anyApi.dto.Rates;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.github.giovannilamarmora.utils.webClient.UtilsUriBuilder;
import io.github.giovannilamarmora.utils.webClient.WebClientRest;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Logged
public class AnyAPIClient {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
  private final WebClientRest webClientRest = new WebClientRest();

  @Value(value = "${rest.client.anyApi.baseUrl}")
  private String anyApiUrl;

  @Value(value = "${rest.client.anyApi.rates}")
  private String getRates;

  @Value(value = "${rest.client.anyApi.apiKey}")
  private String apiKey;

  @Autowired private WebClient.Builder builder;

  @PostConstruct
  void init() {
    webClientRest.setBaseUrl(anyApiUrl);
    webClientRest.init(builder);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.EXTERNAL)
  public ResponseEntity<Rates> getAnyApiForexData(String currency) {
    Map<String, Object> params = new HashMap<>();
    params.put("base", currency);
    params.put("apiKey", apiKey);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

    Mono<ResponseEntity<Rates>> response =
        webClientRest.perform(
            HttpMethod.GET, UtilsUriBuilder.buildUri(getRates, params), null, headers, Rates.class);
    return validateAndGetExchangeRates(response);
  }

  private ResponseEntity<Rates> validateAndGetExchangeRates(
      Mono<ResponseEntity<Rates>> exchangeRatesMono) {
    ResponseEntity<Rates> exchangeRates = exchangeRatesMono.block();

    if (exchangeRates == null || exchangeRates.getBody() == null) {
      LOG.error("ExchangeRate Response is null");
      throw new AnyAPIException("The Response of WebClient body is null");
    }

    return ResponseEntity.status(exchangeRates.getStatusCode())
        .headers(exchangeRates.getHeaders())
        .body(exchangeRates.getBody());
  }
}
