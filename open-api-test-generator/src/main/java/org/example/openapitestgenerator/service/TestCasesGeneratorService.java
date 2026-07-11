package org.example.openapitestgenerator.service;

import org.example.openapitestgenerator.dto.llmrequest.MessageToLlm;
import org.example.openapitestgenerator.dto.llmresponse.LlmResponse;
import org.example.openapitestgenerator.dto.testcase.TestCase;
import org.example.openapitestgenerator.infrastructure.LlmClient;
import org.example.openapitestgenerator.service.prompt.PromptBuilderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

@Service
public class TestCasesGeneratorService
{
    private final PromptBuilderService promptBuilderService;
    private final LlmClient llmClient;
    private static final Logger log = LoggerFactory.getLogger(TestCasesGeneratorService.class);
    private final ObjectMapper mapper;
    private final LlmResponseParserService llmResponseParserService;

    TestCasesGeneratorService(PromptBuilderService promptBuilderService, LlmClient llmClient,
                              LlmResponseParserService llmResponseParserService, ObjectMapper mapper)
    {
        this.promptBuilderService = promptBuilderService;
        this.llmClient = llmClient;
        this.llmResponseParserService = llmResponseParserService;
        this.mapper = mapper;
    }

    public void generateTestCases(String userPrompt)
    {
        String systemPrompt = promptBuilderService.getSystemPrompt();
        List<MessageToLlm> messages = List.of(new MessageToLlm("system", systemPrompt), new MessageToLlm("user", userPrompt));
        String rawLlmResponse = llmClient.generateResponse(messages);
        LlmResponse llmResponse = mapper.readValue(rawLlmResponse, LlmResponse.class);
        String processedResponse = llmResponseParserService.parse(llmResponse);
        List<TestCase> deserializationTestCases = mapper.readValue(processedResponse, new TypeReference<>() {});
        String testCases = mapper.writeValueAsString(deserializationTestCases);
        log.info("Response from the LLM received and processed.");
        log.debug("Response from the LLM: {} ", processedResponse);
        saveTestCases(testCases);
    }

    private void saveTestCases(String testCases)
    {

        Path path = Path.of("src/main/resources/testcases.json");

        try
        {
            Path parentDir = path.getParent();

            if (parentDir != null)
            {
                Files.createDirectories(parentDir);
            }

            Files.writeString(
                    path,
                    testCases,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.SYNC);

            log.info("The test file has been saved.");
        } catch (IOException e)
        {
            log.error("Failed to save the file", e);
            throw new RuntimeException("Failed to save the file", e);
        }
    }

    public List<TestCase> testCasesDeserialization()
    {
        return mapper.readValue(Path.of("src/main/resources/testcases.json").toFile(), new TypeReference<>() {});
    }
}
