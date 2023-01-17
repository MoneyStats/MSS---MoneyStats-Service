package com.giova.service.moneystats.authentication;

import com.giova.service.moneystats.authentication.dto.User;
import com.giova.service.moneystats.authentication.dto.UserRole;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import com.giova.service.moneystats.generic.Response;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.github.giovannilamarmora.utils.interceptors.correlationID.CorrelationIdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Logged
public class AuthService {

    @Autowired
    private IAuthDAO iAuthDAO;

    @Autowired
    private AuthMapper authMapper;

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
}
