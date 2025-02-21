package com.giova.service.moneystats.app.model;

import io.github.giovannilamarmora.utils.utilities.EnumWithValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Category implements EnumWithValue {
  CRYPTO("Crypto");

  private final String value;

  /**
   * @return value
   */
  @Override
  public String getValue() {
    return value;
  }
}
