package com.giova.service.moneystats.app.settings;

import com.fasterxml.jackson.core.type.TypeReference;
import com.giova.service.moneystats.api.emailSender.EmailSenderService;
import com.giova.service.moneystats.api.emailSender.dto.EmailContent;
import com.giova.service.moneystats.api.github.GithubClient;
import com.giova.service.moneystats.app.model.GithubIssues;
import com.giova.service.moneystats.app.model.Support;
import com.giova.service.moneystats.app.wallet.WalletService;
import com.giova.service.moneystats.app.wallet.dto.Wallet;
import com.giova.service.moneystats.authentication.dto.UserData;
import io.github.giovannilamarmora.utils.context.TraceUtils;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.generic.Response;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.github.giovannilamarmora.utils.utilities.Mapper;
import io.github.giovannilamarmora.utils.utilities.Utilities;
import jakarta.transaction.Transactional;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Logged
@Service
@RequiredArgsConstructor
public class SettingsService {

  private final UserData user;
  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  @Autowired private GithubClient githubClient;
  @Autowired private WalletService walletService;
  @Autowired private EmailSenderService emailSenderService;

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public Mono<ResponseEntity<Response>> reportBug(GithubIssues githubIssues) {
    LOG.info("Bug to report: {}", Mapper.writeObjectToString(githubIssues));

    return githubClient
        .openGithubIssues(githubIssues)
        .flatMap(
            issues -> {
              String message = "Bug Reported!";

              Response response =
                  new Response(
                      HttpStatus.OK.value(), message, TraceUtils.getSpanID(), issues.getBody());
              return Mono.just(ResponseEntity.ok(response));
            });
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public ResponseEntity<Response> backupData() throws UtilsException {
    LOG.info("Backup data for user {}", user.getUsername());

    ResponseEntity<Response> getWallets = walletService.getAllWallets(false, true, true, true);
    if (Utilities.isNullOrEmpty(getWallets.getBody())) {
      LOG.error("There is no Wallet saved, backup aborted");
      Response response =
          new Response(
              HttpStatus.OK.value(),
              "There is no wallet to be saved, backup aborted",
              TraceUtils.getSpanID(),
              null);
      return ResponseEntity.ok(response);
    }

    List<Wallet> wallets =
        Mapper.convertObject(getWallets.getBody().getData(), new TypeReference<List<Wallet>>() {});

    String message = "Backup data completed!";

    Response response =
        new Response(
            HttpStatus.OK.value(),
            message,
            TraceUtils.getSpanID(),
            walletService.deleteWalletIds(wallets));

    return ResponseEntity.ok(response);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = Exception.class)
  public ResponseEntity<Response> restoreData(List<Wallet> wallet) {
    LOG.info("Restore data for user {}", user.getUsername());
    walletService.deleteWalletEntities();

    List<Wallet> restoreWallets = walletService.saveWalletEntities(wallet);

    String message = "Data successfully restored!";

    Response response =
        new Response(HttpStatus.OK.value(), message, TraceUtils.getSpanID(), restoreWallets);

    return ResponseEntity.ok(response);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public Mono<ResponseEntity<Response>> contactSupport(Support support) {
    // Send Email
    EmailContent emailContent =
        EmailContent.builder()
            .subject("MoneyStats - Contact Us!")
            .to(support.getEmail())
            .bbc("giovannilamarmora.working@gmail.com")
            .sentDate(new Date())
            .build();
    Map<String, String> param = new HashMap<>();
    param.put("{{NAME}}", support.getName());
    param.put("{{MESSAGE}}", support.getMessage());
    return emailSenderService
        .sendEmail(EmailContent.CONTACT_TEMPLATE, param, emailContent)
        .flatMap(
            emailResponse -> {
              String message = "Email Sent! Check your email address!";

              Response response =
                  new Response(
                      HttpStatus.OK.value(), message, TraceUtils.getSpanID(), emailResponse);

              return Mono.just(ResponseEntity.ok(response));
            });
  }
}
