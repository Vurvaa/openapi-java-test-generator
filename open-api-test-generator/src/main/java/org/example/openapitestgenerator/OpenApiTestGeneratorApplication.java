package org.example.openapitestgenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.nio.charset.StandardCharsets;

@SpringBootApplication
@ConfigurationPropertiesScan
public class OpenApiTestGeneratorApplication
{

    public static void main(String[] args)
    {
        System.setOut(new java.io.PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setProperty("spring.shell.interactive.enabled", "true");
        SpringApplication.run(OpenApiTestGeneratorApplication.class, args);
    }
}