package org.example.openapitestgenerator.cli;

import org.example.openapitestgenerator.dto.endpoints.EndpointInfo;
import org.example.openapitestgenerator.service.*;
import org.example.openapitestgenerator.service.prompt.PromptBuilderService;
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
    private final PromptBuilderService promptBuilder;
    private final TestCasesGeneratorService testCasesGenerator;
    private final PostmanCollectionGenerator postmanGenerator;
    Scanner scanner = new Scanner(System.in);
    String rawOpenApi;

    Cli(LoaderService loader, ParserService parser, SeleniumTestsGeneratorService seleniumTestsGenerator,
        PromptBuilderService promptBuilder, TestCasesGeneratorService testCasesGenerator,
        PostmanCollectionGenerator postmanGenerator)
    {
        this.loader = loader;
        this.parser = parser;
        this.seleniumTestsGenerator = seleniumTestsGenerator;
        this.promptBuilder = promptBuilder;
        this.testCasesGenerator = testCasesGenerator;
        this.postmanGenerator = postmanGenerator;
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
            System.out.println("4. Выход");

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
        String userPrompt = promptBuilder.buildPrompt(endpoints);
        testCasesGenerator.generateTestCases(userPrompt);
        System.out.println("Тест-кейсы сгенерированы");
    }

    @ShellMethod(key = "selenium", value = "Генерация Postman API тестов")
    public void selenium()
    {
        System.out.println("Запуск генерации Selenium UI тестов");
        String tests = seleniumTestsGenerator.generateTests("testcases.json");
        seleniumTestsGenerator.saveTests(tests, "src/test/java/org/example/openapitestgenerator/selenium/GeneratedTests.java");
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

    @ShellMethod(key = "postman", value = "Открыть меню")
    public void postman()
    {
        System.out.println("Запуск генерации Postman API тестов");
        String collection = postmanGenerator.generate(testCasesGenerator.testCasesDeserialization());
        String collectionWithParams = postmanGenerator.generateParams(collection);
        postmanGenerator.save(collectionWithParams, "src/main/resources/collection.json");
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
}