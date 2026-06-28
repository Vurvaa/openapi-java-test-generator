package org.example.openapitestgenerator.dto;

import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;

import java.util.List;

public record EndpointInfo(
        String path,
        String method,
        String operationId,
        String summary,
        List<SecurityRequirement> security,
        List<String> tags,
        List<Parameter> parameters,
        RequestBody requestBody,
        ApiResponses responses
) {
    @Override
    public String toString() {
        return "EndpointInfo{" +
                "path='" + path + '\'' +
                ", method='" + method + '\'' +
                ", operationId='" + operationId + '\'' +
                ", summary='" + summary + '\'' +
                ", security=" + security +
                ", tags=" + tags +
                ", parameters=" + parameters + '\'' +
                ", requestBody=" + requestBody +
                ", responses=" + responses +
                '}';
    }
}
