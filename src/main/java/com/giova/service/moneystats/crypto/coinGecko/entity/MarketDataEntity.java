package com.giova.service.moneystats.crypto.coinGecko.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.giova.service.moneystats.generic.GenericEntity;
import javax.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@Table(name = "MARKET_DATA")
public class MarketDataEntity extends GenericEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", nullable = false)
  private Long id;

  @Column(name = "CURRENCY", nullable = false)
  private String currency;

  @Column(name = "IDENTIFIER", nullable = false)
  private String identifier;

  @Column(name = "SYMBOL", nullable = false)
  private String symbol;

  @Column(name = "NAME", nullable = false)
  private String name;

  @Column(name = "ICON", nullable = false)
  private String icon;

  @Column(name = "CURRENT_PRICE", nullable = false)
  private Double current_price;

  @Column(name = "CATEGORY")
  private String category;

  @Column(name = "MARKET_CAP")
  private Double market_cap;

  @Column(name = "MARKET_CAP_RANK", nullable = false)
  private Long rank;

  @Column(name = "TOTAL_VOLUME")
  private Double total_volume;

  @Column(name = "HIGH_24H")
  private Double high_24h;

  @Column(name = "LOW_24H")
  private Double low_24h;

  @Column(name = "PRICE_CHANGE_24H")
  private Double price_change_24h;

  @Column(name = "PRICE_CHANGE_PERCENTAGE_24H")
  private Double price_change_percentage_24h;

  @Column(name = "MARKET_CAP_CHANGE_24H")
  private Double market_cap_change_24h;

  @Column(name = "MARKET_CAP_CHANGE_PERCENTAGE_24H")
  private Double market_cap_change_percentage_24h;
}
