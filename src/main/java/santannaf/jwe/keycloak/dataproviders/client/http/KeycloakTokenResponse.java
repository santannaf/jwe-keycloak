package santannaf.jwe.keycloak.dataproviders.client.http;

import santannaf.jwe.keycloak.entities.KeycloakToken;

/**
 * Raw token payload returned by the Keycloak token endpoint. JSON fields are snake_case and bound
 * through the global {@code SNAKE_CASE} naming strategy.
 */
record KeycloakTokenResponse(String accessToken, String tokenType, Long expiresIn, String refreshToken) {

    KeycloakToken toKeycloakToken() {
        return new KeycloakToken(accessToken, tokenType, expiresIn, refreshToken);
    }
}
