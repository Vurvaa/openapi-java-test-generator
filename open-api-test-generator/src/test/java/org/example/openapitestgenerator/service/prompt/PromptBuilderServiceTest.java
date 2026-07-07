package org.example.openapitestgenerator.service.prompt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.example.openapitestgenerator.dto.EndpointInfo;
import org.example.openapitestgenerator.exceptions.PromptBuildingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PromptBuilderServiceTest {

    private final PromptBuilderService promptBuilderService = new PromptBuilderService();

    @Test
    void buildPromptShouldBuildPromptForSingleEndpoint() {
        EndpointInfo endpoint = endpoint(Map.of(
                "path", "/api/v1/users",
                "method", "GET",
                "operationId", "findUsers",
                "summary", "Find users",
                "tags", List.of("users"),
                "responses", Map.of(
                        "200", Map.of("description", "OK")
                )
        ));

        String result = promptBuilderService.buildPrompt(List.of(endpoint));

        assertThat(result)
                .contains("Тебе передают набор endpoint'ов OpenAPI")
                .contains("ENDPOINTS:")
                .contains("--- Endpoint 1 ---")
                .contains("\"path\"")
                .contains("/api/v1/users")
                .contains("\"method\"")
                .contains("GET")
                .contains("\"operationId\"")
                .contains("findUsers")
                .contains("\"responses\"")
                .contains("200");
    }

    @Test
    void buildPromptShouldBuildPromptForMultipleEndpoints() {
        EndpointInfo firstEndpoint = endpoint(Map.of(
                "path", "/api/v1/users",
                "method", "GET",
                "operationId", "findUsers"
        ));

        EndpointInfo secondEndpoint = endpoint(Map.of(
                "path", "/api/v1/users",
                "method", "POST",
                "operationId", "createUser"
        ));

        String result = promptBuilderService.buildPrompt(List.of(firstEndpoint, secondEndpoint));

        assertThat(result)
                .contains("--- Endpoint 1 ---")
                .contains("--- Endpoint 2 ---")
                .contains("/api/v1/users")
                .contains("GET")
                .contains("POST")
                .contains("findUsers")
                .contains("createUser");
    }

    @Test
    void buildPromptShouldThrowExceptionWhenEndpointsIsNull() {
        String expectedMessage = "endpoints is null or empty";
        PromptBuildingException exception = Assertions.assertThrows(PromptBuildingException.class,
                () -> promptBuilderService.buildPrompt(null));

        assertThat(exception.getMessage()).isSameAs(expectedMessage);
    }

    @Test
    void buildPromptShouldThrowExceptionWhenEndpointsIsEmpty() {
        String expectedMessage = "endpoints is null or empty";
        PromptBuildingException exception = Assertions.assertThrows(PromptBuildingException.class,
                () -> promptBuilderService.buildPrompt(List.of()));

        assertThat(exception.getMessage()).isSameAs(expectedMessage);
    }

    @Test
    void getSystemPromptShouldReturnSystemPrompt() {
        String result = promptBuilderService.getSystemPrompt();

        assertThat(result)
                .isNotBlank()
                .contains("Ты генератор тест-кейсов")
                .contains("ТОЛЬКО JSON-массив")
                .contains("assertions всегда массив строк")
                .contains("возвращай результат строго в виде JSON");
    }

    @Test
    void buildPromptShouldThrowPromptBuildingExceptionWhenJsonSerializationFails() throws JsonProcessingException {
        PromptBuilderService service = new PromptBuilderService();

        ObjectMapper objectMapper = mock(ObjectMapper.class);
        ObjectWriter objectWriter = mock(ObjectWriter.class);

        EndpointInfo endpoint = endpoint(Map.of(
                "path", "/api/v1/users",
                "method", "GET"
        ));

        List<EndpointInfo> endpoints = List.of(endpoint);

        when(objectMapper.writerWithDefaultPrettyPrinter()).thenReturn(objectWriter);
        when(objectWriter.writeValueAsString(endpoint))
                .thenThrow(new JsonProcessingException("serialization failed") {});

        ReflectionTestUtils.setField(service, "objectMapper", objectMapper);

        assertThatThrownBy(() -> service.buildPrompt(endpoints))
                .isInstanceOf(PromptBuildingException.class)
                .hasMessageContaining("Error building prompt")
                .hasMessageContaining("serialization failed");
    }

    private static EndpointInfo endpoint(Map<String, Object> values) {
        return new ObjectMapper().convertValue(values, EndpointInfo.class);
    }
}