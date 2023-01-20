package com.giova.service.moneystats.authentication;

import com.giova.service.moneystats.authentication.dto.User;
import com.giova.service.moneystats.authentication.dto.UserRole;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import com.giova.service.moneystats.authentication.token.TokenService;
import com.giova.service.moneystats.authentication.token.dto.AuthToken;
import com.giova.service.moneystats.generic.Response;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.github.giovannilamarmora.utils.interceptors.correlationID.CorrelationIdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Logged
public class AuthService {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private IAuthDAO iAuthDAO;

    @Autowired
    private AuthMapper authMapper;

    @Autowired
    private TokenService tokenService;

    BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
    public ResponseEntity<Response> register(User user) {
        user.setRole(UserRole.USER);

        UserEntity userEntity = authMapper.mapUserToUserEntity(user);
        userEntity.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setPassword(null);

        UserEntity saved = iAuthDAO.save(userEntity);
        saved.setPassword(null);

        String message = "User: " + user.getUsername() + " Successfully registered!";

        Response response = new Response(HttpStatus.OK.value(), message, CorrelationIdUtils.getCorrelationId(), authMapper.mapUserEntityToUser(saved));

        return ResponseEntity.ok(response);
    }

    @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
    public ResponseEntity<Response> login(String username, String password) throws UtilsException {

        UserEntity userEntity = iAuthDAO.findUserEntityByUsername(username);
        if (userEntity == null) {
            LOG.error("User not found");
            throw new UtilsException(AuthException.ERR_AUTH_MSS_003, AuthException.ERR_AUTH_MSS_003.getMessage());
        }
        boolean matches =
                bCryptPasswordEncoder.matches(password, userEntity.getPassword());
        if (!matches) {
            LOG.error("User not found");
            throw new UtilsException(AuthException.ERR_AUTH_MSS_003, AuthException.ERR_AUTH_MSS_003.getMessage());
        }

        User user = authMapper.mapUserEntityToUser(userEntity);
        user.setPassword(null);
        user.setAuthToken(tokenService.generateToken(user));

        String message = "Login Successfully! Welcome back " + user.getUsername() + "!";

        Response response = new Response(HttpStatus.OK.value(), message, CorrelationIdUtils.getCorrelationId(), user);

        return ResponseEntity.ok(response);
    }

    @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
    public UserEntity checkLogin(String authToken) throws UtilsException {
        AuthToken token = new AuthToken();
        token.setAccessToken(authToken);
        User user = new User();
        try {
            user = tokenService.parseToken(token);
        } catch (UtilsException e) {
            throw new UtilsException(AuthException.ERR_AUTH_MSS_004, e.getMessage());
        }
        UserEntity userEntity = iAuthDAO.findUserEntityByUsername(user.getUsername());
        //userEntity.setPassword(null);

        if (userEntity == null) {
            LOG.error("User not found");
            throw new UtilsException(AuthException.ERR_AUTH_MSS_003, AuthException.ERR_AUTH_MSS_003.getMessage());
        }
        return userEntity;
    }
}
