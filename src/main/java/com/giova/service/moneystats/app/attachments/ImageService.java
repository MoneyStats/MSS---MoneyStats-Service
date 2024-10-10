package com.giova.service.moneystats.app.attachments;

import com.giova.service.moneystats.app.attachments.dto.Image;
import com.giova.service.moneystats.exception.ExceptionMap;
import io.github.giovannilamarmora.utils.context.TraceUtils;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.generic.Response;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  @Cacheable(cacheNames = "attachment")
  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public ResponseEntity<Response> saveAttachmentDto(MultipartFile file) throws UtilsException {
    Image attachment;
    if (file == null || file.isEmpty()) {
      LOG.error("The file you have been passed is invalid");
      throw new ImageException(
          "The file you have been passed is invalid", ExceptionMap.ERR_IMG_MSS_001.getMessage());
    }
    try {
      attachment = ImageMapper.fromPartToDto(file);
    } catch (UtilsException e) {
      LOG.error("Error on mapping attachment");
      throw new ImageException("Error on mapping attachment", e.getMessage());
    }
    attachmentMap.put(attachment.getFileName(), attachment);

    String message = "Attachment " + attachment.getName() + " Successfully uploaded!";

    Response response =
        new Response(HttpStatus.OK.value(), message, TraceUtils.getSpanID(), attachment);
    return ResponseEntity.ok(response);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public void removeAttachment(String filename) {
    attachmentMap.remove(filename);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public Image getAttachment(String filename) {
    return attachmentMap.get(filename);
  }
}
