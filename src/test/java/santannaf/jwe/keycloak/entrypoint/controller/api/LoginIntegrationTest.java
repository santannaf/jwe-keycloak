package santannaf.jwe.keycloak.entrypoint.controller.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.function.BiFunction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import santannaf.jwe.keycloak.entities.KeycloakToken;
import santannaf.jwe.keycloak.exceptions.KeycloakAuthenticationException;
import santannaf.jwe.keycloak.providers.AuthenticateUserProvider;

@SpringBootTest
class LoginIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FakeAuthProvider fakeAuthProvider;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        fakeAuthProvider.reset();
    }

    @Test
    void returnsTokensForValidCredentials() throws Exception {
        mockMvc.perform(post("/oauth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"demo\",\"password\":\"demo\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andExpect(jsonPath("$.token_type").value("Bearer"));
    }

    @Test
    void returnsUnauthorizedForInvalidCredentials() throws Exception {
        fakeAuthProvider.behavior = (username, password) -> {
            throw new KeycloakAuthenticationException("invalid");
        };

        mockMvc.perform(post("/oauth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"demo\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void returnsBadRequestWhenPasswordMissing() throws Exception {
        mockMvc.perform(post("/oauth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"demo\"}"))
                .andExpect(status().isBadRequest());
    }

    @TestConfiguration
    static class FakeConfig {

        @Bean
        @Primary
        FakeAuthProvider fakeAuthProvider() {
            return new FakeAuthProvider();
        }
    }

    static class FakeAuthProvider implements AuthenticateUserProvider {

        BiFunction<String, String, KeycloakToken> behavior;

        void reset() {
            behavior = (username, password) -> new KeycloakToken("access-token", "Bearer", 300L, "refresh-token");
        }

        @Override
        public KeycloakToken authenticate(String username, String password) {
            return behavior.apply(username, password);
        }
    }
}
