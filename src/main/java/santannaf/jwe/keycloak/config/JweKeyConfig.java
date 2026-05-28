package santannaf.jwe.keycloak.config;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JweKeyConfig {

    private static final int KEY_SIZE = 2048;

    /**
     * Ephemeral RSA key pair generated at a startup. The same backend encrypts (issue) and
     * decrypts (validate) the JWE, so no key material needs to be persisted or shared.
     */
    @Bean
    public KeyPair jweKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(KEY_SIZE);
        return generator.generateKeyPair();
    }
}
