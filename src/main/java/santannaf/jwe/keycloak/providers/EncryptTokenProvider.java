package santannaf.jwe.keycloak.providers;

/**
 * Encrypts a plaintext token into a JWE compact serialization.
 */
public interface EncryptTokenProvider {

    String encrypt(String plaintext);
}
