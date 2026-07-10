package io.movetodata;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

//String baseUrl = System.getenv("BASE_URL");

@Configuration
@OpenAPIDefinition(
//		tags = {
//				@Tag(name = "Create", description = "Create operations."),
//				@Tag(name = "Retrieve", description = "Get operations."),
//				@Tag(name = "Delete", description = "Delete operations."),
//		},
        info = @Info(
                title = "MoveToData.io API",
                version = "1.0.1",
                contact = @Contact(
                        name = "MoveToData.io API Support",
                        url = "https://movetodata.io/contact",
                        email = "techsupport@movetodata.io")
        ),
        servers = {
                @io.swagger.v3.oas.annotations.servers.Server(url = "https://dev.movetodata.io", description = "Cloud"),
                @io.swagger.v3.oas.annotations.servers.Server(url = "http://localhost:8080", description = "MoveToDataAPI")
        }

)

// temporarily disabled
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)

@SpringBootApplication
//@EnableScheduling
//@ComponentScan
//@EntityScan
//@EnableJpaRepositories
//@EnableConfigurationProperties(AppProperties.class)
public class Boson {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Boson.class, args);

    }

}

