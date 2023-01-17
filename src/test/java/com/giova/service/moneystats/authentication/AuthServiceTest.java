package com.giova.service.moneystats.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.giova.service.moneystats.authentication.dto.User;
import com.giova.service.moneystats.generic.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class AuthServiceTest {

    @Autowired
    private AuthService authService;

    private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    public void sendMailTest_successfully() throws IOException {
        Response expected = objectMapper.readValue(
                new ClassPathResource("mock/response/user.json").getInputStream(), Response.class);
        User user = objectMapper.readValue(
                new ClassPathResource("mock/request/user.json").getInputStream(), User.class);

        String data = objectMapper.writeValueAsString(expected.getData());

        ResponseEntity<Response> actual = authService.register(user);
        assertEquals(expected.getStatus(), actual.getBody().getStatus());
        assertEquals(expected.getMessage(), actual.getBody().getMessage());
    }
}
