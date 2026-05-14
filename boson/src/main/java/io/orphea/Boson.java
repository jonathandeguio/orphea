package io.orphea;

import io.orphea.config.AppProperties;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Orphea.io API",
                version = "1.0.1",
                contact = @Contact(
                        name = "Orphea.io API Support",
                        url = "https://orphea.io/contact",
                        email = "techsupport@orphea.io")
        ),
        servers = {
                @io.swagger.v3.oas.annotations.servers.Server(url = "https://buran.orphea.io", description = "Orphea Dev Server"),
                @io.swagger.v3.oas.annotations.servers.Server(url = "http://localhost:8080", description = "Orphea Local Host")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@SpringBootApplication
@EnableScheduling
@EnableTransactionManagement
@EnableConfigurationProperties(AppProperties.class)
public class Boson {
    private static final Logger logger = LoggerFactory.getLogger(Boson.class);

    public static void main(String[] args) {

        ApplicationContext context = SpringApplication.run(Boson.class, args);
        logger.info("Boson Started");
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
