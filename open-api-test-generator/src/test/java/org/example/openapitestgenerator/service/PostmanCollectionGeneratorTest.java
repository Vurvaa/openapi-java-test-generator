package org.example.openapitestgenerator.service;

import org.example.openapitestgenerator.dto.config.Config;
import org.example.openapitestgenerator.dto.testcase.Expected;
import org.example.openapitestgenerator.dto.testcase.TestCase;
import org.example.openapitestgenerator.infrastructure.LlmClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostmanCollectionGeneratorTest {

    @Mock
    private PostmanScriptGenerator scriptGenerator;

    @Mock
    private LlmClient llmClient;

    @Mock(answer = RETURNS_DEEP_STUBS)
    private Config config;

    @Mock
    private LlmResponseParserService llmResponseParserService;

    @InjectMocks
    private PostmanCollectionGenerator generator;

    @TempDir
    private Path tempDir;

    @Test
    void generateShouldCreatePostmanCollection() {
        TestCase testCase = mockGetTestCase();

        when(scriptGenerator.generate(testCase.expected())).thenReturn(List.of());

        String collection = generator.generate(List.of(testCase));

        assertTrue(collection.contains("Generated API Tests"));
        assertTrue(collection.contains("https://schema.getpostman.com/json/collection/v2.1.0/collection.json"));
        assertTrue(collection.contains("Get user"));
        assertTrue(collection.contains("GET"));
        assertTrue(collection.contains("{{baseUrl}}/users/1"));

        verify(scriptGenerator).generate(testCase.expected());
    }

    @Test
    void saveShouldWriteCollectionToFile() throws IOException {
        Path outputPath = tempDir.resolve("collection.json");

        generator.save("{\"info\":\"test\"}", outputPath.toString());

        assertTrue(Files.exists(outputPath));
        assertEquals("{\"info\":\"test\"}", Files.readString(outputPath));
    }


    private TestCase mockGetTestCase() {
        TestCase testCase = mock(
                TestCase.class,
                RETURNS_DEEP_STUBS
        );

        Expected expected = new Expected(200, "test", List.of());

        when(testCase.name()).thenReturn("Get user");
        when(testCase.target().method()).thenReturn("GET");
        when(testCase.target().path()).thenReturn("/users/1");
        when(testCase.inputData()).thenReturn(null);
        when(testCase.expected()).thenReturn(expected);

        return testCase;
    }

}