package org.example.openapitestgenerator.dto.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties
public record Config(@NotNull @Valid ModelConfig model, @NotNull @Valid Prompts prompts) {}
