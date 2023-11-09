package com.giova.service.moneystats.api.forex;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.giova.service.moneystats.api.forex.dto.ExchangeRates;
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
public class ExchangeRatesClient {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
  private final WebClientRest webClientRest = new WebClientRest();

  @Value(value = "${rest.client.exchangeRates.baseUrl}")
  private String exchangeRatesUrl;

  @Value(value = "${rest.client.exchangeRates.rates}")
  private String getRates;

  @Value(value = "${rest.client.exchangeRates.apiKey}")
  private String apiKey;

  @Autowired private WebClient.Builder builder;

  @PostConstruct
  void init() {
    webClientRest.setBaseUrl(exchangeRatesUrl);
    webClientRest.init(builder);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_EXTERNAL)
  public ResponseEntity<ExchangeRates> getForexData(String currency) {
    Map<String, Object> params = new HashMap<>();
    params.put("source", currency);
    params.put("access_key", apiKey);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

    Mono<ResponseEntity<ExchangeRates>> response =
        webClientRest.perform(
            HttpMethod.GET,
            UtilsUriBuilder.toBuild().set(getRates, params),
            null,
            headers,
            ExchangeRates.class);
    return validateAndGetExchangeRates(response);
  }

  private ResponseEntity<ExchangeRates> validateAndGetExchangeRates(
      Mono<ResponseEntity<ExchangeRates>> exchangeRatesMono) {
    ResponseEntity<ExchangeRates> exchangeRates = exchangeRatesMono.block();

    if (exchangeRates == null || exchangeRates.getBody() == null) {
      LOG.error("ExchangeRate Response is null");
      throw new ExchangeRatesException("The Response of WebClient body is null");
    }
    Map<String, Double> quotes = new HashMap<>();
    Map<String, Double> quotesToBeEdited = exchangeRates.getBody().getQuotes();
    quotesToBeEdited
        .keySet()
        .forEach(
            key ->
                quotes.put(
                    getEditedKey(exchangeRates.getBody().getSource(), key),
                    quotesToBeEdited.get(key)));
    exchangeRates.getBody().setQuotes(quotes);
    return ResponseEntity.status(exchangeRates.getStatusCode())
        .headers(exchangeRates.getHeaders())
        .body(exchangeRates.getBody());
  }

  private String getEditedKey(String currency, String key) {
    return key.replace(currency, "");
  }
}
