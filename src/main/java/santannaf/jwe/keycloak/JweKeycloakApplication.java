package santannaf.jwe.keycloak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import santannaf.jwe.keycloak.config.KeycloakProperties;

@SpringBootApplication
@EnableConfigurationProperties(KeycloakProperties.class)
public class JweKeycloakApplication {

    public static void main(String[] args) {
        SpringApplication.run(JweKeycloakApplication.class, args);
    }

}
