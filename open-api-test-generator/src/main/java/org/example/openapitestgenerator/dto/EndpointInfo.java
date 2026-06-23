package org.example.openapitestgenerator.dto;

import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.List;

public record EndpointInfo(
        String path,
        String method,
        String operationId,
        String summary,
        List<String> tags,
        RequestBody requestBody,
        ApiResponses responses
) {
}
