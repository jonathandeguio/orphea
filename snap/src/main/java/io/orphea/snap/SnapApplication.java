package io.orphea.snap;


import io.orphea.snap.config.AppProperties;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

//String baseUrl = System.getenv("BASE_URL");

@Configuration
@OpenAPIDefinition(
//				tags = {
//				@Tag(name = "Create", description = "Create operations."),
//				@Tag(name = "Retrieve", description = "Get operations."),
//				@Tag(name = "Delete", description = "Delete operations."),
//		},
		info = @Info(
				title = "Snap.Orphea.io API",
				version = "1.0.1",
				contact = @Contact(
						name = "snap.orphea.io API Support",
						url = "https://orphea.io/contact",
						email = "techsupport@orphea.io")
		),
		servers = {
				@io.swagger.v3.oas.annotations.servers.Server(url = "https://snap.orphea.io:8082", description = "Cloud"),
				@io.swagger.v3.oas.annotations.servers.Server(url = "http://localhost:8080", description = "SnapAPI")
				
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
@ComponentScan
@EnableTransactionManagement
@EnableConfigurationProperties(AppProperties.class)
@EnableAsync
public class SnapApplication {

	public static void main(String[] args) {
		SpringApplication.run(SnapApplication.class, args);
	}

}
