package santannaf.jwe.keycloak.usecases;

import jakarta.inject.Named;

import santannaf.jwe.keycloak.providers.DecryptTokenProvider;
import santannaf.jwe.keycloak.providers.ValidateKeycloakTokenProvider;

@Named
public class ValidateEncryptedM2mTokenUseCase {

    private final DecryptTokenProvider decryptProvider;
    private final ValidateKeycloakTokenProvider validateProvider;

    public ValidateEncryptedM2mTokenUseCase(DecryptTokenProvider decryptProvider,
                                            ValidateKeycloakTokenProvider validateProvider) {
        this.decryptProvider = decryptProvider;
        this.validateProvider = validateProvider;
    }

    public void execute(String jwe) {
        String accessToken = decryptProvider.decrypt(jwe);
        validateProvider.validate(accessToken);
        System.out.println("Token is ok");
    }
}
