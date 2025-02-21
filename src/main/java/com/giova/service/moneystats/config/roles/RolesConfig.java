package com.giova.service.moneystats.config.roles;

import com.giova.service.moneystats.authentication.AuthException;
import com.giova.service.moneystats.authentication.AuthMapper;
import com.giova.service.moneystats.authentication.dto.UserData;
import com.giova.service.moneystats.exception.config.ExceptionCode;
import com.giova.service.moneystats.exception.config.ExceptionMap;
import io.github.giovannilamarmora.utils.logger.LoggerFilter;
import io.github.giovannilamarmora.utils.utilities.ObjectToolkit;
import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class RolesConfig implements Serializable {

  @Serial private static final long serialVersionUID = 5001545131635232118L;
  private final UserData user;
  private final Logger LOG = LoggerFilter.getLogger(this.getClass());

  @Pointcut("@annotation(com.giova.service.moneystats.config.roles.Roles)")
  public void annotationPointcut() {}

  @Around("annotationPointcut()")
  public Object processMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
    MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
    Method method = signature.getMethod();
    Roles rolesAnnotation = method.getAnnotation(Roles.class);

    if (AuthMapper.isUserInfoDeleteCache(proceedingJoinPoint, method))
      return proceedingJoinPoint.proceed();

    if (rolesAnnotation != null) {
      AppRole[] requiredRoles = rolesAnnotation.value();
      Set<String> requiredRolesSet =
          Arrays.stream(requiredRoles).map(AppRole::name).collect(Collectors.toSet());
      LOG.info(
          "Checking Roles for user {}. Required roles: {}", user.getUsername(), requiredRolesSet);

      if (!ObjectToolkit.isNullOrEmpty(user)) {
        Set<String> userRoles = new HashSet<>(user.getRoles());

        if (userRoles.containsAll(requiredRolesSet)) {
          return proceedingJoinPoint.proceed();
        } else {
          LOG.error(
              "Access denied for user {}. Required roles: {}, User roles: {}",
              user.getUsername(),
              requiredRolesSet,
              userRoles);
          throw new AuthException(
              ExceptionMap.ERR_AUTH_MSS_403, "Access denied: insufficient permissions");
        }
      } else {
        LOG.warn("Unauthenticated access attempt to method: {}", method.getName());
        throw new AuthException(
            ExceptionMap.ERR_AUTH_MSS_401,
            ExceptionCode.UNAUTHENTICATED_USER,
            "Access denied: unauthenticated user");
      }
    } else {
      return proceedingJoinPoint.proceed();
    }
  }
}
