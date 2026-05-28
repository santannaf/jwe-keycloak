package santannaf.jwe.keycloak.entities;

/**
 * User login result with the access and refresh tokens encrypted as JWEs (compact serialization).
 */
public record EncryptedUserTokens(String accessToken, String tokenType, Long expiresIn, String refreshToken) {
}
