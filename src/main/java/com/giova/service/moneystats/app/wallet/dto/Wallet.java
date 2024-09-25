package com.giova.service.moneystats.app.wallet.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.giova.service.moneystats.app.stats.dto.Stats;
import com.giova.service.moneystats.crypto.asset.dto.Asset;
import io.github.giovannilamarmora.utils.generic.GenericDTO;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Wallet extends GenericDTO {

  @NotNull private String name;
  private String type;
  private Double balance;
  private String img;
  private String imgName;
  private Double allTimeHigh;
  private LocalDate allTimeHighDate;
  private Double highPrice;
  private LocalDate highPriceDate;
  private Double lowPrice;
  private LocalDate lowPriceDate;
  private Double performanceLastStats;
  private Double differenceLastStats;
  private LocalDate dateLastStats;
  private Map<String, String> info;
  private String infoString;
  @NotNull private String category;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Asset> assets;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Stats> history;
}
