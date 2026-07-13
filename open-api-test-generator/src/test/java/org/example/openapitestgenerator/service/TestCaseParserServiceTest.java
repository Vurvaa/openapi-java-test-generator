package org.example.openapitestgenerator.service;

import org.example.openapitestgenerator.dto.testcase.TestCase;
import org.example.openapitestgenerator.service.testcases.TestCaseParserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

class TestCaseParserServiceTest {

    private TestCaseParserService parserService;

    @BeforeEach
    void setUp() {
        parserService = new TestCaseParserService();
    }

    @Test
    void testCasesDeserialization_Success() {
        String validJson = "[{\"id\": 1, \"name\": \"Test Case 1\"}]";
        InputStream inputStream = new ByteArrayInputStream(validJson.getBytes());

        List<TestCase> result = parserService.testCasesDeserialization(inputStream);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
    }

    @Test
    void testCasesDeserializationReturnsEmptyListWhenInputStreamIsNull() {
        List<TestCase> result = parserService.testCasesDeserialization(null);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void testCasesDeserializationReturnsEmptyListWhenJsonIsInvalid() {
        String invalidJson = "{ invalid json sequence }";
        InputStream inputStream = new ByteArrayInputStream(invalidJson.getBytes());

        List<TestCase> result = parserService.testCasesDeserialization(inputStream);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }
}