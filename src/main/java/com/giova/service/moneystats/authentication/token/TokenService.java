package com.giova.service.moneystats.authentication.token;

import com.giova.service.moneystats.authentication.AuthException;
import com.giova.service.moneystats.authentication.dto.User;
import com.giova.service.moneystats.authentication.dto.UserRole;
import com.giova.service.moneystats.authentication.token.dto.AuthToken;
import com.giova.service.moneystats.exception.ExceptionMap;
import com.nimbusds.jose.*;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import javax.validation.constraints.NotNull;
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
  private static final String CURRENCY = "currency";
  private static final String EMAIL = "email";
  private static final String ROLE = "role";
  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  @Value(value = "${jwt.secret}")
  private String secret;

  @Value(value = "${jwt.time}")
  private String expirationTime;

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public AuthToken generateToken(User user) {
    Claims claims = Jwts.claims().setSubject(user.getUsername());
    claims.put(FIRSTNAME, user.getName());
    claims.put(LASTNAME, user.getSurname());
    claims.put(EMAIL, user.getEmail());
    claims.put(ROLE, user.getRole());
    // claims.put(PROFILE_PHOTO, user.getProfilePhoto());
    claims.put(CURRENCY, user.getCurrency());
    long dateExp = Long.parseLong(expirationTime);
    Date exp = new Date(System.currentTimeMillis() + dateExp);
    String token =
        Jwts.builder()
            .setClaims(claims)
            .signWith(SignatureAlgorithm.HS512, secret)
            .setExpiration(exp)
            .compact();
    return new AuthToken(dateExp, token);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public User parseToken(AuthToken token) throws UtilsException {
    Claims body;
    try {
      body = Jwts.parser().setSigningKey(secret).parseClaimsJws(token.getAccessToken()).getBody();
    } catch (JwtException e) {
      LOG.error("Not Authorized, parseToken - Exception -> {}", e.getMessage());
      throw new AuthException(ExceptionMap.ERR_AUTH_MSS_002, e.getMessage());
    }

    return new User(
        (@NotNull String) body.get(FIRSTNAME),
        (@NotNull String) body.get(LASTNAME),
        (@NotNull String) body.get(EMAIL),
        body.getSubject(),
        UserRole.valueOf((@NotNull String) body.get(ROLE)),
        // (@NotNull String) body.get(PROFILE_PHOTO),
        (@NotNull String) body.get(CURRENCY));
  }
/*
  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public AuthToken generateToken(User user) throws JOSEException, ParseException {
    // Crea un set di claims
    JWTClaimsSet claimsSet =
        new JWTClaimsSet.Builder()
            .subject(user.getUsername())
            .claim(FIRSTNAME, user.getName())
            .claim(LASTNAME, user.getSurname())
            .claim(EMAIL, user.getEmail())
            .claim(ROLE, user.getRole())
            .claim(CURRENCY, user.getCurrency())
            .expirationTime(new Date(System.currentTimeMillis() + Long.parseLong(expirationTime)))
            .build();

    // Genera una chiave segreta per firmare il JWE
    OctetSequenceKey jwk = new OctetSequenceKey.Builder(secret.getBytes()).build();

    // Crea un oggetto JWE con il JWT firmato
    JWEObject jweObject =
        new JWEObject(
            new JWEHeader.Builder(JWEAlgorithm.PBES2_HS256_A128KW, EncryptionMethod.A128CBC_HS256)
                .contentType("JWT") // Il contenuto è un JWT
                .build(),
            new Payload(new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet)));

    // Serializza il JWE in una stringa
    String jweToken = jweObject.serialize();

    return new AuthToken(jweToken);
  }*/
}
