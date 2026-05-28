package santannaf.jwe.keycloak.entrypoint.controller.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.function.Consumer;

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

import santannaf.jwe.keycloak.exceptions.InvalidTokenException;
import santannaf.jwe.keycloak.providers.EncryptTokenProvider;
import santannaf.jwe.keycloak.providers.ValidateKeycloakTokenProvider;

@SpringBootTest
class ValidateTokenIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private EncryptTokenProvider encryptTokenProvider;

    @Autowired
    private FakeTokenValidator fakeTokenValidator;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        fakeTokenValidator.reset();
    }

    @Test
    void returnsOkForValidEncryptedToken() throws Exception {
        String jwe = encryptTokenProvider.encrypt("dummy-access-token");

        mockMvc.perform(post("/oauth/m2m/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + jwe + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    void returnsUnauthorizedWithNoBodyForMalformedJwe() throws Exception {
        mockMvc.perform(post("/oauth/m2m/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"not-a-access_token\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));
    }

    @Test
    void returnsUnauthorizedForInvalidInnerToken() throws Exception {
        String jwe = encryptTokenProvider.encrypt("dummy-access-token");
        fakeTokenValidator.behavior = accessToken -> {
            throw new InvalidTokenException("expired");
        };

        mockMvc.perform(post("/oauth/m2m/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + jwe + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));
    }

    @Test
    void returnsBadRequestWhenTokenMissing() throws Exception {
        mockMvc.perform(post("/oauth/m2m/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @TestConfiguration
    static class FakeConfig {

        @Bean
        @Primary
        FakeTokenValidator fakeTokenValidator() {
            return new FakeTokenValidator();
        }
    }

    static class FakeTokenValidator implements ValidateKeycloakTokenProvider {

        Consumer<String> behavior;

        void reset() {
            behavior = accessToken -> { };
        }

        @Override
        public void validate(String accessToken) {
            behavior.accept(accessToken);
        }
    }
}
