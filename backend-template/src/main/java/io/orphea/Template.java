package io.orphea;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
        tags = {
                @Tag(name = "Create", description = "Create operations."),
                @Tag(name = "Retrieve", description = "Get operations."),
                @Tag(name = "Delete", description = "Delete operations."),
        },
        info = @Info(
                title = "Orphea.io API",
                version = "1.0.1",
                contact = @Contact(
                        name = "Orphea.io API Support",
                        url = "https://orphea.io/contact",
                        email = "techsupport@orphea.io")
        )
)
@SpringBootApplication
public class Template {

    public static void main(String[] args) {
        SpringApplication.run(Template.class, args);
    }

}
