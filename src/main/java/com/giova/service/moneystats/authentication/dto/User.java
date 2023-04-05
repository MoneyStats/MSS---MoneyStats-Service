package com.giova.service.moneystats.authentication.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.giova.service.moneystats.authentication.token.dto.AuthToken;
import com.giova.service.moneystats.generic.GenericDTO;
import io.github.giovannilamarmora.utils.jsonSerialize.LowerCase;
import io.github.giovannilamarmora.utils.jsonSerialize.UpperCamelCase;
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
public class User extends GenericDTO {
  @NotNull @UpperCamelCase private String name;
  @NotNull @UpperCamelCase private String surname;
  @NotNull @LowerCase private String email;
  @NotNull @LowerCase private String username;
  @NotNull private String password;
  private UserRole role;
  @NotNull private String profilePhoto;
  private String imgName;
  @NotNull private String currency;
  private String githubUser;

  private AuthToken authToken;
  private String tokenReset;

  public User(
      String name, String surname, String email, String username, UserRole role, String currency) {
    this.name = name;
    this.surname = surname;
    this.email = email;
    this.username = username;
    this.role = role;
    this.currency = currency;
  }
}
