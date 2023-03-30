package com.giova.service.moneystats.app.attachments;

import com.giova.service.moneystats.app.attachments.dto.Image;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ImageMapper {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_MAPPER)
  public Image fromPartToDto(MultipartFile file) throws UtilsException {
    try {
      return new Image(
          file.getName(),
          file.getOriginalFilename(),
          file.getContentType(),
          file.getSize(),
          file.getBytes());
    } catch (IOException e) {
      LOG.error("Error on converting Attachment: {}", e.getMessage());
      throw new UtilsException(
          ImageException.ERR_IMG_MSS_001,
          ImageException.ERR_IMG_MSS_001.getMessage()
              + " with filename: "
              + file.getOriginalFilename(),
          e.getMessage());
    }
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_MAPPER)
  public MultipartFile fromDtoToPart(Image image) {
    MultipartFile file =
        new MockMultipartFile(
            image.getName(), image.getFileName(), image.getContentType(), image.getBody());
    return file;
  }
}
