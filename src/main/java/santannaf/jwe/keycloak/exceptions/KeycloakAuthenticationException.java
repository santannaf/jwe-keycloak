package santannaf.jwe.keycloak.exceptions;

/**
 * Raised when Keycloak rejects the supplied client or user credentials (maps to HTTP 401).
 */
public class KeycloakAuthenticationException extends RuntimeException {

    public KeycloakAuthenticationException(String message) {
        super(message);
    }

    public KeycloakAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
