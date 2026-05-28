package santannaf.jwe.keycloak.providers;

/**
 * Validates a Keycloak access token: signature against the realm JWKS, issuer, and expiry.
 */
public interface ValidateKeycloakTokenProvider {

    void validate(String accessToken);
}
