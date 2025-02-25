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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Logged
public class GithubClient {

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

  @LogInterceptor(type = LogTimeTracker.ActionType.EXTERNAL)
  public Mono<ResponseEntity<Object>> openGithubIssues(GithubIssues githubIssues) {
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.ACCEPT, "application/vnd.github+json");
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + githubAuthToken);
    headers.add("X-GitHub-Api-Version", "2022-11-28");

    return webClientRest.perform(
        HttpMethod.POST,
        UtilsUriBuilder.buildUri(openGithubIssuesUrl, null),
        githubIssues,
        headers,
        Object.class);
  }
}
