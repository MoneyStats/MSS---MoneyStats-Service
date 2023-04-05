package com.giova.service.moneystats.app.attachments;

import com.giova.service.moneystats.app.attachments.dto.Image;
import com.giova.service.moneystats.generic.Response;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.github.giovannilamarmora.utils.interceptors.correlationID.CorrelationIdUtils;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Logged
public class ImageService {

  public static Map<String, Image> attachmentMap = new HashMap<>();
  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  @Autowired private ImageMapper mapper;

  @Cacheable(cacheNames = "attachment")
  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public ResponseEntity<Response> saveAttachmentDto(MultipartFile file) throws UtilsException {
    Image attachment;
    if (file == null || file.isEmpty()) {
      LOG.error("The file you have been passed is invalid");
      throw new UtilsException(
          ImageException.ERR_IMG_MSS_001,
          "The file you have been passed is invalid",
          ImageException.ERR_IMG_MSS_001.getMessage());
    }
    try {
      attachment = mapper.fromPartToDto(file);
    } catch (UtilsException e) {
      LOG.error("Error on mapping attachment");
      throw new UtilsException(
          ImageException.ERR_IMG_MSS_001, "Error on mapping attachment", e.getMessage());
    }
    attachmentMap.put(attachment.getFileName(), attachment);

    String message = "Attachment " + attachment.getName() + " Successfully uploaded!";

    Response response =
        new Response(
            HttpStatus.OK.value(), message, CorrelationIdUtils.getCorrelationId(), attachment);
    return ResponseEntity.ok(response);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public void removeAttachment(String filename) {
    attachmentMap.remove(filename);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public Image getAttachment(String filename) {
    return attachmentMap.get(filename);
  }
}
