package com.giova.service.moneystats.authentication.service;

import com.giova.service.moneystats.authentication.dto.UserData;
import reactor.core.publisher.Mono;

public interface AuthRepository {

  /**
   * Authorize request to Access Sphere
   *
   * @param access_token to be validated
   * @param sessionId of the request
   * @return User Info
   */
  Mono<UserData> authorize(String access_token, String sessionId);
}
