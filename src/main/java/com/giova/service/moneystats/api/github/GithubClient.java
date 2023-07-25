package com.giova.service.moneystats.api.github;

import com.giova.service.moneystats.app.model.GithubIssues;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.github.giovannilamarmora.utils.webClient.UtilsUriBuilder;
import io.github.giovannilamarmora.utils.webClient.WebClientRest;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Logged
public class GithubClient {

  private final RestTemplate restTemplate = new RestTemplate();
  private final WebClientRest webClientRest = new WebClientRest();

  @Value(value = "${rest.client.github.baseUrl}")
  private String githubBaseUrl;

  @Value(value = "${rest.client.github.issuesUrl}")
  private String openGithubIssuesUrl;

  @Value(value = "${rest.client.github.authToken}")
  private String githubAuthToken;

  @Autowired private WebClient.Builder builder;

  @PostConstruct
  void init() {
    webClientRest.setBaseUrl(githubBaseUrl);
    webClientRest.init(builder);
  }

  /**
   * DEPRECATED
   *
   * @param githubIssues
   * @return
   */
  @LogInterceptor(type = LogTimeTracker.ActionType.APP_EXTERNAL)
  public ResponseEntity<Object> openGithubIssuesRest(GithubIssues githubIssues) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Accept", "application/vnd.github+json");
    headers.add("Authorization", "Bearer " + githubAuthToken);
    headers.add("X-GitHub-Api-Version", "2022-11-28");
    HttpEntity<GithubIssues> request = new HttpEntity<>(githubIssues, headers);

    ResponseEntity<Object> response =
        restTemplate.exchange(openGithubIssuesUrl, HttpMethod.POST, request, Object.class);

    return response;
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_EXTERNAL)
  public ResponseEntity<Object> openGithubIssues(GithubIssues githubIssues) {
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.ACCEPT, "application/vnd.github+json");
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + githubAuthToken);
    headers.add("X-GitHub-Api-Version", "2022-11-28");

    Mono<ResponseEntity<Object>> response =
        webClientRest.perform(
            HttpMethod.POST,
            UtilsUriBuilder.toBuild()
                .set(openGithubIssuesUrl, null),
            githubIssues,
            headers,
            Object.class);

    return response.block();
  }
}
