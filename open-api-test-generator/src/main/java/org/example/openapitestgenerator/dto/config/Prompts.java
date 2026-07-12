package org.example.openapitestgenerator.dto.config;

import jakarta.validation.constraints.NotBlank;

public record Prompts(@NotBlank String seleniumPrompt, @NotBlank String postmanDataFixerPrompt,
                      @NotBlank String testCasesPrompt) {}
