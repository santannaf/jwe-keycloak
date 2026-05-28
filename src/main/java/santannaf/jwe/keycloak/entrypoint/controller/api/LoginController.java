package santannaf.jwe.keycloak.entrypoint.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import santannaf.jwe.keycloak.entities.EncryptedUserTokens;
import santannaf.jwe.keycloak.entrypoint.controller.api.dto.LoginRequest;
import santannaf.jwe.keycloak.entrypoint.controller.api.dto.LoginResponse;
import santannaf.jwe.keycloak.usecases.LoginUserUseCase;

@RestController
public class LoginController {

    private final LoginUserUseCase loginUseCase;

    public LoginController(LoginUserUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

    @PostMapping("/oauth/login")
    public ResponseEntity<LoginResponse> login(@RequestBody(required = false) LoginRequest request) {
        if (request == null || isBlank(request.username()) || isBlank(request.password())) {
            throw new IllegalArgumentException("username and password are required");
        }

        EncryptedUserTokens tokens = loginUseCase.execute(request.username(), request.password());
        return ResponseEntity.ok(new LoginResponse(
                tokens.accessToken(),
                tokens.tokenType(),
                tokens.expiresIn(),
                tokens.refreshToken()));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
