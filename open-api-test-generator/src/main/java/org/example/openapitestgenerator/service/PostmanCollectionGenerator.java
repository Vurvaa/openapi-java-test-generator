package org.example.openapitestgenerator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.openapitestgenerator.dto.TestCase;
import org.example.openapitestgenerator.dto.postman.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostmanCollectionGenerator {

    private static final String POSTMAN_SCHEMA =
            "https://schema.getpostman.com/json/collection/v2.1.0/collection.json";
    private static final String COLLECTION_NAME = "Generated API Tests";
    private static final String BASE_URL_VARIABLE = "{{baseUrl}}";

    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String RAW_BODY_MODE = "raw";

    private final ObjectMapper objectMapper;

    public PostmanCollectionGenerator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public PostmanCollection generate(List<TestCase> testCases) {
        Info info = new Info(COLLECTION_NAME, POSTMAN_SCHEMA);

        List<Item> items = testCases.stream()
                .map(this::toItem)
                .toList();

        return new PostmanCollection(info, items);
    }

    private Item toItem(TestCase testCase) {
        Request request = new Request(
                testCase.target().method(),
                defaultJsonHeaders(),
                toBody(testCase),
                new Url(BASE_URL_VARIABLE + testCase.target().path())
        );

        return new Item(testCase.name(), request);
    }

    private List<Header> defaultJsonHeaders() {
        return List.of(new Header(CONTENT_TYPE_HEADER, APPLICATION_JSON));
    }

    private Body toBody(TestCase testCase) {
        if (testCase.inputData() == null) {
            return null;
        }

        try {
            String rawJson = objectMapper.writeValueAsString(testCase.inputData());
            return new Body(RAW_BODY_MODE, rawJson);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Не удалось сериализовать inputData в JSON", e);
        }
    }
}