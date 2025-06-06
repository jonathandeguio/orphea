package io.bosler;


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
                title = "Bosler.io API",
                version = "1.0.1",
                contact = @Contact(
                        name = "Bosler.io API Support",
                        url = "https://bosler.io/contact",
                        email = "techsupport@bosler.io")
        )
)
@SpringBootApplication
public class Template {

    public static void main(String[] args) {
        SpringApplication.run(Template.class, args);
    }

}
