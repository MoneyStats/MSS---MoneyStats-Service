package com.giova.service.moneystats.api.emailSender;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.giova.service.moneystats.api.emailSender.dto.EmailContent;
import com.giova.service.moneystats.api.emailSender.dto.EmailResponse;
import com.giova.service.moneystats.exception.config.ExceptionMap;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import reactor.core.publisher.Mono;

@Logged
@Component
public class EmailSenderService {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  @Autowired private EmailSenderClient emailSenderClient;

  @Autowired private ResourceLoader resourceLoader;

  public static String asString(Resource resource) throws UtilsException {
    try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
      return FileCopyUtils.copyToString(reader);
    } catch (IOException e) {
      throw new EmailException(
          ExceptionMap.ERR_EMAIL_SEND_002, ExceptionMap.ERR_EMAIL_SEND_002.getMessage());
    }
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public Mono<EmailResponse> sendEmail(
      String templatePath, Map<String, String> params, EmailContent emailContent)
      throws UtilsException {
    Resource resource = resourceLoader.getResource("classpath:templates/" + templatePath);
    String template = asString(resource);
    for (String key : params.keySet()) {
      template = template.replace(key, params.get(key));
    }

    emailContent.setText(template);
    return emailSenderClient
        .sendEmail(emailContent)
        .map(
            emailResponseResponseEntity -> {
              EmailResponse responseEm = emailResponseResponseEntity.getBody();
              if (responseEm == null) {
                LOG.error("Error on sending email");
                throw new EmailException(
                    ExceptionMap.ERR_EMAIL_SEND_001.getMessage()
                        + "Error on sending email for "
                        + emailContent.getFrom());
              }
              return responseEm;
            });
  }
}
