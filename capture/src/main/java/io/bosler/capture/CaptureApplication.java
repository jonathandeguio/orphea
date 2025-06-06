package io.bosler.capture;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@OpenAPIDefinition(
		info = @Info(
				title = "Capture.Bosler.io API",
				version = "1.0.1",
				contact = @Contact(
						name = "capture.bosler.io API Support",
						url = "https://bosler.io/contact",
						email = "techsupport@bosler.io")
		),
		servers = {
				@io.swagger.v3.oas.annotations.servers.Server(url = "http://localhost:8080", description = "CaptureAPI")
		}

)
@SpringBootApplication
@EnableScheduling
public class CaptureApplication {

	public static void main(String[] args) {
		SpringApplication.run(CaptureApplication.class, args);
	}

}
