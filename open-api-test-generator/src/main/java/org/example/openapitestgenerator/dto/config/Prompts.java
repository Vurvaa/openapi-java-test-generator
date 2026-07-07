package org.example.openapitestgenerator.dto.config;

import jakarta.validation.constraints.NotBlank;

public record Prompts(@NotBlank String apiSwaggerPrompt, @NotBlank String postmanPrompt,
                      @NotBlank String seleniumPrompt) {}
