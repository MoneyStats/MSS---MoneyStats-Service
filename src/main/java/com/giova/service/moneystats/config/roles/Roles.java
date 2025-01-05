package com.giova.service.moneystats.config.roles;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Roles {
  AppRole[] value();
}
