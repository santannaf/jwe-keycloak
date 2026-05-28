package santannaf.jwe.keycloak.entities;

/**
 * An access token encrypted as a JWE in compact serialization.
 */
public record EncryptedToken(String jwe, String tokenType) {
}
