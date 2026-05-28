package santannaf.jwe.keycloak.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "keycloak")
public record KeycloakProperties(String baseUrl, String realm, Client m2m, Client login) {

    public record Client(String clientId, String clientSecret) {
    }

    public String tokenEndpoint() {
        return baseUrl + "/realms/" + realm + "/protocol/openid-connect/token";
    }

    public String jwksEndpoint() {
        return baseUrl + "/realms/" + realm + "/protocol/openid-connect/certs";
    }

    public String issuer() {
        return baseUrl + "/realms/" + realm;
    }
}
