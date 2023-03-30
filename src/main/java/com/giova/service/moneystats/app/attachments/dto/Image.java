package com.giova.service.moneystats.app.attachments.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.giova.service.moneystats.generic.GenericDTO;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Image extends GenericDTO {

  @NotNull(message = "Name must not be null")
  @NotEmpty(message = "Name must not be empty")
  private String name;

  @NotNull(message = "fileName must not be null")
  @NotEmpty(message = "fileName must not be empty")
  private String fileName;

  @NotNull(message = "contentType must not be null")
  @NotEmpty(message = "contentType must not be empty")
  private String contentType;

  private long size;

  @NotNull(message = "body must not be null")
  @NotEmpty(message = "body must not be empty")
  private byte[] body;
}
