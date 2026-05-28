package santannaf.jwe.keycloak.providers;

import santannaf.jwe.keycloak.entities.KeycloakToken;

/**
 * Authenticates a user against Keycloak using the password (direct access) grant.
 */
public interface AuthenticateUserProvider {

    KeycloakToken authenticate(String username, String password);
}
