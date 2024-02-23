package com.giova.service.moneystats.settings.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.giovannilamarmora.utils.generic.GenericDTO;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserSettingDTO extends GenericDTO {
  @NotNull private String currency;
  private String cryptoCurrency;
  private String githubUser;
  private String completeRequirement;
  private String liveWallets;
}
