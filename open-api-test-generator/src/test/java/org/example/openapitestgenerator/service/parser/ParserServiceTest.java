package org.example.openapitestgenerator.service.parser;

import io.swagger.v3.oas.models.parameters.Parameter;
import org.example.openapitestgenerator.dto.endpoints.EndpointInfo;
import org.example.openapitestgenerator.exceptions.OpenApiParsingException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ParserServiceTest {
    private final ParserService parserService = new ParserService();

    @Test
    void parserOpenApiSpecShouldThrowExceptionWhenTextIsNull() {
        assertThrows(
                OpenApiParsingException.class,
                ()->parserService.parseOpenApiSpec(null)
        );
    }

    @Test
    void parserOpenApiSpecShouldThrowExceptionwhenTextisEmpty() {
        assertThrows(
                OpenApiParsingException.class,
                () -> parserService.parseOpenApiSpec("")
        );
    }

    @Test
    void parseOpenApiSpecShouldParseSingleGetEndpoint() {
        String openApiText = """
            openapi: 3.0.0
            info:
              title: Test API
              version: 1.0.0

            paths:
              /users:
                get:
                  operationId: getUsers
                  summary: Get users
                  tags:
                    - Users
                  responses:
                    "200":
                      description: OK
            """;
        List<EndpointInfo> endpoints = parserService.parseOpenApiSpec(openApiText);

        assertEquals(1, endpoints.size());

        EndpointInfo endpoint = endpoints.get(0);


        assertEquals("/users",endpoint.path());
        assertEquals("GET",endpoint.method());
        assertEquals("getUsers", endpoint.operationId());
        assertEquals("Get users", endpoint.summary());
        assertEquals(List.of("Users"), endpoint.tags());

        assertNotNull(endpoint.responses());
        assertTrue(endpoint.responses().containsKey("200"));

    }

    @Test
    void parseOpenApiSpecShouldParseMultipleEndpoints() {
        String openApiText = """
            openapi: 3.0.0
            info:
              title: Test API
              version: 1.0.0

            paths:
              /users:
                get:
                  operationId: getUsers
                  responses:
                    "200":
                      description: OK
                post:
                  operationId: createUser
                  responses:
                    "201":
                      description: Created

              /users/{id}:
                get:
                  operationId: getUserById
                  responses:
                    "200":
                      description: OK
                    "404":
                      description: Not Found
                delete:
                  operationId: deleteUser
                  responses:
                    "204":
                      description: No Content
            """;

        List<EndpointInfo> endpoints = parserService.parseOpenApiSpec(openApiText);

        assertEquals(4, endpoints.size());

        assertTrue(endpoints.stream()
                .anyMatch(endpoint -> endpoint.path().equals("/users")
                        && endpoint.method().equals("GET")
                        && endpoint.operationId().equals("getUsers")));

        assertTrue(endpoints.stream()
                .anyMatch(endpoint -> endpoint.path().equals("/users")
                        && endpoint.method().equals("POST")
                        && endpoint.operationId().equals("createUser")));

        assertTrue(endpoints.stream()
                .anyMatch(endpoint -> endpoint.path().equals("/users/{id}")
                        && endpoint.method().equals("GET")
                        && endpoint.operationId().equals("getUserById")));

        assertTrue(endpoints.stream()
                .anyMatch(endpoint -> endpoint.path().equals("/users/{id}")
                        && endpoint.method().equals("DELETE")
                        && endpoint.operationId().equals("deleteUser")));
    }

    @Test
    void parseOpenApiSpecShouldParseQueryParameters() {
        String openApiText = """
            openapi: 3.0.0
            info:
              title: Test API
              version: 1.0.0

            paths:
              /users:
                get:
                  operationId: getUsers
                  parameters:
                    - name: page
                      in: query
                      required: false
                      schema:
                        type: integer
                    - name: size
                      in: query
                      required: false
                      schema:
                        type: integer
                  responses:
                    "200":
                      description: OK
            """;

        List<EndpointInfo> endpoints = parserService.parseOpenApiSpec(openApiText);

        assertEquals(1, endpoints.size());

        EndpointInfo endpoint = endpoints.get(0);

        assertEquals(2, endpoint.parameters().size());

        assertEquals("page", endpoint.parameters().get(0).getName());
        assertEquals("query", endpoint.parameters().get(0).getIn());
        assertFalse(endpoint.parameters().get(0).getRequired());

        assertEquals("size", endpoint.parameters().get(1).getName());
        assertEquals("query", endpoint.parameters().get(1).getIn());
        assertFalse(endpoint.parameters().get(1).getRequired());
    }

    @Test
    void parseOpenApiSpecShouldParsePathParameter() {
        String openApiText = """
            openapi: 3.0.0
            info:
              title: Test API
              version: 1.0.0

            paths:
              /users/{id}:
                get:
                  operationId: getUserById
                  parameters:
                    - name: id
                      in: path
                      required: true
                      schema:
                        type: integer
                  responses:
                    "200":
                      description: OK
                    "404":
                      description: Not Found
            """;

        List<EndpointInfo> endpoints = parserService.parseOpenApiSpec(openApiText);

        assertEquals(1, endpoints.size());

        EndpointInfo endpoint = endpoints.get(0);

        assertEquals("/users/{id}", endpoint.path());
        assertEquals("GET", endpoint.method());

        assertEquals(1, endpoint.parameters().size());

        Parameter id = endpoint.parameters().get(0);

        assertEquals("id", id.getName());
        assertEquals("path", id.getIn());
        assertTrue(id.getRequired());

        assertTrue(endpoint.responses().containsKey("200"));
        assertTrue(endpoint.responses().containsKey("404"));
    }

    @Test
    void parseOpenApiSpecShouldParseRequestBody() {
        String openApiText = """
            openapi: 3.0.0
            info:
              title: Test API
              version: 1.0.0

            paths:
              /users:
                post:
                  operationId: createUser
                  requestBody:
                    required: true
                    content:
                      application/json:
                        schema:
                          type: object
                          properties:
                            name:
                              type: string
                  responses:
                    "201":
                      description: Created
            """;

        List<EndpointInfo> endpoints = parserService.parseOpenApiSpec(openApiText);

        assertEquals(1, endpoints.size());

        EndpointInfo endpoint = endpoints.get(0);

        assertEquals("/users", endpoint.path());
        assertEquals("POST", endpoint.method());
        assertNotNull(endpoint.requestBody());
        assertTrue(endpoint.requestBody().getRequired());
        assertTrue(endpoint.requestBody().getContent().containsKey("application/json"));
    }


    @Test
    void parseOpenApiSpecShouldParseResponses() {
        String openApiText = """
            openapi: 3.0.0
            info:
              title: Test API
              version: 1.0.0

            paths:
              /users/{id}:
                get:
                  operationId: getUserById
                  responses:
                    "200":
                      description: OK
                    "400":
                      description: Bad Request
                    "404":
                      description: Not Found
            """;

        List<EndpointInfo> endpoints = parserService.parseOpenApiSpec(openApiText);

        assertEquals(1, endpoints.size());

        EndpointInfo endpoint = endpoints.get(0);

        assertNotNull(endpoint.responses());
        assertTrue(endpoint.responses().containsKey("200"));
        assertTrue(endpoint.responses().containsKey("400"));
        assertTrue(endpoint.responses().containsKey("404"));
    }


    @Test
    void parseOpenApiSpecShouldUseEmptyListsWhenTagsAndParametersAreMissing() {
        String openApiText = """
            openapi: 3.0.0
            info:
              title: Test API
              version: 1.0.0

            paths:
              /health:
                get:
                  operationId: healthCheck
                  responses:
                    "200":
                      description: OK
            """;

        List<EndpointInfo> endpoints = parserService.parseOpenApiSpec(openApiText);

        assertEquals(1, endpoints.size());

        EndpointInfo endpoint = endpoints.get(0);

        assertNotNull(endpoint.tags());
        assertTrue(endpoint.tags().isEmpty());

        assertNotNull(endpoint.parameters());
        assertTrue(endpoint.parameters().isEmpty());
    }

    @Test
    void parseOpenApiSpecShouldThrowExceptionWhenOpenApiDocumentIsInvalid() {
        String invalidOpenApiText = """
            invalid:
              yaml:
                - without openapi structure
            """;

        assertThrows(
                OpenApiParsingException.class,
                () -> parserService.parseOpenApiSpec(invalidOpenApiText)
        );
    }
}
