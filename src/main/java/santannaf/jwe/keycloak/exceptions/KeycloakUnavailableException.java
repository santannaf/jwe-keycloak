package santannaf.jwe.keycloak.exceptions;

/**
 * Raised when Keycloak cannot be reached or returns a server-side error (maps to HTTP 502).
 */
public class KeycloakUnavailableException extends RuntimeException {

    public KeycloakUnavailableException(String message) {
        super(message);
    }

    public KeycloakUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
