package com.giova.service.moneystats.api.accessSphere.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.giova.service.moneystats.api.accessSphere.dto.shared.JWTData;
import com.giova.service.moneystats.api.accessSphere.dto.shared.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfoResponse {
  private JWTData userInfo;
  private User user;
}
