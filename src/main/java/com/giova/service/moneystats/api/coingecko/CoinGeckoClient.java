package com.giova.service.moneystats.api.coingecko;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Logged
public class CoinGeckoClient {

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

  @LogInterceptor(type = LogTimeTracker.ActionType.EXTERNAL)
  public Mono<ResponseEntity<List<CoinGeckoMarketData>>> getMarketData(
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

    return webClientRest.performList(
        HttpMethod.GET,
        UtilsUriBuilder.buildUri(marketDataUrl, params),
        null,
        headers,
        CoinGeckoMarketData.class);
  }
}
