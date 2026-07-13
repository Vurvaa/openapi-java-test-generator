/*
package org.example.openapitestgenerator.cli;

import org.example.openapitestgenerator.service.loader.LoaderService;
import org.example.openapitestgenerator.service.parser.ParserService;
import org.example.openapitestgenerator.service.postman.PostmanCollectionGenerator;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

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

        verify(loader).load("https://api.apis.guru/v2/specs/httpbin.org/0.9.2/openapi.json");
        verifyNoInteractions(parser);
        verifyNoInteractions(promptBuilder);
        verifyNoInteractions(testCasesGenerator);
        verifyNoInteractions(seleniumTestsGenerator);
        verifyNoInteractions(postmanGenerator);
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
    void runShouldGenerateTestCasesWhenChoiceIs1() {
        when(loader.load(anyString())).thenReturn("raw openapi");
        when(parser.parseOpenApiSpec("raw openapi")).thenReturn(List.of());
        when(promptBuilder.buildPrompt(List.of())).thenReturn("prompt");

        setInput("1\n4\n");

        cli.run();

        verify(parser).parseOpenApiSpec("raw openapi");
        verify(promptBuilder).buildPrompt(List.of());
        verify(testCasesGenerator).generateTestCases("prompt");
    }

    @Test
    void seleniumShouldGenerateAndSaveTestsWithoutRun() {
        when(seleniumTestsGenerator.generateTests("testcases.json")).thenReturn("generated tests");

        setInput("2\n");

        cli.selenium();

        verify(seleniumTestsGenerator).generateTests("testcases.json");
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
        verify(seleniumTestsGenerator).runTests();

        assertTrue(output.toString().contains("Запуск тестов"));
    }

    @Test
    void postmanShouldGenerateAndSaveCollectionWithoutRun() {
        when(testCasesGenerator.testCasesDeserialization()).thenReturn(List.of());
        when(postmanGenerator.generate(any())).thenReturn("collection");
        when(postmanGenerator.generateParams("collection"))
                .thenReturn("collection with params");

        setInput("2\n");

        cli.postman();

        verify(testCasesGenerator).testCasesDeserialization();
        verify(postmanGenerator).generate(any());
        verify(postmanGenerator).generateParams("collection");
        verify(postmanGenerator, never()).startNewman();

        assertTrue(output.toString().contains("Коллекция сгенерирована"));
    }

    @Test
    void postmanShouldGenerateSaveCollectionAndRunNewman() {
        when(testCasesGenerator.testCasesDeserialization()).thenReturn(List.of());
        when(postmanGenerator.generate(any())).thenReturn("collection");
        when(postmanGenerator.generateParams("collection"))
                .thenReturn("collection with params");

        setInput("1\n");

        cli.postman();

        verify(testCasesGenerator).testCasesDeserialization();
        verify(postmanGenerator).generate(any());
        verify(postmanGenerator).generateParams("collection");
        verify(postmanGenerator).startNewman();

        assertTrue(output.toString().contains("Запуск тестов"));
    }

    private void setInput(String input) {
        cli.scanner = new Scanner(
                new ByteArrayInputStream(input.getBytes())
        );
    }

}*/
