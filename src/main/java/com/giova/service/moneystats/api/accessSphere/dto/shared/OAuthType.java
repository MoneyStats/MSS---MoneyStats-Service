package com.giova.service.moneystats.api.accessSphere.dto.shared;

public enum OAuthType {
  ALL_TYPE("All-Type"),
  BEARER("Bearer"),
  GOOGLE("google");

  private final String type;

  OAuthType(String type) {
    this.type = type;
  }

  public String type() {
    return type;
  }
}
