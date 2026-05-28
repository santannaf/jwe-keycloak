package santannaf.jwe.keycloak.exceptions;

/**
 * Raised when the inner Keycloak token fails validation (bad signature, issuer, or expired).
 * Maps to HTTP 401.
 */
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
