package santannaf.jwe.keycloak.dataproviders.client.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import santannaf.jwe.keycloak.config.KeycloakProperties;
import santannaf.jwe.keycloak.entities.KeycloakToken;
import santannaf.jwe.keycloak.exceptions.KeycloakAuthenticationException;
import santannaf.jwe.keycloak.exceptions.KeycloakUnavailableException;
import santannaf.jwe.keycloak.providers.AuthenticateUserProvider;

@Service
public class KeycloakPasswordGrantDataProvider implements AuthenticateUserProvider {

    private static final Logger log = LoggerFactory.getLogger(KeycloakPasswordGrantDataProvider.class);

    private final RestClient keycloakRestClient;
    private final KeycloakProperties properties;

    public KeycloakPasswordGrantDataProvider(RestClient keycloakRestClient, KeycloakProperties properties) {
        this.keycloakRestClient = keycloakRestClient;
        this.properties = properties;
    }

    @Override
    public KeycloakToken authenticate(String username, String password) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", properties.login().clientId());
        form.add("client_secret", properties.login().clientSecret());
        form.add("username", username);
        form.add("password", password);

        try {
            KeycloakTokenResponse response = keycloakRestClient.post()
                    .uri(properties.tokenEndpoint())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(KeycloakTokenResponse.class);

            if (response == null || response.accessToken() == null) {
                throw new KeycloakUnavailableException("Keycloak returned an empty token response");
            }
            return response.toKeycloakToken();
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode().is4xxClientError()) {
                log.warn("Keycloak rejected the password grant. Status: {}", e.getStatusCode());
                throw new KeycloakAuthenticationException("Invalid username or password", e);
            }
            log.error("Keycloak server error on password grant. Status: {}", e.getStatusCode());
            throw new KeycloakUnavailableException("Keycloak returned a server error", e);
        } catch (ResourceAccessException e) {
            log.error("Keycloak unreachable on password grant request", e);
            throw new KeycloakUnavailableException("Keycloak is unreachable", e);
        }
    }
}
