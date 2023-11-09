package com.giova.service.moneystats.crypto.forex.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.giova.service.moneystats.generic.GenericDTO;
import io.github.giovannilamarmora.utils.jsonSerialize.UpperCase;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ForexData extends GenericDTO {
  private LocalDateTime lastUpdate;
  @UpperCase private String currency;
  private Map<String, Double> quotes;
}
