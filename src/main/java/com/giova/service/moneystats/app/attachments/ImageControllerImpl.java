package com.giova.service.moneystats.app.attachments;

import io.github.giovannilamarmora.utils.generic.Response;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Logged
@RestController
@RequestMapping("/v1")
@CrossOrigin(origins = "*")
@Tag(name = "Image", description = "API to Handle Images")
public class ImageControllerImpl implements ImageController {

  @Autowired private ImageService imageService;

  /**
   * Upload a file image
   *
   * @param file to b uploaded
   * @return image JSON
   */
  @Override
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public Mono<ResponseEntity<Response>> uploadAttachment(FilePart file) {
    return imageService.saveAttachment(file);
  }

  /**
   * Delete the image
   *
   * @param imageName Name of the image to be deleted
   * @return Response confirmation
   */
  @Override
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public ResponseEntity<Response> removeAttachment(String imageName) {
    return imageService.deleteAttachment(imageName);
  }

  /**
   * Get the image as a JSON or as an Image file
   *
   * @param imageName Name of the image
   * @param image Get the image instead of JSON
   * @return Image
   */
  @Override
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public ResponseEntity<?> getAttachment(String imageName, Boolean image) {
    return imageService.getAttachment(imageName, image);
  }
}
