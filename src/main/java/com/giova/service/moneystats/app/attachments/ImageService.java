package com.giova.service.moneystats.app.attachments;

import com.giova.service.moneystats.app.attachments.dto.Image;
import com.giova.service.moneystats.exception.config.ExceptionMap;
import com.giova.service.moneystats.utilities.Utils;
import io.github.giovannilamarmora.utils.context.TraceUtils;
import io.github.giovannilamarmora.utils.generic.Response;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.github.giovannilamarmora.utils.utilities.ObjectToolkit;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Logged
public class ImageService {

  public static Map<String, Image> attachmentMap = new HashMap<>();
  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  @Value(value = "${spring.webflux.multipart.max-request-size}")
  private String maxSize;

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public Mono<ResponseEntity<Response>> saveAttachment(FilePart file) {
    if (ObjectToolkit.isNullOrEmpty(file)) {
      LOG.error("The file you have been passed is invalid");
      throw new ImageException(
          "The file you have been passed is invalid", ExceptionMap.ERR_IMG_MSS_001.getMessage());
    }
    Mono<Image> attachment = ImageMapper.fromPartToDto(file, Utils.convertToSize(maxSize));

    return attachment.flatMap(
        attachmentDTO -> {
          attachmentMap.put(attachmentDTO.getFileName(), attachmentDTO);
          String message = "Attachment " + attachmentDTO.getName() + " Successfully uploaded!";

          Response response =
              new Response(HttpStatus.OK.value(), message, TraceUtils.getSpanID(), attachmentDTO);
          return Mono.just(ResponseEntity.ok(response));
        });
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public ResponseEntity<?> getAttachment(String imageName, Boolean getImage) {
    Image imageJSON = getAttachment(imageName);

    if (ObjectToolkit.isNullOrEmpty(imageJSON)) {
      LOG.error("The image with name {} could not be found", imageName);
      throw new ImageException(ExceptionMap.ERR_IMG_MSS_001);
    }

    if (Boolean.TRUE.equals(getImage)) {
      byte[] imageBytes = imageJSON.getBody();

      ByteArrayResource resource = new ByteArrayResource(imageBytes);

      return ResponseEntity.ok()
          .contentType(MediaType.parseMediaType(imageJSON.getContentType()))
          .contentLength(imageJSON.getSize())
          .body(resource);
    } else {
      String message = "Attachment " + imageName + " Successfully found!";
      Response response =
          new Response(HttpStatus.OK.value(), message, TraceUtils.getSpanID(), imageJSON);
      return ResponseEntity.ok(response);
    }
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public ResponseEntity<Response> deleteAttachment(String imageName) {
    removeAttachment(imageName);
    String message = "Attachment " + imageName + " Successfully removed!";
    Response response = new Response(HttpStatus.OK.value(), message, TraceUtils.getSpanID(), null);
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
