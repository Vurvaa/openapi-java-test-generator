package org.example.openapitestgenerator.service;

import org.example.openapitestgenerator.dto.config.Config;
import org.example.openapitestgenerator.dto.llmrequest.MessageToLlm;
import org.example.openapitestgenerator.dto.llmresponse.LlmResponse;
import org.example.openapitestgenerator.dto.testcase.TestCase;
import org.example.openapitestgenerator.infrastructure.LlmClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class SeleniumTestsGeneratorService
{
    private static final Logger log = LoggerFactory.getLogger(SeleniumTestsGeneratorService.class);
    private final TestCaseParserService testCaseParserService;
    private final LlmClient llmClient;
    private final Config config;
    private final LlmResponseParserService llmResponseParserService;
    private final ObjectMapper mapper = new ObjectMapper();

    SeleniumTestsGeneratorService(TestCaseParserService testCaseParserService, LlmClient llmClient, Config config, LlmResponseParserService llmResponseParserService)
    {
        this.testCaseParserService = testCaseParserService;
        this.llmClient = llmClient;
        this.config = config;
        this.llmResponseParserService = llmResponseParserService;
    }

    public String generateTests(String testCasesFilePath)
    {
        InputStream file;

        try
        {
            file = new ClassPathResource(testCasesFilePath).getInputStream();
        }
        catch (IOException e)
        {
            log.error("Error reading or converting test cases", e);
            throw new RuntimeException("Error reading or converting test cases", e);
        }

        List<TestCase> testCases = testCaseParserService.testCasesDeserialization(file);
        String testCasesJson = mapper.writeValueAsString(testCases);

        String systemPrompt = config.prompts().seleniumPrompt();

        String userPrompt = """
                Generate the JUnit 5 test class using SwaggerTestTemplate for the following JSON configuration:
                
                %s
                """.formatted(testCasesJson);

        List<MessageToLlm> messages = List.of(new MessageToLlm("system", systemPrompt), new MessageToLlm("user", userPrompt));
        String rawLlmResponse = llmClient.generateResponse(messages);
        LlmResponse llmResponse = mapper.readValue(rawLlmResponse, LlmResponse.class);
        String processedResponse = llmResponseParserService.parse(llmResponse);
        log.info("Response from the LLM received and processed.");
        log.debug("Response from the LLM: {} ", processedResponse);
        return processedResponse;
    }

    public void saveTests(String llmResponse, String outputPath)
    {
        Path path = Path.of(outputPath);

        try
        {
            Path parentDir = path.getParent();

            if (parentDir != null)
            {
                Files.createDirectories(parentDir);
            }

            Files.writeString(path, llmResponse);

            log.info("The test file has been saved.");
        }
        catch (IOException e)
        {
            log.error("Failed to save the file", e);
            throw new RuntimeException("Failed to save the file", e);
        }
    }

    public void runTests()
    {
        try
        {
            new ProcessBuilder("cmd.exe", "/c", "mvnw.cmd test -Dtest=org.example.openapitestgenerator.selenium.GeneratedTests")
                    .inheritIO()
                    .start();
        } catch (IOException e)
        {
            System.out.println("Не удалось запустить: " + e.getMessage());
        }
    }
}
