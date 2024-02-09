package com.giova.service.moneystats.api.forex.exchageRates.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExchangeRates {
  private String success;
  private String terms;
  private String privacy;
  private String timestamp;
  private String source;
  private Map<String, Double> quotes;
  private RatesError error;

  @Builder
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class RatesError {
    private Integer code;
    private String info;
  }
}
