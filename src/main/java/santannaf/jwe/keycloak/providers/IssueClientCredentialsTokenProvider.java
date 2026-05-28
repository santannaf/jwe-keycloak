package santannaf.jwe.keycloak.providers;

import santannaf.jwe.keycloak.entities.KeycloakToken;

/**
 * Obtains an access token from Keycloak using the {@code client_credentials} grant.
 */
public interface IssueClientCredentialsTokenProvider {

    KeycloakToken issue();
}
