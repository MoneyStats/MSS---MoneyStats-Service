package com.giova.service.moneystats.crypto.asset.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.giova.service.moneystats.app.stats.dto.Stats;
import com.giova.service.moneystats.crypto.operations.dto.Operations;
import com.giova.service.moneystats.generic.GenericDTO;
import io.github.giovannilamarmora.utils.jsonSerialize.LowerCase;
import io.github.giovannilamarmora.utils.jsonSerialize.UpperCamelCase;
import io.github.giovannilamarmora.utils.jsonSerialize.UpperCase;
import java.time.LocalDate;
import java.util.List;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Asset extends GenericDTO {

  @LowerCase private String identifier;
  @UpperCamelCase private String name;
  @UpperCamelCase private String category;
  @UpperCase private String symbol;
  private Long rank;
  private Double current_price;
  private Double value; // Is the current balance in USD
  private String icon;
  private Double balance;
  private Double invested;
  private LocalDate lastUpdate;
  private Double performance;
  private Double trend;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Operations> operations;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Stats> history;
}
