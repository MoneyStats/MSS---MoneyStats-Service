package com.giova.service.moneystats.crypto.asset.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.giova.service.moneystats.app.stats.dto.Stats;
import com.giova.service.moneystats.generic.GenericDTO;
import io.github.giovannilamarmora.utils.jsonSerialize.UpperCamelCase;
import io.github.giovannilamarmora.utils.jsonSerialize.UpperCase;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Asset extends GenericDTO {

  private String identifier;
  @UpperCamelCase private String name;
  @UpperCase private String symbol;
  private Long rank;
  private Integer current_price;
  private Double value; // Is the current balance in USD
  private String icon;
  private Double balance;
  private Double invested;
  private LocalDate lastUpdate;
  private Double performance;
  private Double trend;
  private List<Stats> history;
}
