package santannaf.jwe.keycloak.usecases;

import jakarta.inject.Named;

import santannaf.jwe.keycloak.entities.EncryptedUserTokens;
import santannaf.jwe.keycloak.entities.KeycloakToken;
import santannaf.jwe.keycloak.providers.AuthenticateUserProvider;
import santannaf.jwe.keycloak.providers.EncryptTokenProvider;

@Named
public class LoginUserUseCase {

    private final AuthenticateUserProvider authenticateUserProvider;
    private final EncryptTokenProvider encryptTokenProvider;

    public LoginUserUseCase(AuthenticateUserProvider authenticateUserProvider,
                            EncryptTokenProvider encryptTokenProvider) {
        this.authenticateUserProvider = authenticateUserProvider;
        this.encryptTokenProvider = encryptTokenProvider;
    }

    public EncryptedUserTokens execute(String username, String password) {
        KeycloakToken token = authenticateUserProvider.authenticate(username, password);
        String encryptedAccessToken = encryptTokenProvider.encrypt(token.accessToken());
        String encryptedRefreshToken = token.refreshToken() == null
                ? null
                : encryptTokenProvider.encrypt(token.refreshToken());
        return new EncryptedUserTokens(
                encryptedAccessToken, token.tokenType(), token.expiresIn(), encryptedRefreshToken);
    }
}
