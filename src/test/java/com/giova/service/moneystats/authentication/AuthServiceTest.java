package com.giova.service.moneystats.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.giova.service.moneystats.authentication.dto.User;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.nimbusds.jose.JOSEException;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.generic.Response;
import java.io.IOException;
import java.util.Base64;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest()
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AuthServiceTest {

  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
  @Autowired private AuthService authService;
  private WireMockServer wireMockServer;

  @BeforeEach
  public void setUp() {
    wireMockServer = new WireMockServer(8086);
    wireMockServer.start();
    WireMock.configureFor(wireMockServer.port());
  }

  @AfterEach
  public void tearDown() {
    if (wireMockServer != null) {
      wireMockServer.stop();
    }
  }


  @Test
  public void registerTest_successfully() throws IOException, UtilsException {
    Response expected =
        objectMapper.readValue(
            new ClassPathResource("mock/response/user.json").getInputStream(), Response.class);
    User user =
        objectMapper.readValue(
            new ClassPathResource("mock/request/user.json").getInputStream(), User.class);

    User userEx = objectMapper.convertValue(expected.getData(), User.class);
    String token = "token";

    ResponseEntity<Response> actual = authService.register(user, token);

    User userAc = objectMapper.convertValue(actual.getBody().getData(), User.class);
    assertEquals(expected.getStatus(), actual.getBody().getStatus());
    assertEquals(expected.getMessage(), actual.getBody().getMessage());
    assertEquals(userEx.getName(), userAc.getName());
    assertEquals(userEx.getUsername(), userAc.getUsername());
  }

  // @Test
  public void loginTest_successfully() throws IOException, UtilsException, JOSEException {
    User register =
        objectMapper.readValue(
            new ClassPathResource("mock/request/user.json").getInputStream(), User.class);
    String token = "token";

    ResponseEntity<Response> actualR = authService.register(register, token);

    User registered = objectMapper.convertValue(actualR.getBody().getData(), User.class);

    Response expected =
        objectMapper.readValue(
            new ClassPathResource("mock/response/login.json").getInputStream(), Response.class);

    User user = objectMapper.convertValue(expected.getData(), User.class);

    String encode = registered.getUsername() + ":" + "Chicco.2024";
    String basic = Base64.getEncoder().encodeToString(encode.getBytes());

    ResponseEntity<Response> actual = authService.login(basic);

    User userAc = objectMapper.convertValue(actual.getBody().getData(), User.class);
    assertEquals(expected.getStatus(), actual.getBody().getStatus());
    assertEquals(expected.getMessage(), actual.getBody().getMessage());
    assertEquals(user.getName(), userAc.getName());
    assertEquals(user.getUsername(), userAc.getUsername());
  }

  // @Test
  public void checkLoginTest_successfully() throws IOException, UtilsException, JOSEException {
    User register =
        objectMapper.readValue(
            new ClassPathResource("mock/request/user.json").getInputStream(), User.class);
    String token = "token";

    ResponseEntity<Response> actualR = authService.register(register, token);

    User registered = objectMapper.convertValue(actualR.getBody().getData(), User.class);

    String encode = registered.getUsername() + ":" + "Chicco.2024";
    String basic = Base64.getEncoder().encodeToString(encode.getBytes());

    ResponseEntity<Response> actual = authService.login(basic);

    User userAc = objectMapper.convertValue(actual.getBody().getData(), User.class);

    UserEntity checkLogin =
        authService.checkLogin(
            userAc.getAuthToken().getType() + " " + userAc.getAuthToken().getAccessToken());
    assertEquals(userAc.getName(), checkLogin.getName());
    assertEquals(userAc.getUsername(), checkLogin.getUsername());
  }

  // @Test
  public void userInfoTest_successfully() throws IOException, UtilsException, JOSEException {
    User register =
        objectMapper.readValue(
            new ClassPathResource("mock/request/user.json").getInputStream(), User.class);
    String token = "token";

    ResponseEntity<Response> actualR = authService.register(register, token);

    User registered = objectMapper.convertValue(actualR.getBody().getData(), User.class);

    String encode = registered.getUsername() + ":" + "Chicco.2024";
    String basic = Base64.getEncoder().encodeToString(encode.getBytes());

    ResponseEntity<Response> actual = authService.login(basic);

    User userAc = objectMapper.convertValue(actual.getBody().getData(), User.class);

    ResponseEntity<Response> checkLogin =
        authService.userInfo(userAc.getAuthToken().getAccessToken());
    assertEquals(HttpStatus.OK, checkLogin.getStatusCode());
  }

  @Test
  public void testForgotPassword() throws Exception {
    User register =
        objectMapper.readValue(
            new ClassPathResource("mock/request/user.json").getInputStream(), User.class);
    String token = "token";

    ResponseEntity<Response> actualR = authService.register(register, token);
    User userAc = objectMapper.convertValue(actualR.getBody().getData(), User.class);

    wireMockServer.stubFor(
        WireMock.post(WireMock.urlEqualTo("/v1/send-email?htmlText=true"))
            .willReturn(
                WireMock.aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper.writeValueAsString(userAc))));

    // Mock di invocazione al servizio di forgotPassword
    Mono<ResponseEntity<Response>> responseMono = authService.forgotPassword(userAc.getEmail());

    // Verifica reattiva con StepVerifier
    StepVerifier.create(responseMono)
        .expectNextMatches(
            response -> {
              assertNotNull(response);
              assertEquals(HttpStatus.OK, response.getStatusCode());
              assertEquals(
                  "Email Sent! Check your email address!", response.getBody().getMessage());
              return true;
            })
        .expectComplete()
        .verify();
  }
}
