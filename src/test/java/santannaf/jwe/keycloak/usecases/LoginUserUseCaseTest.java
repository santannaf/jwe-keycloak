package santannaf.jwe.keycloak.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import santannaf.jwe.keycloak.entities.EncryptedUserTokens;
import santannaf.jwe.keycloak.entities.KeycloakToken;
import santannaf.jwe.keycloak.exceptions.KeycloakAuthenticationException;
import santannaf.jwe.keycloak.providers.AuthenticateUserProvider;
import santannaf.jwe.keycloak.providers.EncryptTokenProvider;

class LoginUserUseCaseTest {

    @Test
    void returnsEncryptedTokensForValidCredentials() {
        AuthenticateUserProvider authProvider =
                (username, password) -> new KeycloakToken("access-1", "Bearer", 300L, "refresh-1");
        EncryptTokenProvider encryptor = plaintext -> "enc(" + plaintext + ")";

        LoginUserUseCase useCase = new LoginUserUseCase(authProvider, encryptor);
        EncryptedUserTokens tokens = useCase.execute("demo", "demo");

        assertEquals("enc(access-1)", tokens.accessToken());
        assertEquals("enc(refresh-1)", tokens.refreshToken());
        assertEquals("Bearer", tokens.tokenType());
    }

    @Test
    void failsForRejectedCredentials() {
        AuthenticateUserProvider authProvider = (username, password) -> {
            throw new KeycloakAuthenticationException("invalid");
        };
        EncryptTokenProvider encryptor = plaintext -> "unused";

        LoginUserUseCase useCase = new LoginUserUseCase(authProvider, encryptor);

        assertThrows(KeycloakAuthenticationException.class, () -> useCase.execute("demo", "wrong"));
    }
}
