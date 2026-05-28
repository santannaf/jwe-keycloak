package santannaf.jwe.keycloak.entrypoint.controller.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import santannaf.jwe.keycloak.entrypoint.controller.api.dto.ErrorResponse;
import santannaf.jwe.keycloak.exceptions.InvalidEncryptedTokenException;
import santannaf.jwe.keycloak.exceptions.InvalidTokenException;
import santannaf.jwe.keycloak.exceptions.KeycloakAuthenticationException;
import santannaf.jwe.keycloak.exceptions.KeycloakUnavailableException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(KeycloakAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthFailure(KeycloakAuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(e.getMessage()));
    }

    // A bad token (malformed/undecryptable JWE or invalid inner token) returns 401 with no body,
    // so the response never reveals which validation stage failed.
    @ExceptionHandler({InvalidTokenException.class, InvalidEncryptedTokenException.class})
    public ResponseEntity<Void> handleInvalidToken(RuntimeException e) {
        log.debug("Token validation failed: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ExceptionHandler(KeycloakUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleBadGateway(KeycloakUnavailableException ignored) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new ErrorResponse("Keycloak is unavailable"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        log.error("Unexpected error handling request", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Internal error"));
    }
}
