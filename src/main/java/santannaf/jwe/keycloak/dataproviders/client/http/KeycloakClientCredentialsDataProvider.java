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
import santannaf.jwe.keycloak.providers.IssueClientCredentialsTokenProvider;

@Service
public class KeycloakClientCredentialsDataProvider implements IssueClientCredentialsTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(KeycloakClientCredentialsDataProvider.class);

    private final RestClient keycloakRestClient;
    private final KeycloakProperties properties;

    public KeycloakClientCredentialsDataProvider(RestClient keycloakRestClient, KeycloakProperties properties) {
        this.keycloakRestClient = keycloakRestClient;
        this.properties = properties;
    }

    @Override
    public KeycloakToken issue() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", properties.m2m().clientId());
        form.add("client_secret", properties.m2m().clientSecret());

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
                log.warn("Keycloak rejected client_credentials request. Status: {}", e.getStatusCode());
                throw new KeycloakAuthenticationException("Keycloak rejected the client credentials", e);
            }
            log.error("Keycloak server error on client_credentials. Status: {}", e.getStatusCode());
            throw new KeycloakUnavailableException("Keycloak returned a server error", e);
        } catch (ResourceAccessException e) {
            log.error("Keycloak unreachable on client_credentials request", e);
            throw new KeycloakUnavailableException("Keycloak is unreachable", e);
        }
    }
}
