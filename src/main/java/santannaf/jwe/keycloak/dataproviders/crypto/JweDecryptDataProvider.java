package santannaf.jwe.keycloak.dataproviders.crypto;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.text.ParseException;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.RSADecrypter;
import org.springframework.stereotype.Component;

import santannaf.jwe.keycloak.exceptions.InvalidEncryptedTokenException;
import santannaf.jwe.keycloak.providers.DecryptTokenProvider;

@Component
public class JweDecryptDataProvider implements DecryptTokenProvider {

    private final RSAPrivateKey privateKey;

    public JweDecryptDataProvider(KeyPair jweKeyPair) {
        this.privateKey = (RSAPrivateKey) jweKeyPair.getPrivate();
    }

    @Override
    public String decrypt(String jwe) {
        try {
            JWEObject jweObject = JWEObject.parse(jwe);
            jweObject.decrypt(new RSADecrypter(privateKey));
            return jweObject.getPayload().toString();
        } catch (ParseException | JOSEException e) {
            throw new InvalidEncryptedTokenException("Value is not a valid JWE or cannot be decrypted", e);
        }
    }
}
