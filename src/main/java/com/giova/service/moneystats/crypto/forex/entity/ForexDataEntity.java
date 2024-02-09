package com.giova.service.moneystats.crypto.forex.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.giovannilamarmora.utils.generic.GenericEntity;
import java.time.LocalDateTime;
import javax.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@Table(name = "FOREX_DATA")
public class ForexDataEntity extends GenericEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", nullable = false)
  private Long id;

  @Column(name = "LAST_UPDATE", nullable = false)
  private LocalDateTime lastUpdate;

  @Column(name = "CURRENCY", nullable = false)
  private String currency;

  @Lob
  @Column(name = "QUOTES", nullable = false)
  private String quotes;
}
