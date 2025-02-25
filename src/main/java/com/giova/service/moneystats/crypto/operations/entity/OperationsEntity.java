package com.giova.service.moneystats.crypto.operations.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.giova.service.moneystats.crypto.asset.entity.AssetEntity;
import io.github.giovannilamarmora.utils.generic.GenericEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@Table(name = "OPERATIONS")
public class OperationsEntity extends GenericEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", nullable = false)
  private Long id;

  @Column(name = "IDENTIFIER")
  private String identifier;

  @Column(name = "TYPE", nullable = false)
  private String type;

  @Column(name = "STATUS", nullable = false)
  private String status;

  @Column(name = "ENTRY_DATE")
  private LocalDateTime entryDate;

  @Column(name = "ENTRY_COIN", nullable = false)
  private String entryCoin;

  @Column(name = "ENTRY_PRICE")
  private String entryPrice;

  @Column(name = "ENTRY_PRICE_VALUE")
  private Double entryPriceValue;

  @Column(name = "ENTRY_QUANTITY")
  private Double entryQuantity;

  @Column(name = "EXIT_DATE")
  private LocalDateTime exitDate;

  @Column(name = "EXIT_COIN", nullable = false)
  private String exitCoin;

  @Column(name = "EXIT_PRICE")
  private String exitPrice;

  @Column(name = "EXIT_PRICE_VALUE")
  private Double exitPriceValue;

  @Column(name = "EXIT_QUANTITY")
  private Double exitQuantity;

  @Column(name = "PERFORMANCE")
  private Double performance;

  @Column(name = "TREND")
  private Double trend;

  @Column(name = "FEES")
  private Double fees;

  @Column(name = "USER_IDENTIFIER", nullable = false)
  private String userIdentifier;

  @ManyToOne
  @JoinColumn(name = "ASSET_ID", nullable = false)
  @JsonIgnore // 🔹 Evita la serializzazione ciclica
  private AssetEntity asset;
}
