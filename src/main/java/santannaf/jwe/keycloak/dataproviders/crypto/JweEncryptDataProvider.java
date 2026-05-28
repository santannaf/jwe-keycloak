package santannaf.jwe.keycloak.dataproviders.crypto;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSAEncrypter;
import org.springframework.stereotype.Component;

import santannaf.jwe.keycloak.providers.EncryptTokenProvider;

@Component
public class JweEncryptDataProvider implements EncryptTokenProvider {

    private final RSAPublicKey publicKey;

    public JweEncryptDataProvider(KeyPair jweKeyPair) {
        this.publicKey = (RSAPublicKey) jweKeyPair.getPublic();
    }

    @Override
    public String encrypt(String plaintext) {
        try {
            JWEObject jweObject = new JWEObject(
                    new JWEHeader(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM),
                    new Payload(plaintext));
            jweObject.encrypt(new RSAEncrypter(publicKey));
            return jweObject.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Failed to encrypt token as JWE", e);
        }
    }
}
