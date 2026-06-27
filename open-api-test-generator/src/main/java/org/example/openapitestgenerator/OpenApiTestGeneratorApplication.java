package org.example.openapitestgenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class OpenApiTestGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenApiTestGeneratorApplication.class, args);
    }
}
