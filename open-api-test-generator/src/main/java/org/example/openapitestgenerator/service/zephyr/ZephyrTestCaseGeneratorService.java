package org.example.openapitestgenerator.service.zephyr;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.example.openapitestgenerator.dto.testcase.InputData;
import org.example.openapitestgenerator.dto.testcase.TestCase;
import org.example.openapitestgenerator.exceptions.ZephyrTestCaseException;
import org.example.openapitestgenerator.service.TestCaseParserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class ZephyrTestCaseGeneratorService {
    private static final Logger log = LoggerFactory.getLogger(ZephyrTestCaseGeneratorService.class);
    private final TestCaseParserService testCaseParserService;
    private final String[] HEADERS = {"Summary", "Precondition", "Objective", "Test Script"};
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ZephyrTestCaseGeneratorService(TestCaseParserService testCaseParserService) {
        this.testCaseParserService = testCaseParserService;
    }


    public void convertTestCasesToCSVZephyrFormat(String jsonFilePath, String zephyrFilePath) {
        log.info("Parsing test cases from JSON file: {}", jsonFilePath);

        try {
            Path jsonPath = Path.of(jsonFilePath);
            List<TestCase> testCaseList = testCaseParserService.testCasesDeserialization(Files.newInputStream(jsonPath));

            saveToZephyrCSV(testCaseList, zephyrFilePath);
        } catch (IOException e) {
            log.error("Error reading JSON file: {}", jsonFilePath, e);
            throw new ZephyrTestCaseException("Error reading JSON file: " + jsonFilePath);
        }
    }

    public void saveToZephyrCSV(List<TestCase> testCaseList, String zephyrFilePath) {
        log.info("Saving test cases to Zephyr CSV format");

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(HEADERS)
                .get();
        try (CSVPrinter csvPrinter = new CSVPrinter(
                new OutputStreamWriter(new FileOutputStream(zephyrFilePath), StandardCharsets.UTF_8),
                csvFormat)) {
            for (TestCase testCase : testCaseList) {
                csvPrinter.printRecord(
                        testCase.name(),
                        buildPrecondition(testCase),
                        buildObjective(testCase),
                        buildTestScriptForCsv(testCase)
                );
            }
        } catch (IOException e) {
            log.error("Error writing to Zephyr CSV file: {}", zephyrFilePath, e);
            throw new ZephyrTestCaseException("Error writing to Zephyr CSV file: " + zephyrFilePath);
        }

    }

    private String buildPrecondition(TestCase tc) {
        return String.join("\n",
                "Метод: " + tc.target().method(),
                "Endpoint: " + tc.target().path()
        );
    }

    private String buildObjective(TestCase tc) {
        return "Проверить сценарий: " + tc.name();
    }

    private String buildTestScriptForCsv(TestCase tc) {
        StringBuilder script = new StringBuilder();
        int stepNumber = 1;

        if (tc.target().uiPage() != null && !tc.target().uiPage().isEmpty()) {
            script.append("Шаг ").append(stepNumber++).append(": Открыть страницу ")
                    .append(tc.target().uiPage()).append("\n");
            script.append("  Ожидаемый результат: Страница успешно загружена\n\n");
        }

        script.append("Шаг ").append(stepNumber++).append(": Отправить ")
                .append(tc.target().method()).append(" ").append(tc.target().path()).append("\n");

        String inputData = serializeInputDataToJson(tc.inputData());

        if (inputData != null && !inputData.isEmpty()) {
            script.append("  Данные: ").append(inputData).append("\n");
        }

        script.append("  Ожидаемый результат: Получен HTTP-статус ")
                .append(tc.expected().status()).append("\n\n");

        if (tc.expected().uiAssertText() != null && !tc.expected().uiAssertText().isEmpty()) {
            script.append("Шаг ").append(stepNumber++).append(": Проверить текст на странице\n");
            script.append("  Ожидаемый результат: Отображается текст: ")
                    .append(tc.expected().uiAssertText()).append("\n\n");
        }

        if (tc.expected().assertions() != null && !tc.expected().assertions().isEmpty()) {
            for (String assertion : tc.expected().assertions()) {
                if (assertion != null && !assertion.isEmpty()) {
                    script.append("Шаг ").append(stepNumber++).append(": Проверить утверждение\n");
                    script.append("  Данные: ").append(assertion).append("\n");
                    script.append("  Ожидаемый результат: Утверждение истинно\n\n");
                }
            }
        }

        return script.toString().trim();
    }


    private String serializeInputDataToJson(InputData inputData) {
        if (inputData == null) {
            return "";
        }
        return objectMapper.writeValueAsString(inputData);

    }

}
