package santannaf.jwe.keycloak.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import santannaf.jwe.keycloak.entities.EncryptedToken;
import santannaf.jwe.keycloak.entities.KeycloakToken;
import santannaf.jwe.keycloak.providers.EncryptTokenProvider;
import santannaf.jwe.keycloak.providers.IssueClientCredentialsTokenProvider;
import santannaf.jwe.keycloak.exceptions.KeycloakUnavailableException;

class IssueEncryptedM2mTokenUseCaseTest {

    @Test
    void encryptsAccessTokenObtainedFromKeycloak() {
        IssueClientCredentialsTokenProvider issuer =
                () -> new KeycloakToken("access-123", "Bearer", 300L, null);
        EncryptTokenProvider encryptor = plaintext -> "access_token(" + plaintext + ")";

        IssueEncryptedM2mTokenUseCase useCase = new IssueEncryptedM2mTokenUseCase(issuer, encryptor);
        EncryptedToken result = useCase.execute();

        assertEquals("access_token(access-123)", result.jwe());
        assertEquals("Bearer", result.tokenType());
    }

    @Test
    void propagatesKeycloakFailure() {
        IssueClientCredentialsTokenProvider issuer = () -> {
            throw new KeycloakUnavailableException("down");
        };
        EncryptTokenProvider encryptor = plaintext -> "unused";

        IssueEncryptedM2mTokenUseCase useCase = new IssueEncryptedM2mTokenUseCase(issuer, encryptor);

        assertThrows(KeycloakUnavailableException.class, useCase::execute);
    }
}
