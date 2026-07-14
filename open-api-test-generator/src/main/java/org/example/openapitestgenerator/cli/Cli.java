package org.example.openapitestgenerator.cli;

import org.example.openapitestgenerator.dto.endpoints.EndpointInfo;
import org.example.openapitestgenerator.dto.testcase.TestCase;
import org.example.openapitestgenerator.service.multithreading.ParallelExecutorService;
import org.example.openapitestgenerator.service.llmresponse.LlmResponseSaverService;
import org.example.openapitestgenerator.service.loader.LoaderService;
import org.example.openapitestgenerator.service.parser.ParserService;
import org.example.openapitestgenerator.service.postman.PostmanCollectionGenerator;
import org.example.openapitestgenerator.service.testcases.TestCasesPromptBuilderService;
import org.example.openapitestgenerator.service.selenium.SeleniumTestsGeneratorService;
import org.example.openapitestgenerator.service.testcases.TestCasesGeneratorService;
import org.example.openapitestgenerator.service.zephyr.CsvZephyrService;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.List;
import java.util.Scanner;

@ShellComponent
public class Cli
{
    private final LoaderService loader;
    private final ParserService parser;
    private final SeleniumTestsGeneratorService seleniumTestsGenerator;
    private final TestCasesGeneratorService testCasesGenerator;
    private final PostmanCollectionGenerator postmanGenerator;
    private final LlmResponseSaverService saver;
    private final ParallelExecutorService executor;
    private final TestCasesPromptBuilderService promptBuilder;
    private final CsvZephyrService csvZephyrService;
    Scanner scanner = new Scanner(System.in);
    String rawOpenApi;

    Cli(LoaderService loader, ParserService parser, SeleniumTestsGeneratorService seleniumTestsGenerator, TestCasesGeneratorService testCasesGenerator,
        PostmanCollectionGenerator postmanGenerator, LlmResponseSaverService saver, ParallelExecutorService executor, TestCasesPromptBuilderService promptBuilder, CsvZephyrService csvZephyrService)
    {
        this.loader = loader;
        this.parser = parser;
        this.seleniumTestsGenerator = seleniumTestsGenerator;
        this.testCasesGenerator = testCasesGenerator;
        this.postmanGenerator = postmanGenerator;
        this.saver = saver;
        this.executor = executor;
        this.promptBuilder = promptBuilder;
        this.csvZephyrService = csvZephyrService;
    }

    @ShellMethod(key = "start", value = "Открыть меню")
    public void run()
    {
        boolean running = true;

        String url = "https://api.apis.guru/v2/specs/httpbin.org/0.9.2/openapi.json";
        rawOpenApi = loader.load(url);

        while (running)
        {
            System.out.println("Выберите действие");
            System.out.println("1. Генерация тест-кейсов");
            System.out.println("2. Генерация Selenium UI тестов");
            System.out.println("3. Генерация Postman API тестов");
            System.out.println("4. Генерация тест-кейсов для Zephyr");
            System.out.println("5. Выход");

            String choice = scanner.nextLine().trim();

            switch (choice)
            {
                case "1":
                    testcases();
                    break;

                case "2":
                    selenium();
                    break;

                case "3":
                    postman();
                    break;

                case "4":
                    zephyr();
                    break;

                case "5":
                    System.out.println("Завершение работы");
                    running = false;
                    break;

                default:
                    System.out.println("Неверный ввод");
                    break;
            }
        }
    }

    @ShellMethod(key = "testcase", value = "Генерация тест-кейсов")
    public void testcases()
    {
        System.out.println("Запуск генерации тест-кейсов");
        List<EndpointInfo> endpoints = parser.parseOpenApiSpec(rawOpenApi);
        System.out.println("Выберите режим работы");
        System.out.println("1. Однопоточный");
        System.out.println("2. Многопоточный"); // На данный момент бесполезен
        String choice = scanner.nextLine().trim();
        List<TestCase> testCases;
        switch (choice)
        {
            case "1":
                String userPrompt = promptBuilder.buildPrompt(endpoints);
                testCases = testCasesGenerator.generateTestCases(userPrompt);
                break;

            case "2":
                testCases = executor.generateParallel(endpoints, 2, testCasesGenerator::processor);
                break;

            default:
                System.out.println("Неверный ввод");
                return;
        }

        String stringTestCases = testCasesGenerator.testCaseToString(testCases);
        saver.save(stringTestCases, "src/main/resources/testcases.json");
        System.out.println("Тест-кейсы сгенерированы");
    }

    @ShellMethod(key = "selenium", value = "Генерация Selenium UI тестов")
    public void selenium()
    {
        System.out.println("Запуск генерации Selenium UI тестов");
        String tests = seleniumTestsGenerator.generateTests("testcases.json");
        saver.save(tests, "src/test/java/org/example/openapitestgenerator/selenium/GeneratedTests.java");
        System.out.println("Тесты сгенерированы");
        System.out.println("1. Запуск сгенерированных тестов");
        System.out.println("2. Вернуться в меню");

        String subChoice = scanner.nextLine().trim();

        if ("1".equals(subChoice))
        {
            System.out.println("Запуск тестов");
            seleniumTestsGenerator.runTests();
        }
    }

    @ShellMethod(key = "postman", value = "Генерация Postman API тестов")
    public void postman()
    {
        System.out.println("Запуск генерации Postman API тестов");
        String collection = postmanGenerator.generate(testCasesGenerator.testCasesDeserialization());
        String collectionWithParams = postmanGenerator.generateParams(collection);
        saver.save(collectionWithParams, "src/main/resources/collection.json");
        System.out.println("Коллекция сгенерирована");
        System.out.println("1. Запуск тестов");
        System.out.println("2. Вернуться в меню");

        String anotherChoice = scanner.nextLine().trim();

        if ("1".equals(anotherChoice))
        {
            System.out.println("Запуск тестов");
            postmanGenerator.startNewman();
        }
    }

    @ShellMethod(key = "zephyr", value = "Генерация тест кейсов для Zephyr")
    public void zephyr(){
        System.out.println("Запуск генерации тест-кейсов для Zephyr");
        List<TestCase> testCases = testCasesGenerator.testCasesDeserialization();
        csvZephyrService.generateZephyrTestCases(testCases, "src/main/resources/zephyr.csv");
        System.out.println("Тест-кейсы для Zephyr сгенерированы");
    }


}