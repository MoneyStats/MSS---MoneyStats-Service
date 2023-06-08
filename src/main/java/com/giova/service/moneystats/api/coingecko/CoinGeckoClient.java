package com.giova.service.moneystats.api.coingecko;

import com.giova.service.moneystats.api.coingecko.dto.CoinGeckoMarketData;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@Logged
public class CoinGeckoClient {

  private final RestTemplate restTemplate = new RestTemplate();

  @Value(value = "${rest.client.coinGecko.url}")
  private String coinGeckoUrl;

  @Value(value = "${rest.client.coinGecko.marketDataUrl}")
  private String marketDataUrl;

  public ResponseEntity<CoinGeckoMarketData> getMarketData(String currency) {
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
    ResponseEntity<CoinGeckoMarketData> response =
        restTemplate.exchange(urlTemplate, HttpMethod.GET, request, CoinGeckoMarketData.class);

    return response;
  }
}
