package io.movetodata.capture;

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
				title = "Capture.MoveToData.io API",
				version = "1.0.1",
				contact = @Contact(
						name = "capture.movetodata.io API Support",
						url = "https://movetodata.io/contact",
						email = "techsupport@movetodata.io")
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
