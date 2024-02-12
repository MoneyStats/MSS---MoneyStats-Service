package com.giova.service.moneystats.api.coingecko;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giova.service.moneystats.api.coingecko.dto.CoinGeckoMarketData;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.github.giovannilamarmora.utils.webClient.UtilsUriBuilder;
import io.github.giovannilamarmora.utils.webClient.WebClientRest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Component
@Logged
public class CoinGeckoClient {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  private final RestTemplate restTemplate = new RestTemplate();
  private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
  private final WebClientRest webClientRest = new WebClientRest();

  @Value(value = "${rest.client.coinGecko.url}")
  private String coinGeckoUrl;

  @Value(value = "${rest.client.coinGecko.marketDataUrl}")
  private String marketDataUrl;

  @Autowired private WebClient.Builder builder;

  @PostConstruct
  void init() {
    webClientRest.setBaseUrl(coinGeckoUrl);
    webClientRest.init(builder);
  }

  @Deprecated
  @LogInterceptor(type = LogTimeTracker.ActionType.EXTERNAL)
  public ResponseEntity<List<CoinGeckoMarketData>> getMarketDataRest(String currency) {
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    HttpEntity<?> request = new HttpEntity<>(headers);
    String urlTemplate =
        UriComponentsBuilder.fromHttpUrl(coinGeckoUrl + marketDataUrl)
            .queryParam("vs_currency", currency)
            .queryParam("order", "market_cap_desc")
            .queryParam("per_page", 200)
            .queryParam("page", 1)
            .queryParam("sparkline", false)
            .queryParam("locale", "it")
            .encode()
            .toUriString();
    ResponseEntity<Object> response =
        restTemplate.exchange(urlTemplate, HttpMethod.GET, request, Object.class);
    List<CoinGeckoMarketData> list =
        mapper.convertValue(response.getBody(), new TypeReference<List<CoinGeckoMarketData>>() {});

    return ResponseEntity.ok(list);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.EXTERNAL)
  public ResponseEntity<List<CoinGeckoMarketData>> getMarketData(
      String currency, Integer page, Boolean isStable) {
    Map<String, Object> params = new HashMap<>();
    params.put("vs_currency", currency);
    params.put("order", "market_cap_desc");
    if (isStable) params.put("per_page", 100);
    else params.put("per_page", 250);
    params.put("page", page);
    params.put("sparkline", false);
    params.put("locale", "it");
    if (isStable) params.put("category", "stablecoins");

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

    Mono<ResponseEntity<Object>> response =
        webClientRest.perform(
            HttpMethod.GET,
            UtilsUriBuilder.toBuild().set(marketDataUrl, params),
            null,
            headers,
            Object.class);

    List<CoinGeckoMarketData> list =
        mapper.convertValue(
            response.block().getBody(), new TypeReference<List<CoinGeckoMarketData>>() {});

    return ResponseEntity.ok(list);
  }
}
