package santannaf.jwe.keycloak.entities;

/**
 * Token material returned by Keycloak. {@code refreshToken} may be absent (e.g. client_credentials).
 */
public record KeycloakToken(String accessToken, String tokenType, Long expiresIn, String refreshToken) {
}
