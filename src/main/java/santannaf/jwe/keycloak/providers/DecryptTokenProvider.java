package santannaf.jwe.keycloak.providers;

/**
 * Decrypts a JWE compact serialization back into its plaintext token.
 */
public interface DecryptTokenProvider {

    String decrypt(String jwe);
}
