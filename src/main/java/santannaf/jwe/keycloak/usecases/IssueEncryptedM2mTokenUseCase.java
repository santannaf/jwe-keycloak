package santannaf.jwe.keycloak.usecases;

import jakarta.inject.Named;

import santannaf.jwe.keycloak.entities.EncryptedToken;
import santannaf.jwe.keycloak.entities.KeycloakToken;
import santannaf.jwe.keycloak.providers.EncryptTokenProvider;
import santannaf.jwe.keycloak.providers.IssueClientCredentialsTokenProvider;

@Named
public class IssueEncryptedM2mTokenUseCase {

    private final IssueClientCredentialsTokenProvider issueProvider;
    private final EncryptTokenProvider encryptProvider;

    public IssueEncryptedM2mTokenUseCase(IssueClientCredentialsTokenProvider issueProvider,
                                         EncryptTokenProvider encryptProvider) {
        this.issueProvider = issueProvider;
        this.encryptProvider = encryptProvider;
    }

    public EncryptedToken execute() {
        KeycloakToken token = issueProvider.issue();
        String jwe = encryptProvider.encrypt(token.accessToken());
        return new EncryptedToken(jwe, token.tokenType());
    }
}
