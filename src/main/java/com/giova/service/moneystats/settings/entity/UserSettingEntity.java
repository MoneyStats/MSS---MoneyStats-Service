package com.giova.service.moneystats.settings.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import io.github.giovannilamarmora.utils.generic.GenericEntity;
import jakarta.persistence.*;
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
@Table(name = "USER_SETTINGS")
public class UserSettingEntity extends GenericEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", nullable = false)
  private Long id;

  // @Enumerated(EnumType.STRING)
  @Column(name = "CURRENCY")
  private String currency;

  @Column(name = "CRYPTO_CURRENCY")
  private String cryptoCurrency;

  @Lob
  @Column(name = "GITHUB_USER")
  private String githubUser;

  @Column(name = "COMPLETE_REQUIREMENT")
  private String completeRequirement;

  @Column(name = "LIVE_WALLETS")
  private String liveWallets;

  @OneToOne
  @JsonBackReference
  @JoinColumn(name = "USER_ID", nullable = false)
  private UserEntity user;
}
