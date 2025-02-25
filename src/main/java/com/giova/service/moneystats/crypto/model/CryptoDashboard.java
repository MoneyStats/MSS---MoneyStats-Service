package com.giova.service.moneystats.crypto.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.giova.service.moneystats.app.wallet.dto.Wallet;
import com.giova.service.moneystats.crypto.asset.dto.Asset;
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
  private DashboardInfo holdingLong;
  private DashboardInfo trading;
  private DashboardInfo performance;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Integer> yearsWalletStats;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<LocalDate> statsAssetsDays;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Asset> assets;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Wallet> wallets;
}
