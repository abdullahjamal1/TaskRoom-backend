package app.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@Data
@ConfigurationProperties(prefix = "app")
@ConfigurationPropertiesScan("application.properties")
public class ApplicationConfig {

    private String secret;

    private String url;

    private String emailFrom;

    private String emailSupport;

    private boolean emailErrors;

    private boolean emailMock;

    private String userRoot;

    private boolean userVerification;

    private String frontendUrl;

    private String mongoUrl;

    public String getMongoUrl(){
        System.out.println(System.getenv(this.mongoUrl).substring(1));
        return System.getenv(this.mongoUrl).substring(1);
    }
 

}
