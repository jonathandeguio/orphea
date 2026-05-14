package io.orphea;


import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.File;

@SpringBootApplication
@RequiredArgsConstructor
public class InitializeData {

    @Bean
    CommandLineRunner run(
    ) {
        return args -> {

            File orpheaJuliaHome = new File(System.getenv("JULIA_BASE_PATH"));
            if (!orpheaJuliaHome.exists()) {
                orpheaJuliaHome.mkdirs();
            }


        };
    }
}