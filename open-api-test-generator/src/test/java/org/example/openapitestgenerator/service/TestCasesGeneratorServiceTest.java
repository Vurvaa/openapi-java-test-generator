package org.example.openapitestgenerator.service;

import org.example.openapitestgenerator.dto.llmrequest.MessageToLlm;
import org.example.openapitestgenerator.dto.llmresponse.LlmResponse;
import org.example.openapitestgenerator.dto.testcase.TestCase;
import org.example.openapitestgenerator.infrastructure.LlmClient;
import org.example.openapitestgenerator.service.prompt.PromptBuilderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class TestCasesGeneratorServiceTest {

    private static final Path TEST_CASES_PATH = Path.of("src/main/resources/testcases.json");

    @Mock
    private PromptBuilderService promptBuilderService;

    @Mock
    private LlmClient llmClient;

    @Mock
    private LlmResponseParserService llmResponseParserService;

    @InjectMocks
    private TestCasesGeneratorService service;

    private boolean fileExisted;
    private String originalFileContent;

    @BeforeEach
    void setUp() throws IOException {
        fileExisted = Files.exists(TEST_CASES_PATH);

        if (fileExisted) {
            originalFileContent = Files.readString(TEST_CASES_PATH);
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        if (fileExisted) {
            Files.createDirectories(TEST_CASES_PATH.getParent());
            Files.writeString(TEST_CASES_PATH, originalFileContent);
        } else {
            Files.deleteIfExists(TEST_CASES_PATH);
        }
    }

    @Test
    void generateTestCasesShouldSendMessagesToLlm() {
        when(promptBuilderService.getSystemPrompt()).thenReturn("system prompt");
        when(llmClient.generateResponse(List.of(
                new MessageToLlm("system", "system prompt"),
                new MessageToLlm("user", "user prompt")
        ))).thenReturn("""
                {
                  "choices": [
                    {
                      "message": {
                        "content": "raw response"
                      }
                    }
                  ]
                }
                """);
        when(llmResponseParserService.parse(any(LlmResponse.class))).thenReturn("[]");

        service.generateTestCases("user prompt");

        verify(promptBuilderService).getSystemPrompt();
        verify(llmClient).generateResponse(List.of(
                new MessageToLlm("system", "system prompt"),
                new MessageToLlm("user", "user prompt")
        ));
    }

    @Test
    void generateTestCasesShouldParseLlmResponse() {
        when(promptBuilderService.getSystemPrompt()).thenReturn("system prompt");
        when(llmClient.generateResponse(any())).thenReturn("""
                {
                  "choices": [
                    {
                      "message": {
                        "content": "raw response"
                      }
                    }
                  ]
                }
                """);
        when(llmResponseParserService.parse(any(LlmResponse.class))).thenReturn("[]");

        service.generateTestCases("user prompt");

        verify(llmResponseParserService).parse(any(LlmResponse.class));
    }

    @Test
    void generateTestCasesShouldSaveTestCasesToFile() throws IOException {
        when(promptBuilderService.getSystemPrompt()).thenReturn("system prompt");
        when(llmClient.generateResponse(any()))
                .thenReturn("""
                {
                  "choices": [
                    {
                      "message": {
                        "content": "raw response"
                      }
                    }
                  ]
                }
                """);
        when(llmResponseParserService.parse(any(LlmResponse.class)))
                .thenReturn("[]");

        service.generateTestCases("user prompt");

        assertTrue(Files.exists(TEST_CASES_PATH));
        assertEquals("[]", Files.readString(TEST_CASES_PATH));
    }

    @Test
    void testCasesDeserializationShouldReadTestCasesFromFile() throws IOException {
        Files.createDirectories(TEST_CASES_PATH.getParent());
        Files.writeString(TEST_CASES_PATH, "[]");

        List<TestCase> testCases = service.testCasesDeserialization();

        assertTrue(testCases.isEmpty());
    }

}