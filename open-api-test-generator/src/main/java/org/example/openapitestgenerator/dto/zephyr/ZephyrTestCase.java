package org.example.openapitestgenerator.dto.zephyr;

import java.util.List;

public record ZephyrTestCase(
        String projectKey,
        String name,
        Integer folderId,
        String ownerId,
        String statusName,
        String priorityName,
        List<String> labels,
        String precondition,
        String objective,
        TestScript testScript
) {}
