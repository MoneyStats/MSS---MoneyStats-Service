package com.giova.service.moneystats.authentication.token.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthToken {

  public static final String JWE_TOKEN_COOKIE = "jwe_cookie_token";

  private Long expirationTime;
  private String type;
  private String accessToken;
}
