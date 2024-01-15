package com.giova.service.moneystats.api.emailSender;

import com.giova.service.moneystats.api.emailSender.dto.EmailContent;
import com.giova.service.moneystats.api.emailSender.dto.EmailResponse;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.github.giovannilamarmora.utils.webClient.UtilsUriBuilder;
import io.github.giovannilamarmora.utils.webClient.WebClientRest;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
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
public class EmailSenderClient {

  private final RestTemplate restTemplate = new RestTemplate();
  private final WebClientRest webClientRest = new WebClientRest();

  @Value(value = "${rest.client.emailSender.url}")
  private String emailSenderUrl;

  @Value(value = "${rest.client.emailSender.sendEmailUrl}")
  private String sendEmailUrl;

  @Autowired private WebClient.Builder builder;

  @PostConstruct
  void init() {
    webClientRest.setBaseUrl(emailSenderUrl);
    webClientRest.init(builder);
  }

  @Deprecated
  @LogInterceptor(type = LogTimeTracker.ActionType.EXTERNAL)
  public ResponseEntity<EmailResponse> sendEmailRest(EmailContent emailContent) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Accept", "application/json");
    HttpEntity<EmailContent> request = new HttpEntity<>(emailContent, headers);
    String urlTemplate =
        UriComponentsBuilder.fromHttpUrl(emailSenderUrl + sendEmailUrl)
            .queryParam("htmlText", true)
            .encode()
            .toUriString();
    ResponseEntity<EmailResponse> response =
        restTemplate.exchange(urlTemplate, HttpMethod.POST, request, EmailResponse.class);

    return response;
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.EXTERNAL)
  public ResponseEntity<EmailResponse> sendEmail(EmailContent emailContent) {
    Map<String, Object> params = new HashMap<>();
    params.put("htmlText", true);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

    Mono<ResponseEntity<EmailResponse>> response =
        webClientRest.perform(
            HttpMethod.POST,
            UtilsUriBuilder.toBuild().set(sendEmailUrl, params),
            emailContent,
            headers,
            EmailResponse.class);

    return response.block();
  }
}
