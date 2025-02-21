package com.giova.service.moneystats.app.settings;

import com.giova.service.moneystats.app.model.GithubIssues;
import com.giova.service.moneystats.app.model.Support;
import com.giova.service.moneystats.app.wallet.dto.Wallet;
import com.giova.service.moneystats.authentication.service.AuthCacheService;
import com.giova.service.moneystats.config.roles.AppRole;
import com.giova.service.moneystats.config.roles.Roles;
import com.giova.service.moneystats.scheduler.CronCachingReset;
import io.github.giovannilamarmora.utils.context.TraceUtils;
import io.github.giovannilamarmora.utils.generic.Response;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Logged
@RestController
@RequestMapping("/v1/settings")
@CrossOrigin(origins = "*")
@Tag(name = "Settings", description = "API to Handle the Settings App")
public class SettingsControllerImpl implements SettingsController {

  @Autowired private SettingsService settingsService;
  @Autowired private CronCachingReset cronCachingReset;
  @Autowired private AuthCacheService authCacheService;

  /**
   * API to report a BUG
   *
   * @param githubIssues Request Data
   * @return Response
   */
  @Override
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public Mono<ResponseEntity<Response>> bugReport(GithubIssues githubIssues) {
    return settingsService.reportBug(githubIssues);
  }

  /**
   * API to make the backup dell'app
   *
   * @param token Of the user
   * @return Response JSON
   */
  @Override
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public ResponseEntity<Response> backupData(String token) {
    return settingsService.backupData();
  }

  /**
   * API to Restore Data
   *
   * @param wallets To be restored
   * @param token of the User
   * @return Confirmation Response
   */
  @Override
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public ResponseEntity<Response> restoreData(List<Wallet> wallets, String token) {
    return settingsService.restoreData(wallets);
  }

  /**
   * API to contact support
   *
   * @param support body to be sent
   * @return Response
   */
  @Override
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public Mono<ResponseEntity<Response>> contactUs(Support support) {
    return settingsService.contactSupport(support);
  }

  /**
   * API to clean the cache of the app
   *
   * @param token ADMIN token
   * @return Cache cleaned
   */
  @Override
  @Roles(value = AppRole.MONEY_STATS_ADMIN)
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public ResponseEntity<Response> cleanCache(
      ServerWebExchange exchange, Boolean isUserInfo, String token) {
    authCacheService.evictAuthenticationCache(exchange);
    if (!isUserInfo) cronCachingReset.scheduleCleanCache();
    Response response =
        new Response(
            HttpStatus.OK.value(), "Cache cleaned successfully", TraceUtils.getSpanID(), null);
    return ResponseEntity.ok(response);
  }
}
