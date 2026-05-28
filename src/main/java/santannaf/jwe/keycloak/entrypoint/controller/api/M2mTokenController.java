package santannaf.jwe.keycloak.entrypoint.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import santannaf.jwe.keycloak.entities.EncryptedToken;
import santannaf.jwe.keycloak.entrypoint.controller.api.dto.IssueTokenResponse;
import santannaf.jwe.keycloak.entrypoint.controller.api.dto.ValidateTokenRequest;
import santannaf.jwe.keycloak.entrypoint.controller.api.dto.ValidationResponse;
import santannaf.jwe.keycloak.usecases.IssueEncryptedM2mTokenUseCase;
import santannaf.jwe.keycloak.usecases.ValidateEncryptedM2mTokenUseCase;

@RestController
@RequestMapping("/oauth/m2m")
public class M2mTokenController {

    private final IssueEncryptedM2mTokenUseCase issueUseCase;
    private final ValidateEncryptedM2mTokenUseCase validateUseCase;

    public M2mTokenController(IssueEncryptedM2mTokenUseCase issueUseCase,
                              ValidateEncryptedM2mTokenUseCase validateUseCase) {
        this.issueUseCase = issueUseCase;
        this.validateUseCase = validateUseCase;
    }

    @PostMapping("/token")
    public ResponseEntity<IssueTokenResponse> issue() {
        EncryptedToken token = issueUseCase.execute();
        return ResponseEntity.ok(new IssueTokenResponse(token.jwe(), token.tokenType()));
    }

    @PostMapping("/login")
    public ResponseEntity<ValidationResponse> validate(@RequestBody(required = false) ValidateTokenRequest request) {
        if (request == null || request.token() == null || request.token().isBlank()) {
            throw new IllegalArgumentException("token is required");
        }
        validateUseCase.execute(request.token());
        return ResponseEntity.ok(new ValidationResponse("ok"));
    }
}
