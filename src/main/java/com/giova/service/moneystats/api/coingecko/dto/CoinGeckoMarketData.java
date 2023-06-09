package com.giova.service.moneystats.api.coingecko.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CoinGeckoMarketData {
    private String id;
    private String symbol;
    private String name;
    private String image;
    private Double current_price;
    private Double market_cap;
    private Long market_cap_rank;
    private Double fully_diluted_valuation;
    private Double total_volume;
    private Double high_24h;
    private Double low_24h;
    private Double price_change_24h;
    private Double price_change_percentage_24h;
    private Double market_cap_change_24h;
    private Double market_cap_change_percentage_24h;
    private Double circulating_supply;
    private Double total_supply;
    private Double max_supply;
    private Double ath;
    private Double ath_change_percentage;
    private LocalDateTime ath_date;
    private Double atl;
    private Double atl_change_percentage;
    private LocalDateTime atl_date;
    private Roi roi;
    private LocalDateTime last_updated;
}
