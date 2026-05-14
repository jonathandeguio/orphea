package io.orphea.fractal;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
//		tags = {
//				@Tag(name = "Create", description = "Create operations."),
//				@Tag(name = "Retrieve", description = "Get operations."),
//				@Tag(name = "Delete", description = "Delete operations."),
//		},
		info = @Info(
				title = "Orphea.io API",
				version = "1.0.1",
				contact = @Contact(
						name = "Orphea.io API Support",
						url = "https://orphea.io/contact",
						email = "techsupport@orphea.io")
		),
		servers = {
				@io.swagger.v3.oas.annotations.servers.Server(url = "https://dev.orphea.io", description = "Cloud"),
				@io.swagger.v3.oas.annotations.servers.Server(url = "http://localhost:8080", description = "OrpheaAPI")
		}

)


@SecurityScheme(
		name = "bearerAuth",
		type = SecuritySchemeType.HTTP,
		bearerFormat = "JWT",
		scheme = "bearer"
)

@SpringBootApplication
public class FractalApplication {

	public static void main(String[] args) {
		SpringApplication.run(FractalApplication.class, args);
	}

}
