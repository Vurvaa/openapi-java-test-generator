package org.example.openapitestgenerator;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@Validated
@ConfigurationProperties()
public record Config(
        @NotNull OutputConfig output,
        @NotNull GitConfig git,
        @NotEmpty Map<String, ModelConfig> models,
        @NotBlank String logPath,
        @NotBlank String logLevel,
        @NotEmpty Map<String, String> startText)
{
    public record OutputConfig(boolean saveLlmInput, @NotBlank String dir) {}

    public record GitConfig(
            @NotBlank String accessToken,
            @NotBlank String defaultRepoUrl,
            @NotBlank String defaultBranch,
            @NotNull PostmanCollectionsConfig postmanCollections
    ) {}

    public record PostmanCollectionsConfig(
            @NotBlank String accessToken,
            @NotBlank String defaultRepoUrl,
            @NotBlank String defaultBranch
    ) {}

    public record ModelConfig(
            @NotBlank String name,
            String apiKey,
            @NotBlank String baseUrl,
            @NotBlank String modelName,
            double temperature,
            boolean enabled
    ) {}
}
