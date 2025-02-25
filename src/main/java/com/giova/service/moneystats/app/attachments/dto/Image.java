package com.giova.service.moneystats.app.attachments.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.giovannilamarmora.utils.generic.GenericDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Image extends GenericDTO {

  @NotBlank(message = "Name must not be valid")
  private String name;

  @NotBlank(message = "fileName must not be valid")
  private String fileName;

  @NotBlank(message = "contentType must not be valid")
  private String contentType;

  private long size;

  @NotBlank(message = "body must not be valid")
  private byte[] body;
}
