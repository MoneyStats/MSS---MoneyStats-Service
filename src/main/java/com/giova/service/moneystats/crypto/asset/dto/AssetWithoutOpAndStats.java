package com.giova.service.moneystats.crypto.asset.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.giovannilamarmora.utils.generic.GenericDTO;
import io.github.giovannilamarmora.utils.jsonSerialize.LowerCase;
import io.github.giovannilamarmora.utils.jsonSerialize.UpperCamelCase;
import io.github.giovannilamarmora.utils.jsonSerialize.UpperCase;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssetWithoutOpAndStats extends GenericDTO {

  @LowerCase private String identifier;
  @UpperCamelCase private String name;
  @UpperCamelCase private String category;
  @UpperCase private String symbol;
  private Long rank;
  private String icon;
  private Double balance;
  private Double invested;
  private LocalDate lastUpdate;
  private Double performance;
  private Double trend;
  private Long walletId;

  public AssetWithoutOpAndStats(
      Long id,
      LocalDateTime creationDate,
      LocalDateTime updateDate,
      LocalDateTime deletedDate,
      String identifier,
      String name,
      String category,
      String symbol,
      Long rank,
      String icon,
      Double balance,
      Double invested,
      LocalDate lastUpdate,
      Double performance,
      Double trend,
      Long walletId) {
    super(id, creationDate, updateDate, deletedDate);
    this.identifier = identifier;
    this.name = name;
    this.category = category;
    this.symbol = symbol;
    this.rank = rank;
    this.icon = icon;
    this.balance = balance;
    this.invested = invested;
    this.lastUpdate = lastUpdate;
    this.performance = performance;
    this.trend = trend;
    this.walletId = walletId;
  }
}
