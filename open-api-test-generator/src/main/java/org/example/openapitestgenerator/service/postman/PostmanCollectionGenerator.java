package org.example.openapitestgenerator.service.postman;

import org.example.openapitestgenerator.service.llmresponse.LlmResponseParserService;
import tools.jackson.databind.ObjectMapper;
import org.example.openapitestgenerator.dto.config.Config;
import org.example.openapitestgenerator.dto.llmrequest.MessageToLlm;
import org.example.openapitestgenerator.dto.llmresponse.LlmResponse;
import org.example.openapitestgenerator.dto.testcase.TestCase;
import org.example.openapitestgenerator.dto.postman.*;
import org.example.openapitestgenerator.infrastructure.LlmClient;
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
    private final PostmanScriptGenerator scriptGenerator;
    private final LlmClient llmClient;
    private final Config config;
    private final LlmResponseParserService llmResponseParserService;

    public PostmanCollectionGenerator(PostmanScriptGenerator scriptGenerator, LlmClient llmClient,
                                      Config config, LlmResponseParserService llmResponseParserService,
                                      ObjectMapper objectMapper) {
        this.scriptGenerator = scriptGenerator;
        this.llmClient = llmClient;
        this.config = config;
        this.llmResponseParserService = llmResponseParserService;
        this.objectMapper = objectMapper;
    }

    public String generate(List<TestCase> testCases) {
        Info info = new Info(COLLECTION_NAME, POSTMAN_SCHEMA);

        List<Item> items = testCases.stream()
                .map(this::toItem)
                .toList();

        PostmanCollection collection = new PostmanCollection(info, items);
        return objectMapper.writeValueAsString(collection);
    }

    private Item toItem(TestCase testCase) {
        Body body = toBody(testCase);

        Request request = new Request(
                testCase.target().method(),
                body == null ? List.of() : defaultJsonHeaders(),
                body,
                toUrl(testCase.target().path())
        );

        return new Item(
                testCase.name(),
                request,
                scriptGenerator.generate(testCase.expected())
        );
    }

    private List<Header> defaultJsonHeaders() {
        return List.of(new Header(CONTENT_TYPE_HEADER, APPLICATION_JSON));
    }

    private Body toBody(TestCase testCase) {
        if (!supportsBody(testCase.target().method()) || testCase.inputData() == null) {
            return null;
        }

        String rawJson = objectMapper.writeValueAsString(testCase.inputData());
        return new Body(RAW_BODY_MODE, rawJson);
    }

    private Url toUrl(String path) {
        String fullUrl = BASE_URL_VARIABLE + path;

        List<String> pathParts = List.of(
                path.replaceFirst("^/", "").split("/")
        );

        return new Url(
                fullUrl,
                List.of("{{baseUrl}}"),
                pathParts
        );
    }

    private boolean supportsBody(String method) {
        return List.of("POST", "PUT", "PATCH")
                .contains(method.toUpperCase());
    }

    public void startNewman()
    {
        try
        {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("cmd.exe", "/c", "newman", "run", "src/main/resources/collection.json", "--global-var", "baseUrl=http://localhost:80", "--ignore-redirects");
            processBuilder.inheritIO();
            processBuilder.start();
        }
        catch (Exception e)
        {
            System.out.println("Не удалось запустить Newman");
        }
    }

    public String generateParams(String collection)
    {
        String userPrompt = """
                Here is the raw Postman collection JSON file generated from the OpenAPI spec.\s
                Please find all path parameters inside curly braces and replace them with valid test data according to the rules:
                
                %s
                """.formatted(collection);

        String systemPrompt = config.prompts().postmanDataFixerPrompt();
        List<MessageToLlm> messages = List.of(new MessageToLlm("system", systemPrompt), new MessageToLlm("user", userPrompt));
        String rawLlmResponse = llmClient.generateResponse(messages);
        LlmResponse llmResponse;
        llmResponse = objectMapper.readValue(rawLlmResponse, LlmResponse.class);
        return llmResponseParserService.parse(llmResponse);
    }
}