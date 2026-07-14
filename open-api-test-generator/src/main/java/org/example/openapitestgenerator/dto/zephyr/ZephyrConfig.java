package org.example.openapitestgenerator.dto.zephyr;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "zephyr")
public record ZephyrConfig(
        String baseUrl,
        String apiToken,
        String projectKey,
        Integer folderId,
        String ownerId,
        String defaultStatusName,
        String defaultPriorityName
) {}
