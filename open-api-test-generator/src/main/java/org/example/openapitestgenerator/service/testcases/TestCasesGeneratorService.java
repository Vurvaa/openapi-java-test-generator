package org.example.openapitestgenerator.service.testcases;

import org.example.openapitestgenerator.dto.endpoints.EndpointInfo;
import org.example.openapitestgenerator.dto.llmrequest.MessageToLlm;
import org.example.openapitestgenerator.dto.llmresponse.LlmResponse;
import org.example.openapitestgenerator.dto.testcase.TestCase;
import org.example.openapitestgenerator.infrastructure.LlmClient;
import org.example.openapitestgenerator.service.llmresponse.LlmResponseParserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.util.List;

@Service
public class TestCasesGeneratorService
{
    private final TestCasesPromptBuilderService promptBuilderService;
    private final LlmClient llmClient;
    private static final Logger log = LoggerFactory.getLogger(TestCasesGeneratorService.class);
    private final ObjectMapper mapper;
    private final LlmResponseParserService llmResponseParserService;
    private final TestCasesPromptBuilderService promptBuilder;

    TestCasesGeneratorService(TestCasesPromptBuilderService promptBuilderService, LlmClient llmClient, LlmResponseParserService llmResponseParserService, ObjectMapper mapper, TestCasesPromptBuilderService promptBuilder)
    {
        this.promptBuilderService = promptBuilderService;
        this.llmClient = llmClient;
        this.llmResponseParserService = llmResponseParserService;
        this.mapper = mapper;
        this.promptBuilder = promptBuilder;
    }

    public List<TestCase> generateTestCases(String userPrompt)
    {
        String systemPrompt = promptBuilderService.getSystemPrompt();
        List<MessageToLlm> messages = List.of(new MessageToLlm("system", systemPrompt), new MessageToLlm("user", userPrompt));
        String rawLlmResponse = llmClient.generateResponse(messages);
        LlmResponse llmResponse = mapper.readValue(rawLlmResponse, LlmResponse.class);
        String processedResponse = llmResponseParserService.parse(llmResponse);
        log.info("Generation successfully completed");
        return mapper.readValue(processedResponse, new TypeReference<>() {});
    }

    public List<TestCase> testCasesDeserialization()
    {
        return mapper.readValue(Path.of("src/main/resources/testcases.json").toFile(), new TypeReference<>() {});
    }

    public List<TestCase> processor(List<EndpointInfo> chunk)
    {
        String userPrompt = promptBuilder.buildPrompt(chunk);
        return generateTestCases(userPrompt);
    }

    public String testCaseToString(List<TestCase> testCaseList)
    {
        return mapper.writeValueAsString(testCaseList);
    }
}
