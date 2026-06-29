package org.example.openapitestgenerator.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@Validated
@ConfigurationProperties
public record Config(
        @NotNull @Valid OutputConfig output,
        @NotNull @Valid ModelConfig model,
        @NotEmpty Map<String, String> startText)
{
    public record OutputConfig(@NotNull Boolean saveLlmInput, @NotBlank String dir) {}

    public record ModelConfig(
            @NotBlank String name,
            @NotBlank String baseUrl,
            @NotBlank String modelName,
            @NotNull
            @DecimalMin("0.0") @DecimalMax("2.0")
            Double temperature
    ) {}
}
