package io.movetodata;


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

            File movetodataJuliaHome = new File(System.getenv("JULIA_BASE_PATH"));
            if (!movetodataJuliaHome.exists()) {
                movetodataJuliaHome.mkdirs();
            }


        };
    }
}