package org.example.openapitestgenerator.service.selenium;

import tools.jackson.databind.ObjectMapper;
import org.example.openapitestgenerator.dto.config.Config;
import org.example.openapitestgenerator.dto.config.Prompts;
import org.example.openapitestgenerator.dto.llmrequest.MessageToLlm;
import org.example.openapitestgenerator.dto.llmresponse.LlmResponse;
import org.example.openapitestgenerator.dto.testcase.TestCase;
import org.example.openapitestgenerator.infrastructure.LlmClient;
import org.example.openapitestgenerator.service.llmresponse.LlmResponseParserService;
import org.example.openapitestgenerator.service.testcases.TestCaseParserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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

    @Mock
    private ObjectMapper mapper;

    @Test
    void generateTestsShouldThrowRuntimeExceptionWhenFileDoesNotExist() {
        SeleniumTestsGeneratorService service = service();

        assertThatThrownBy(
                () -> service.generateTests("missing-testcases.json")
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Error reading or converting test cases")
                .hasCauseInstanceOf(java.io.IOException.class);

        verifyNoInteractions(testCaseParserService);
        verifyNoInteractions(llmClient);
        verifyNoInteractions(config);
        verifyNoInteractions(llmResponseParserService);
        verifyNoInteractions(mapper);
    }

    private SeleniumTestsGeneratorService service() {
        return new SeleniumTestsGeneratorService(
                testCaseParserService,
                llmClient,
                config,
                llmResponseParserService,
                mapper
        );
    }
}
