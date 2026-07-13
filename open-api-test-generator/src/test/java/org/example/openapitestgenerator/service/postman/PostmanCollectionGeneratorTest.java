package org.example.openapitestgenerator.service.postman;

import org.example.openapitestgenerator.dto.testcase.InputData;
import tools.jackson.databind.ObjectMapper;
import org.example.openapitestgenerator.dto.config.Config;
import org.example.openapitestgenerator.dto.llmrequest.MessageToLlm;
import org.example.openapitestgenerator.dto.llmresponse.LlmResponse;
import org.example.openapitestgenerator.dto.postman.PostmanCollection;
import org.example.openapitestgenerator.dto.testcase.TestCase;
import org.example.openapitestgenerator.dto.testcase.Expected;
import org.example.openapitestgenerator.dto.testcase.Target;
import org.example.openapitestgenerator.dto.config.Prompts;
import org.example.openapitestgenerator.infrastructure.LlmClient;
import org.example.openapitestgenerator.service.llmresponse.LlmResponseParserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostmanCollectionGeneratorTest {

    @Mock
    private PostmanScriptGenerator scriptGenerator;

    @Mock
    private LlmClient llmClient;

    @Mock
    private Config config;

    @Mock
    private Prompts prompts;

    @Mock
    private LlmResponseParserService llmResponseParserService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private TestCase testCase;

    @Mock
    private Target target;

    @Mock
    private Expected expected;

    @Test
    void generateShouldCreateCollectionForGetRequestWithoutBody() {
        PostmanCollectionGenerator generator = generator();

        when(testCase.name()).thenReturn("Get user");
        when(testCase.target()).thenReturn(target);
        when(testCase.expected()).thenReturn(expected);

        when(target.method()).thenReturn("GET");
        when(target.path()).thenReturn("/users/1");

        when(scriptGenerator.generate(expected)).thenReturn(List.of());
        when(objectMapper.writeValueAsString(any(PostmanCollection.class)))
                .thenReturn("generated collection");

        String result = generator.generate(List.of(testCase));

        assertThat(result).isEqualTo("generated collection");

        ArgumentCaptor<PostmanCollection> collectionCaptor =
                ArgumentCaptor.forClass(PostmanCollection.class);

        verify(objectMapper).writeValueAsString(collectionCaptor.capture());
        verify(scriptGenerator).generate(expected);
        verify(objectMapper, never()).writeValueAsString(testCase.inputData());

        PostmanCollection collection = collectionCaptor.getValue();

        assertThat(collection.info().name())
                .isEqualTo("Generated API Tests");
        assertThat(collection.info().schema())
                .isEqualTo(
                        "https://schema.getpostman.com/json/"
                                + "collection/v2.1.0/collection.json"
                );

        assertThat(collection.item()).hasSize(1);

        var item = collection.item().getFirst();

        assertThat(item.name()).isEqualTo("Get user");
        assertThat(item.request().method()).isEqualTo("GET");
        assertThat(item.request().header()).isEmpty();
        assertThat(item.request().body()).isNull();

        assertThat(item.request().url().raw())
                .isEqualTo("{{baseUrl}}/users/1");
        assertThat(item.request().url().host())
                .containsExactly("{{baseUrl}}");
        assertThat(item.request().url().path())
                .containsExactly("users", "1");
    }

    @Test
    void generateShouldCreatePostRequestWithJsonBodyAndHeader() {
        PostmanCollectionGenerator generator = generator();

        InputData inputData = new InputData();
        inputData.add("name", "John");
        inputData.add("age", 20);

        when(testCase.name()).thenReturn("Create user");
        when(testCase.target()).thenReturn(target);
        when(testCase.expected()).thenReturn(expected);
        when(testCase.inputData()).thenReturn(inputData);

        when(target.method()).thenReturn("POST");
        when(target.path()).thenReturn("/users");

        when(objectMapper.writeValueAsString(inputData))
                .thenReturn("""
                        {"name":"John","age":20}
                        """.trim());

        when(scriptGenerator.generate(expected)).thenReturn(List.of());

        when(objectMapper.writeValueAsString(any(PostmanCollection.class)))
                .thenReturn("generated collection");

        String result = generator.generate(List.of(testCase));

        assertThat(result).isEqualTo("generated collection");

        ArgumentCaptor<PostmanCollection> collectionCaptor =
                ArgumentCaptor.forClass(PostmanCollection.class);

        verify(objectMapper).writeValueAsString(inputData);
        verify(objectMapper).writeValueAsString(collectionCaptor.capture());

        PostmanCollection collection = collectionCaptor.getValue();
        var request = collection.item().getFirst().request();

        assertThat(request.method()).isEqualTo("POST");

        assertThat(request.header()).hasSize(1);
        assertThat(request.header().getFirst().key())
                .isEqualTo("Content-Type");
        assertThat(request.header().getFirst().value())
                .isEqualTo("application/json");

        assertThat(request.body()).isNotNull();
        assertThat(request.body().mode()).isEqualTo("raw");
        assertThat(request.body().raw())
                .isEqualTo("""
                        {"name":"John","age":20}
                        """.trim());
    }

    @Test
    void generateShouldNotCreateBodyWhenInputDataIsNull() {
        PostmanCollectionGenerator generator = generator();

        when(testCase.name()).thenReturn("Create user");
        when(testCase.target()).thenReturn(target);
        when(testCase.expected()).thenReturn(expected);
        when(testCase.inputData()).thenReturn(null);

        when(target.method()).thenReturn("POST");
        when(target.path()).thenReturn("/users");

        when(scriptGenerator.generate(expected)).thenReturn(List.of());
        when(objectMapper.writeValueAsString(any(PostmanCollection.class)))
                .thenReturn("generated collection");

        generator.generate(List.of(testCase));

        ArgumentCaptor<PostmanCollection> collectionCaptor =
                ArgumentCaptor.forClass(PostmanCollection.class);

        verify(objectMapper).writeValueAsString(collectionCaptor.capture());

        var request = collectionCaptor
                .getValue()
                .item()
                .getFirst()
                .request();

        assertThat(request.body()).isNull();
        assertThat(request.header()).isEmpty();
    }

    @Test
    void generateShouldSupportPatchMethodIgnoringCase() {
        PostmanCollectionGenerator generator = generator();

        InputData inputData = new InputData();
        inputData.add("id", 1);

        when(testCase.name()).thenReturn("Update user");
        when(testCase.target()).thenReturn(target);
        when(testCase.expected()).thenReturn(expected);
        when(testCase.inputData()).thenReturn(inputData);

        when(target.method()).thenReturn("patch");
        when(target.path()).thenReturn("/users/1");

        when(objectMapper.writeValueAsString(inputData))
                .thenReturn("""
                        {"name":"Updated"}
                        """.trim());

        when(scriptGenerator.generate(expected)).thenReturn(List.of());
        when(objectMapper.writeValueAsString(any(PostmanCollection.class)))
                .thenReturn("generated collection");

        generator.generate(List.of(testCase));

        ArgumentCaptor<PostmanCollection> collectionCaptor =
                ArgumentCaptor.forClass(PostmanCollection.class);

        verify(objectMapper).writeValueAsString(collectionCaptor.capture());

        var request = collectionCaptor
                .getValue()
                .item()
                .getFirst()
                .request();

        assertThat(request.body()).isNotNull();
        assertThat(request.header()).hasSize(1);
    }

    @Test
    void generateShouldCreateEmptyCollectionWhenTestCasesAreEmpty() {
        PostmanCollectionGenerator generator = generator();

        when(objectMapper.writeValueAsString(any(PostmanCollection.class)))
                .thenReturn("empty collection");

        String result = generator.generate(List.of());

        assertThat(result).isEqualTo("empty collection");

        ArgumentCaptor<PostmanCollection> collectionCaptor =
                ArgumentCaptor.forClass(PostmanCollection.class);

        verify(objectMapper).writeValueAsString(collectionCaptor.capture());

        assertThat(collectionCaptor.getValue().item()).isEmpty();
        verify(scriptGenerator, never()).generate(any());
    }

    @Test
    void generateParamsShouldSendPromptsAndReturnParsedResponse() {
        PostmanCollectionGenerator generator = generator();

        String collection = """
                {
                  "item": [
                    {
                      "url": "/users/{userId}"
                    }
                  ]
                }
                """;

        String rawLlmResponse = """
                {
                  "choices": []
                }
                """;

        LlmResponse llmResponse = mock(LlmResponse.class);

        when(config.prompts()).thenReturn(prompts);
        when(prompts.postmanDataFixerPrompt())
                .thenReturn("system prompt");

        when(llmClient.generateResponse(any()))
                .thenReturn(rawLlmResponse);

        when(objectMapper.readValue(rawLlmResponse, LlmResponse.class))
                .thenReturn(llmResponse);

        when(llmResponseParserService.parse(llmResponse))
                .thenReturn("fixed collection");

        String result = generator.generateParams(collection);

        assertThat(result).isEqualTo("fixed collection");

        ArgumentCaptor<List<MessageToLlm>> messagesCaptor =
                ArgumentCaptor.forClass(List.class);

        verify(llmClient).generateResponse(messagesCaptor.capture());
        verify(objectMapper)
                .readValue(rawLlmResponse, LlmResponse.class);
        verify(llmResponseParserService).parse(llmResponse);

        List<MessageToLlm> messages = messagesCaptor.getValue();

        assertThat(messages).hasSize(2);

        assertThat(messages.get(0).role()).isEqualTo("system");
        assertThat(messages.get(0).content())
                .isEqualTo("system prompt");

        assertThat(messages.get(1).role()).isEqualTo("user");
        assertThat(messages.get(1).content())
                .contains(collection);
        assertThat(messages.get(1).content())
                .contains("find all path parameters");
    }

    private PostmanCollectionGenerator generator() {
        return new PostmanCollectionGenerator(
                scriptGenerator,
                llmClient,
                config,
                llmResponseParserService,
                objectMapper
        );
    }
}
