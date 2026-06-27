package org.example.openapitestgenerator.service;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.example.openapitestgenerator.dto.EndpointInfo;
import org.example.openapitestgenerator.exceptions.OpenApiParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ParserService {
    private static final Logger log = LoggerFactory.getLogger(ParserService.class);

    private static final Set<String> SUPPORTED_METHODS =
            Set.of("get", "post", "put", "delete", "patch", "head", "options", "trace");

    public List<EndpointInfo> parseOpenApiSpec(String openApiText){
        if(openApiText == null || openApiText.isEmpty()){
            log.error("OpenAPI specification text is null or empty");
            throw new OpenApiParsingException("OpenAPI specification text is null or empty");
        }

        OpenAPIV3Parser parser = new OpenAPIV3Parser();
        ParseOptions parseOptions = new ParseOptions();
        parseOptions.setResolve(true);
        parseOptions.setResolveFully(true);
        SwaggerParseResult result = parser.readContents(openApiText, null, parseOptions);

        if(result.getMessages() != null && !result.getMessages().isEmpty()){
            log.warn(result.getMessages().toString());
        }

        OpenAPI openAPI = result.getOpenAPI();
        if(openAPI == null){
            log.error("Couldn't parse the OpenAPI document");
            throw new OpenApiParsingException("Couldn't parse the OpenAPI document");
        }
        return extractEndpoints(openAPI);
    }

    private List<EndpointInfo> extractEndpoints(OpenAPI openAPI) {
        List<EndpointInfo> endpoints = new ArrayList<>();
        Paths paths = openAPI.getPaths();

        if (paths == null) {
            return endpoints;
        }

        for (Map.Entry<String, PathItem> entry : paths.entrySet()) {
            String path = entry.getKey();
            PathItem pathItem = entry.getValue();

            if (pathItem == null) {
                continue;
            }

            Map<PathItem.HttpMethod, Operation> operationsMap = pathItem.readOperationsMap();
            for (Map.Entry<PathItem.HttpMethod, Operation> operationEntry : operationsMap.entrySet()) {
                String method = operationEntry.getKey().name().toLowerCase();
                Operation operation = operationEntry.getValue();

                if (!SUPPORTED_METHODS.contains(method) || operation == null) {
                    continue;
                }
                EndpointInfo endpointInfo = new EndpointInfo(
                        path,
                        method.toUpperCase(),
                        operation.getOperationId(),
                        operation.getSummary(),
                        operation.getTags() != null ? operation.getTags() : List.of(),
                        operation.getParameters() != null ? operation.getParameters() : List.of(),
                        operation.getRequestBody(),
                        operation.getResponses()
                );
                endpoints.add(endpointInfo);
            }

        }
        return endpoints;
    }


}
