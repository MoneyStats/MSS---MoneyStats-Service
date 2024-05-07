package com.giova.service.moneystats.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.giova.service.moneystats.authentication.dto.User;
import io.github.giovannilamarmora.utils.generic.Response;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@ActiveProfiles("test")
@WebMvcTest(controllers = AuthControllerImpl.class)
@ContextConfiguration(classes = AuthControllerImpl.class)
public class AuthControllerImplTest {

  @MockBean private AuthService authService;

  @Autowired private MockMvc mockMvc;

  private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @Test
  public void testRegister_successfully() throws Exception {
    Response expected =
        objectMapper.readValue(
            new ClassPathResource("mock/response/user.json").getInputStream(), Response.class);
    User user =
        objectMapper.readValue(
            new ClassPathResource("mock/request/user.json").getInputStream(), User.class);

    String userAsString = objectMapper.writeValueAsString(user);
    String token = "token";

    Mockito.when(authService.register(user, token)).thenReturn(ResponseEntity.ok(expected));
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/v1/auth/sign-up")
                .param("invitationCode", "token")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(userAsString))
        .andExpect(MockMvcResultMatchers.status().isOk());
  }

  @Test
  public void testLogin_successfully() throws Exception {
    Response expected =
        objectMapper.readValue(
            new ClassPathResource("mock/response/login.json").getInputStream(), Response.class);
    User user =
        objectMapper.readValue(
            new ClassPathResource("mock/request/user.json").getInputStream(), User.class);

    String encode = user.getUsername() + ":" + "string";
    String basic = Base64.getEncoder().encodeToString(encode.getBytes());

    Mockito.when(authService.login(basic)).thenReturn(ResponseEntity.ok(expected));

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(
                    "/v1/auth/login?username=" + user.getUsername() + "&password=string")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Basic token"))
        .andExpect(MockMvcResultMatchers.status().isOk());
  }
}
