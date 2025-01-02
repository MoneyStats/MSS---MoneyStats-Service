package com.giova.service.moneystats.app.stats.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.giova.service.moneystats.app.wallet.entity.WalletEntity;
import com.giova.service.moneystats.crypto.asset.entity.AssetEntity;
import io.github.giovannilamarmora.utils.generic.GenericEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
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
@Table(name = "STATS")
public class StatsEntity extends GenericEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", nullable = false)
  private Long id;

  @Column(name = "DATE", nullable = false)
  private LocalDate date;

  @Column(name = "BALANCE", nullable = false)
  private Double balance;

  @Column(name = "PERCENTAGE", nullable = false)
  private Double percentage;

  @Column(name = "TREND", nullable = false)
  private Double trend;

  @Column(name = "USER_IDENTIFIER", nullable = false)
  private String userIdentifier;

  @ManyToOne
  @JoinColumn(name = "ASSET_ID")
  private AssetEntity asset;

  @ManyToOne
  @JoinColumn(name = "WALLET_ID")
  private WalletEntity wallet;
}
