package org.example.openapitestgenerator.service.zephyr;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.example.openapitestgenerator.dto.testcase.TestCase;
import org.example.openapitestgenerator.exceptions.ZephyrTestCaseException;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class CsvZephyrService extends ZephyrService {
    private final String[] HEADERS = {"Summary", "Precondition", "Objective", "Test Script"};

    public void generateZephyrTestCases(List<TestCase> testCaseList, String zephyrFilePath){
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
                        buildTestScript(testCase)
                );
            }
        } catch (IOException e) {
            log.error("Error writing to Zephyr CSV file: {}", zephyrFilePath, e);
            throw new ZephyrTestCaseException("Error writing to Zephyr CSV file: " + zephyrFilePath);
        }
    }

    @Override
    protected String buildPrecondition(TestCase tc) {
        return String.join("\n",
                "Метод: " + tc.target().method(),
                "Endpoint: " + tc.target().path()
        );
    }

    private String buildTestScript(TestCase tc) {
        StringBuilder script = new StringBuilder();
        int stepNumber = 1;

        if (tc.target().uiPage() != null && !tc.target().uiPage().isBlank()) {
            script.append("Шаг ").append(stepNumber++).append(": Открыть страницу ")
                    .append(tc.target().uiPage()).append("\n");
            script.append("  Ожидаемый результат: Страница успешно загружена\n\n");
        }

        script.append("Шаг ").append(stepNumber++).append(": Отправить ")
                .append(tc.target().method()).append(" ").append(tc.target().path()).append("\n");

        String inputData = serializeInputDataToJson(tc.inputData());

        if (inputData != null && !inputData.isBlank()) {
            script.append("  Данные: ").append(inputData).append("\n");
        }

        script.append("  Ожидаемый результат: Получен HTTP-статус ")
                .append(tc.expected().status()).append("\n\n");

        if (tc.expected().uiAssertText() != null && !tc.expected().uiAssertText().isBlank()) {
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
}
