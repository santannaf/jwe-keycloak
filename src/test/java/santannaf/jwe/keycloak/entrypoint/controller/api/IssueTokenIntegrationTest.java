package santannaf.jwe.keycloak.entrypoint.controller.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import santannaf.jwe.keycloak.entities.KeycloakToken;
import santannaf.jwe.keycloak.exceptions.KeycloakAuthenticationException;
import santannaf.jwe.keycloak.exceptions.KeycloakUnavailableException;
import santannaf.jwe.keycloak.providers.IssueClientCredentialsTokenProvider;

@SpringBootTest
class IssueTokenIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FakeIssueProvider fakeIssueProvider;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        fakeIssueProvider.reset();
    }

    @Test
    void returnsEncryptedTokenOnSuccess() throws Exception {
        mockMvc.perform(post("/oauth/m2m/token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andExpect(jsonPath("$.token_type").value("Bearer"));
    }

    @Test
    void returnsUnauthorizedWhenKeycloakRejectsClient() throws Exception {
        fakeIssueProvider.behavior = () -> {
            throw new KeycloakAuthenticationException("bad client");
        };

        mockMvc.perform(post("/oauth/m2m/token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void returnsBadGatewayWhenKeycloakUnavailable() throws Exception {
        fakeIssueProvider.behavior = () -> {
            throw new KeycloakUnavailableException("down");
        };

        mockMvc.perform(post("/oauth/m2m/token"))
                .andExpect(status().isBadGateway());
    }

    @TestConfiguration
    static class FakeConfig {

        @Bean
        @Primary
        FakeIssueProvider fakeIssueProvider() {
            return new FakeIssueProvider();
        }
    }

    static class FakeIssueProvider implements IssueClientCredentialsTokenProvider {

        Supplier<KeycloakToken> behavior;

        void reset() {
            behavior = () -> new KeycloakToken("access-token-xyz", "Bearer", 300L, null);
        }

        @Override
        public KeycloakToken issue() {
            return behavior.get();
        }
    }
}
