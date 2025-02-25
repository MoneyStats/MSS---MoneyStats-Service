package com.giova.service.moneystats.crypto.marketData.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.giovannilamarmora.utils.generic.GenericDTO;
import io.github.giovannilamarmora.utils.jsonSerialize.UpperCamelCase;
import io.github.giovannilamarmora.utils.jsonSerialize.UpperCase;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MarketData extends GenericDTO {
  private String identifier;
  @UpperCase private String symbol;
  @UpperCamelCase private String name;
  private String icon;
  @UpperCase private String currency;
  private Double current_price;
  private String category;
  private Double market_cap;
  private Long rank;
  private Double total_volume;
  private Double high_24h;
  private Double low_24h;
  private Double price_change_24h;
  private Double price_change_percentage_24h;
  private Double market_cap_change_24h;
  private Double market_cap_change_percentage_24h;
}
