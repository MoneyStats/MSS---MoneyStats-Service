package com.giova.service.moneystats.crypto.asset.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.giova.service.moneystats.app.stats.entity.StatsEntity;
import com.giova.service.moneystats.app.wallet.entity.WalletEntity;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import com.giova.service.moneystats.crypto.operations.entity.OperationsEntity;
import io.github.giovannilamarmora.utils.generic.GenericEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;
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
@Table(name = "ASSETS")
public class AssetEntity extends GenericEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", nullable = false)
  private Long id;

  @Column(name = "IDENTIFIER")
  private String identifier;

  @Column(name = "NAME")
  private String name;

  @Column(name = "CATEGORY")
  private String category;

  @Column(name = "SYMBOL", length = 10)
  private String symbol;

  @Column(name = "CRYPTO_RANK")
  private Long rank;

  @Lob
  @Column(name = "ICON")
  private String icon;

  @Column(name = "BALANCE")
  private Double balance;

  @Column(name = "INVESTED")
  private Double invested;

  @Column(name = "LAST_UPDATE")
  private LocalDate lastUpdate;

  @Column(name = "PERFORMANCE")
  private Double performance;

  @Column(name = "TREND")
  private Double trend;

  @OrderBy(value = "exitDate DESC")
  @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<OperationsEntity> operations;

  @ManyToOne
  @JoinColumn(name = "USER_ID", nullable = false)
  private UserEntity user;

  @ManyToOne
  @JoinColumn(name = "WALLET_ID", nullable = false)
  private WalletEntity wallet;

  @OrderBy(value = "date")
  @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<StatsEntity> history;
}
