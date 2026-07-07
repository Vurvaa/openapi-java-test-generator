package org.example.openapitestgenerator.dto.config;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ModelConfig(
        @NotBlank String name,
        @NotBlank String baseUrl,
        @NotBlank String modelName,
        @NotNull
        @DecimalMin("0.0") @DecimalMax("2.0")
        Double temperature
) {}
