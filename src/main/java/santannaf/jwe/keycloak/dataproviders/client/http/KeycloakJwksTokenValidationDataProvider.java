package santannaf.jwe.keycloak.dataproviders.client.http;

import java.net.MalformedURLException;
import java.net.URI;
import java.text.ParseException;
import java.util.Set;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import santannaf.jwe.keycloak.config.KeycloakProperties;
import santannaf.jwe.keycloak.exceptions.InvalidTokenException;
import santannaf.jwe.keycloak.exceptions.KeycloakUnavailableException;
import santannaf.jwe.keycloak.providers.ValidateKeycloakTokenProvider;

@Service
public class KeycloakJwksTokenValidationDataProvider implements ValidateKeycloakTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(KeycloakJwksTokenValidationDataProvider.class);

    private final KeycloakProperties properties;
    private volatile ConfigurableJWTProcessor<SecurityContext> jwtProcessor;

    public KeycloakJwksTokenValidationDataProvider(KeycloakProperties properties) {
        this.properties = properties;
    }

    @Override
    public void validate(String accessToken) {
        try {
            jwtProcessor().process(accessToken, null);
        } catch (ParseException | JOSEException | BadJOSEException e) {
            // JOSEException covers BadJOSEException / BadJWTException (signature, issuer, expiry failures)
            log.warn("Inner Keycloak token failed validation: {}", e.getMessage());
            throw new InvalidTokenException("Inner Keycloak token is invalid", e);
        }
    }

    private ConfigurableJWTProcessor<SecurityContext> jwtProcessor() {
        ConfigurableJWTProcessor<SecurityContext> local = jwtProcessor;
        if (local == null) {
            synchronized (this) {
                local = jwtProcessor;
                if (local == null) {
                    local = buildProcessor();
                    jwtProcessor = local;
                }
            }
        }
        return local;
    }

    private ConfigurableJWTProcessor<SecurityContext> buildProcessor() {
        try {
            JWKSource<SecurityContext> jwkSource = JWKSourceBuilder
                    .create(URI.create(properties.jwksEndpoint()).toURL())
                    .build();

            DefaultJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();
            processor.setJWSKeySelector(new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwkSource));
            processor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>(
                    new JWTClaimsSet.Builder().issuer(properties.issuer()).build(),
                    Set.of("exp")));
            return processor;
        } catch (MalformedURLException e) {
            throw new KeycloakUnavailableException("Invalid Keycloak JWKS URL", e);
        }
    }
}
