/*
package org.example.openapitestgenerator.service;

import org.example.openapitestgenerator.dto.config.Config;
import org.example.openapitestgenerator.dto.config.Prompts;
import org.example.openapitestgenerator.dto.llmresponse.LlmResponse;
import org.example.openapitestgenerator.dto.testcase.TestCase;
import org.example.openapitestgenerator.infrastructure.LlmClient;
import org.example.openapitestgenerator.service.llmresponse.LlmResponseParserService;
import org.example.openapitestgenerator.service.selenium.SeleniumTestsGeneratorService;
import org.example.openapitestgenerator.service.testcases.TestCaseParserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SeleniumTestsGeneratorServiceTest {

    @Mock
    private TestCaseParserService testCaseParserService;
    @Mock
    private LlmClient llmClient;
    @Mock
    private Config config;
    @Mock
    private Prompts prompts;
    @Mock
    private LlmResponseParserService llmResponseParserService;

    private SeleniumTestsGeneratorService generatorService;

    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper();
        generatorService = new SeleniumTestsGeneratorService(
                testCaseParserService, llmClient, config, llmResponseParserService, mapper
        );
    }

    @Test
    void generateTestsSuccess() {
        TestCase testCase = mock(TestCase.class);
        String dummyResourcePath = "application.properties";

        List<TestCase> mockTestCases = List.of(testCase);
        String mockRawLlmResponse = "{\"choices\":[]}";
        String mockParsedResult = "public class GeneratedSeleniumTest {}";

        when(testCaseParserService.testCasesDeserialization(any(InputStream.class)))
                .thenReturn(mockTestCases);

        when(config.prompts()).thenReturn(prompts);

        when(prompts.seleniumPrompt()).thenReturn("Test text");

        when(llmClient.generateResponse(anyList()))
                .thenReturn(mockRawLlmResponse);

        when(llmResponseParserService.parse(any(LlmResponse.class)))
                .thenReturn(mockParsedResult);

        String result = generatorService.generateTests(dummyResourcePath);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(mockParsedResult, result);
        verify(llmClient).generateResponse(anyList());
    }

    @Test
    void generateTestsThrowsRuntimeExceptionWhenFileNotFound() {
        String expectedMessage = "Error reading or converting test cases";
        String nonExistentPath = "invalid/path/to/file.json";

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class,
                () -> generatorService.generateTests(nonExistentPath));

        Assertions.assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void saveTestsSuccess(@TempDir Path tempDir) throws Exception {
        String codeContent = "public class SeleniumTest {}";
        Path targetFilePath = tempDir.resolve("GeneratedTest.java");

        generatorService.saveTests(codeContent, targetFilePath.toString());

        Assertions.assertTrue(Files.exists(targetFilePath));
        String fileContent = Files.readString(targetFilePath);
        Assertions.assertEquals(codeContent, fileContent);
    }
}
*/
