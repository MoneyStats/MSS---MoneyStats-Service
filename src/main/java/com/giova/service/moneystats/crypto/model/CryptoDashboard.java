package com.giova.service.moneystats.crypto.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.giova.service.moneystats.app.wallet.dto.Wallet;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CryptoDashboard {
  private Double balance;
  private String currency;
  private Double btcBalance;
  private LocalDate lastUpdate;
  private LocalDate performanceSince;

  // TODO: To check
  private String value;
  private Double performance;
  private Double performanceValue;

  private LocalDate performanceLastDate;
  private Double lastStatsPerformance;
  private Double lastStatsBalanceDifference;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<LocalDate> statsAssetsDays;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Wallet> wallets;
}
