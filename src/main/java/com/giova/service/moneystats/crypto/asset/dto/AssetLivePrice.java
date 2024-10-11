package com.giova.service.moneystats.crypto.asset.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.giovannilamarmora.utils.jsonSerialize.LowerCase;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssetLivePrice {

  @LowerCase private String identifier;
  private Double balance;
  private Long walletId;
}
