package org.example.openapitestgenerator.cli;

import org.example.openapitestgenerator.service.multithreading.ParallelExecutorService;
import org.example.openapitestgenerator.service.loader.LoaderService;
import org.example.openapitestgenerator.service.parser.ParserService;
import org.example.openapitestgenerator.service.postman.PostmanCollectionGenerator;
import org.example.openapitestgenerator.service.llmresponse.LlmResponseSaverService;
import org.example.openapitestgenerator.service.selenium.SeleniumTestsGeneratorService;
import org.example.openapitestgenerator.service.testcases.TestCasesGeneratorService;
import org.example.openapitestgenerator.service.testcases.TestCasesPromptBuilderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CliTest {

    @Mock
    private LoaderService loader;

    @Mock
    private ParserService parser;

    @Mock
    private SeleniumTestsGeneratorService seleniumTestsGenerator;

    @Mock
    private TestCasesPromptBuilderService promptBuilder;

    @Mock
    private TestCasesGeneratorService testCasesGenerator;

    @Mock
    private PostmanCollectionGenerator postmanGenerator;

    @Mock
    private LlmResponseSaverService saver;

    @Mock
    private ParallelExecutorService executor;

    @InjectMocks
    private Cli cli;

    private PrintStream originalOut;
    private ByteArrayOutputStream output;

    @BeforeEach
    void setUp() {
        originalOut = System.out;
        output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void runShouldLoadOpenApiAndExitWhenChoiceIs4() {
        when(loader.load(anyString())).thenReturn("raw openapi");

        setInput("4\n");

        cli.run();

        verify(loader).load(
                "https://api.apis.guru/v2/specs/httpbin.org/0.9.2/openapi.json"
        );

        verifyNoInteractions(parser);
        verifyNoInteractions(promptBuilder);
        verifyNoInteractions(testCasesGenerator);
        verifyNoInteractions(seleniumTestsGenerator);
        verifyNoInteractions(postmanGenerator);
        verifyNoInteractions(saver);
        verifyNoInteractions(executor);

        assertTrue(output.toString().contains("Завершение работы"));
    }

    @Test
    void runShouldPrintInvalidInputAndContinue() {
        when(loader.load(anyString())).thenReturn("raw openapi");

        setInput("wrong\n4\n");

        cli.run();

        assertTrue(output.toString().contains("Неверный ввод"));
        assertTrue(output.toString().contains("Завершение работы"));
    }

    @Test
    void runShouldGenerateTestCasesInSingleThreadModeWhenChoiceIs1() {
        when(loader.load(anyString())).thenReturn("raw openapi");
        when(parser.parseOpenApiSpec("raw openapi")).thenReturn(List.of());
        when(promptBuilder.buildPrompt(List.of())).thenReturn("prompt");
        when(testCasesGenerator.generateTestCases("prompt"))
                .thenReturn(List.of());
        when(testCasesGenerator.testCaseToString(List.of()))
                .thenReturn("generated test cases");

        setInput("1\n1\n4\n");

        cli.run();

        verify(parser).parseOpenApiSpec("raw openapi");
        verify(promptBuilder).buildPrompt(List.of());
        verify(testCasesGenerator).generateTestCases("prompt");
        verify(testCasesGenerator).testCaseToString(List.of());
        verify(saver).save(
                "generated test cases",
                "src/main/resources/testcases.json"
        );

        verifyNoInteractions(executor);

        assertTrue(output.toString().contains("Тест-кейсы сгенерированы"));
    }

    @Test
    void testcasesShouldGenerateTestCasesInParallelMode() {
        cli.rawOpenApi = "raw openapi";

        when(parser.parseOpenApiSpec("raw openapi")).thenReturn(List.of());
        when(executor.generateParallel(any(), eq(2), any()))
                .thenReturn(List.of());
        when(testCasesGenerator.testCaseToString(List.of()))
                .thenReturn("generated test cases");

        setInput("2\n");

        cli.testcases();

        verify(parser).parseOpenApiSpec("raw openapi");
        verify(executor).generateParallel(any(), eq(2), any());
        verify(testCasesGenerator).testCaseToString(List.of());
        verify(saver).save(
                "generated test cases",
                "src/main/resources/testcases.json"
        );

        verifyNoInteractions(promptBuilder);

        assertTrue(output.toString().contains("Тест-кейсы сгенерированы"));
    }

    @Test
    void testcasesShouldPrintInvalidInputAndNotGenerateTestCases() {
        cli.rawOpenApi = "raw openapi";

        when(parser.parseOpenApiSpec("raw openapi")).thenReturn(List.of());

        setInput("wrong\n");

        cli.testcases();

        verify(parser).parseOpenApiSpec("raw openapi");
        verifyNoInteractions(promptBuilder);
        verifyNoInteractions(executor);
        verifyNoInteractions(saver);

        verify(testCasesGenerator, never())
                .generateTestCases(anyString());
        verify(testCasesGenerator, never())
                .testCaseToString(any());

        assertTrue(output.toString().contains("Неверный ввод"));
    }

    @Test
    void seleniumShouldGenerateAndSaveTestsWithoutRun() {
        when(seleniumTestsGenerator.generateTests("testcases.json"))
                .thenReturn("generated tests");

        setInput("2\n");

        cli.selenium();

        verify(seleniumTestsGenerator).generateTests("testcases.json");
        verify(saver).save(
                "generated tests",
                "src/test/java/org/example/openapitestgenerator/"
                        + "selenium/GeneratedTests.java"
        );
        verify(seleniumTestsGenerator, never()).runTests();

        assertTrue(output.toString().contains("Тесты сгенерированы"));
    }

    @Test
    void seleniumShouldGenerateSaveAndRunTests() {
        when(seleniumTestsGenerator.generateTests("testcases.json"))
                .thenReturn("generated tests");

        setInput("1\n");

        cli.selenium();

        verify(seleniumTestsGenerator).generateTests("testcases.json");
        verify(saver).save(
                "generated tests",
                "src/test/java/org/example/openapitestgenerator/"
                        + "selenium/GeneratedTests.java"
        );
        verify(seleniumTestsGenerator).runTests();

        assertTrue(output.toString().contains("Запуск тестов"));
    }

    @Test
    void postmanShouldGenerateAndSaveCollectionWithoutRun() {
        when(testCasesGenerator.testCasesDeserialization())
                .thenReturn(List.of());
        when(postmanGenerator.generate(any()))
                .thenReturn("collection");
        when(postmanGenerator.generateParams("collection"))
                .thenReturn("collection with params");

        setInput("2\n");

        cli.postman();

        verify(testCasesGenerator).testCasesDeserialization();
        verify(postmanGenerator).generate(any());
        verify(postmanGenerator).generateParams("collection");
        verify(saver).save(
                "collection with params",
                "src/main/resources/collection.json"
        );
        verify(postmanGenerator, never()).startNewman();

        assertTrue(output.toString().contains("Коллекция сгенерирована"));
    }

    @Test
    void postmanShouldGenerateSaveCollectionAndRunNewman() {
        when(testCasesGenerator.testCasesDeserialization())
                .thenReturn(List.of());
        when(postmanGenerator.generate(any()))
                .thenReturn("collection");
        when(postmanGenerator.generateParams("collection"))
                .thenReturn("collection with params");

        setInput("1\n");

        cli.postman();

        verify(testCasesGenerator).testCasesDeserialization();
        verify(postmanGenerator).generate(any());
        verify(postmanGenerator).generateParams("collection");
        verify(saver).save(
                "collection with params",
                "src/main/resources/collection.json"
        );
        verify(postmanGenerator).startNewman();

        assertTrue(output.toString().contains("Запуск тестов"));
    }

    private void setInput(String input) {
        cli.scanner = new Scanner(
                new ByteArrayInputStream(input.getBytes())
        );
    }
}
