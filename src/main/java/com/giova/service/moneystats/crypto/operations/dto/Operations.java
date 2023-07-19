package com.giova.service.moneystats.crypto.operations.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.giova.service.moneystats.generic.GenericDTO;
import java.time.LocalDateTime;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Operations extends GenericDTO {

  private Long id;
  private String type;
  private String status;
  private LocalDateTime entryDate;
  private String entryCoin;
  private String entryPrice;
  private Double entryPriceValue;
  private Double entryQuantity;
  private LocalDateTime exitDate;
  private String exitCoin;
  private String exitPrice;
  private Double exitPriceValue;
  private Double exitQuantity;
  private Double performance;
  private Double trend;
}
