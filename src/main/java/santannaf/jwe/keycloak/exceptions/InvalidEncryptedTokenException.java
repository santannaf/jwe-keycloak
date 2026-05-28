package santannaf.jwe.keycloak.exceptions;

/**
 * Raised when a submitted value is not a valid JWE or cannot be decrypted (maps to HTTP 400).
 */
public class InvalidEncryptedTokenException extends RuntimeException {

    public InvalidEncryptedTokenException(String message) {
        super(message);
    }

    public InvalidEncryptedTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
