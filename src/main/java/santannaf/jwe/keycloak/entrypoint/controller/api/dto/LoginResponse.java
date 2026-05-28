package santannaf.jwe.keycloak.entrypoint.controller.api.dto;

public record LoginResponse(String accessToken, String tokenType, Long expiresIn, String refreshToken) {
}
