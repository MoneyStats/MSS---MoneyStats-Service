package com.giova.service.moneystats.authentication.token;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giova.service.moneystats.authentication.AuthException;
import com.giova.service.moneystats.authentication.dto.User;
import com.giova.service.moneystats.authentication.dto.UserRole;
import com.giova.service.moneystats.authentication.token.dto.AuthToken;
import com.giova.service.moneystats.exception.ExceptionMap;
import com.giova.service.moneystats.settings.dto.UserSettingDTO;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWEDecryptionKeySelector;
import com.nimbusds.jose.proc.JWEKeySelector;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.validation.constraints.NotNull;
import java.text.ParseException;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Logged
@Service
public class TokenService {

  private static final String FIRSTNAME = "firstName";
  private static final String LASTNAME = "lastName";
  private static final String PROFILE_PHOTO = "profilePhoto";
  private static final String SETTINGS = "settings";
  private static final String EMAIL = "email";
  private static final String ROLE = "role";
  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

  @Value(value = "${jwt.secret}")
  private String secret;

  @Value(value = "${jwt.time}")
  private String expirationTime;

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public AuthToken generateJWTToken(User user) {
    Claims claims = Jwts.claims().setSubject(user.getUsername());
    claims.put(FIRSTNAME, user.getName());
    claims.put(LASTNAME, user.getSurname());
    claims.put(EMAIL, user.getEmail());
    claims.put(ROLE, user.getRole());
    // claims.put(PROFILE_PHOTO, user.getProfilePhoto());
    claims.put(SETTINGS, user.getSettings());
    long dateExp = Long.parseLong(expirationTime);
    Date exp = new Date(System.currentTimeMillis() + dateExp);
    String token =
        Jwts.builder()
            .setClaims(claims)
            .signWith(SignatureAlgorithm.HS512, secret)
            .setExpiration(exp)
            .compact();
    return new AuthToken(dateExp, "Bearer", token);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public User parseJWTToken(AuthToken token) throws UtilsException {
    Claims body;
    try {
      body = Jwts.parser().setSigningKey(secret).parseClaimsJws(token.getAccessToken()).getBody();
    } catch (JwtException e) {
      LOG.error("Not Authorized, parseToken - Exception -> {}", e.getMessage());
      throw new AuthException(ExceptionMap.ERR_AUTH_MSS_008, e.getMessage());
    }

    return new User(
        (@NotNull String) body.get(FIRSTNAME),
        (@NotNull String) body.get(LASTNAME),
        (@NotNull String) body.get(EMAIL),
        body.getSubject(),
        UserRole.valueOf((@NotNull String) body.get(ROLE)),
        // (@NotNull String) body.get(PROFILE_PHOTO),
        (@NotNull UserSettingDTO) body.get(SETTINGS));
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public AuthToken generateToken(User user) throws JOSEException {
    // Crea un set di claims
    JWTClaimsSet claimsSet = null;
    try {
      claimsSet =
          new JWTClaimsSet.Builder()
              .subject(user.getUsername())
              .claim(FIRSTNAME, user.getName())
              .claim(LASTNAME, user.getSurname())
              .claim(EMAIL, user.getEmail())
              .claim(ROLE, user.getRole())
              .claim(SETTINGS, mapper.writeValueAsString(user.getSettings()))
              .expirationTime(new Date(System.currentTimeMillis() + Long.parseLong(expirationTime)))
              .build();
    } catch (JsonProcessingException e) {
      LOG.error("Not Authorized, generateToken - Exception -> {}", e.getMessage());
      throw new AuthException(ExceptionMap.ERR_AUTH_MSS_008, e.getMessage());
    }

    Payload payload = new Payload(claimsSet.toJSONObject());

    JWEHeader header = new JWEHeader(JWEAlgorithm.DIR, EncryptionMethod.A256CBC_HS512);

    byte[] secretKey = secret.getBytes();
    DirectEncrypter encrypted = new DirectEncrypter(secretKey);

    JWEObject jweObject = new JWEObject(header, payload);
    jweObject.encrypt(encrypted);
    String token = jweObject.serialize();

    return new AuthToken(claimsSet.getExpirationTime().getTime(), "Bearer", token);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public User parseToken(AuthToken token) throws UtilsException {
    try {
      String tokenSplit = token.getAccessToken().split("Bearer ")[1];
      ConfigurableJWTProcessor<SimpleSecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
      JWKSource<SimpleSecurityContext> jweKeySource = new ImmutableSecret<>(secret.getBytes());
      JWEKeySelector<SimpleSecurityContext> jweKeySelector =
          new JWEDecryptionKeySelector<>(
              JWEAlgorithm.DIR, EncryptionMethod.A256CBC_HS512, jweKeySource);
      jwtProcessor.setJWEKeySelector(jweKeySelector);

      JWTClaimsSet claimsSet = jwtProcessor.process(tokenSplit, null);

      UserSettingDTO settingDTO =
          mapper.readValue((String) claimsSet.getClaim(SETTINGS), UserSettingDTO.class);

      return new User(
          (String) claimsSet.getClaim(FIRSTNAME),
          (String) claimsSet.getClaim(LASTNAME),
          (String) claimsSet.getClaim(EMAIL),
          claimsSet.getSubject(),
          UserRole.valueOf((String) claimsSet.getClaim(ROLE)),
          settingDTO);
    } catch (JOSEException | ParseException | BadJOSEException | JsonProcessingException e) {
      LOG.error("Not Authorized, parseToken - Exception -> {}", e.getMessage());
      throw new AuthException(ExceptionMap.ERR_AUTH_MSS_008, e.getMessage());
    }
  }
}
