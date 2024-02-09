package com.giova.service.moneystats.app.attachments;

import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.generic.Response;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Logged
@RestController
@RequestMapping("/v1")
@CrossOrigin(origins = "*")
public class ImageController {

  @Autowired private ImageService imageService;

  @PostMapping(
      value = "/upload/attachment",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Tag(
      name = "Upload Attachment",
      description = "API to upload the attachment before to send email")
  @Operation(
      description = "API to upload the attachment before to send email",
      tags = "Upload Attachment")
  @LogInterceptor(type = LogTimeTracker.ActionType.CONTROLLER)
  public ResponseEntity<Response> uploadAttachment(@RequestPart(name = "file") MultipartFile file)
      throws UtilsException {
    return imageService.saveAttachmentDto(file);
  }
}
