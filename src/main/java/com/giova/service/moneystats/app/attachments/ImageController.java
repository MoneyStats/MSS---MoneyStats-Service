package com.giova.service.moneystats.app.attachments;

import io.github.giovannilamarmora.utils.exception.dto.ExceptionResponse;
import io.github.giovannilamarmora.utils.generic.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

public interface ImageController {

  /**
   * Get the image as a JSON or as an Image file
   *
   * @param imageName Name of the image
   * @param image Get the image instead of JSON
   * @return Image
   */
  @GetMapping(value = "/attachment/{imageName}")
  @Operation(description = "API to read the attachment", summary = "Get Attachment", tags = "Image")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE /*,
               examples = @ExampleObject(value = "@upload.json")*/))
  @ApiResponse(
      responseCode = "401",
      description = "Invalid Token",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE /*,
              examples = @ExampleObject(value = "@invalid-token-exception.json")*/))
  @ApiResponse(
      responseCode = "404",
      description = "Image not found",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE /*,
              examples = @ExampleObject(value = "@invalid-token-exception.json")*/))
  ResponseEntity<?> getAttachment(
      @PathVariable(value = "imageName")
          @Valid
          @NotBlank(message = "Invalid Image Name")
          @Schema(description = "Name of the image to be searched", example = "image.jpg")
          String imageName,
      @RequestParam(value = "image", required = false, defaultValue = "false")
          @Schema(description = "Get the image instead of the JSON", example = "true")
          Boolean image);

  /**
   * Upload a file image
   *
   * @param file to b uploaded
   * @return image JSON
   */
  @PostMapping(
      value = "/attachment/upload",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      description = "API to upload the attachment before to send email",
      summary = "Upload Attachment",
      tags = "Image")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE /*,
              examples = @ExampleObject(value = "@upload.json")*/))
  @ApiResponse(
      responseCode = "400",
      description = "File To Larger",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE /*,
              examples = @ExampleObject(value = "@file-to-big.json")*/))
  @ApiResponse(
      responseCode = "401",
      description = "Invalid Token",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE /*,
              examples = @ExampleObject(value = "@invalid-token-exception.json")*/))
  @ApiResponse(
      responseCode = "404",
      description = "Image not found",
      content =
          @Content(
              schema = @Schema(implementation = ExceptionResponse.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE /*,
              examples = @ExampleObject(value = "@invalid-token-exception.json")*/))
  Mono<ResponseEntity<Response>> uploadAttachment(
      @RequestPart(name = "file")
          @Valid
          @NotNull(message = "Image cannot be null")
          @Schema(description = "Image File")
          FilePart file);

  /**
   * Delete the image
   *
   * @param imageName Name of the image to be deleted
   * @return Response confirmation
   */
  @DeleteMapping(value = "/attachment/{imageName}")
  @Operation(
      description = "API to remove the attachment",
      summary = "Remove Attachment",
      tags = "Image")
  @ApiResponse(
      responseCode = "200",
      description = "Successful operation",
      content =
          @Content(
              schema = @Schema(implementation = Response.class),
              mediaType = MediaType.APPLICATION_JSON_VALUE))
  @ApiResponse(
      responseCode = "401",
      description = "Invalid Token",
      content = @Content(schema = @Schema(implementation = ExceptionResponse.class) /*,
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              examples = @ExampleObject(value = "@invalid-token-exception.json")*/))
  ResponseEntity<Response> removeAttachment(
      @PathVariable(value = "imageName")
          @Valid
          @NotBlank(message = "Invalid Image Name")
          @Schema(description = "Name of the image to be removed", example = "image.jpg")
          String imageName);
}
