package santannaf.jwe.keycloak.usecases;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import santannaf.jwe.keycloak.exceptions.InvalidEncryptedTokenException;
import santannaf.jwe.keycloak.exceptions.InvalidTokenException;
import santannaf.jwe.keycloak.providers.DecryptTokenProvider;
import santannaf.jwe.keycloak.providers.ValidateKeycloakTokenProvider;

class ValidateEncryptedM2mTokenUseCaseTest {

    @Test
    void validJweAndTokenSucceed() {
        DecryptTokenProvider decryptor = jwe -> "access-token";
        ValidateKeycloakTokenProvider validator = accessToken -> { };

        ValidateEncryptedM2mTokenUseCase useCase = new ValidateEncryptedM2mTokenUseCase(decryptor, validator);

        assertDoesNotThrow(() -> useCase.execute("any-access_token"));
    }

    @Test
    void undecryptableJweFails() {
        DecryptTokenProvider decryptor = jwe -> {
            throw new InvalidEncryptedTokenException("bad access_token");
        };
        ValidateKeycloakTokenProvider validator = accessToken -> { };

        ValidateEncryptedM2mTokenUseCase useCase = new ValidateEncryptedM2mTokenUseCase(decryptor, validator);

        assertThrows(InvalidEncryptedTokenException.class, () -> useCase.execute("garbage"));
    }

    @Test
    void invalidInnerTokenFails() {
        DecryptTokenProvider decryptor = jwe -> "access-token";
        ValidateKeycloakTokenProvider validator = accessToken -> {
            throw new InvalidTokenException("expired");
        };

        ValidateEncryptedM2mTokenUseCase useCase = new ValidateEncryptedM2mTokenUseCase(decryptor, validator);

        assertThrows(InvalidTokenException.class, () -> useCase.execute("any-access_token"));
    }
}
