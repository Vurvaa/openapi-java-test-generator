package org.example.openapitestgenerator.service.zephyr;

import org.example.openapitestgenerator.dto.testcase.TestCase;
import org.example.openapitestgenerator.dto.zephyr.Step;
import org.example.openapitestgenerator.dto.zephyr.TestScript;
import org.example.openapitestgenerator.dto.zephyr.ZephyrConfig;
import org.example.openapitestgenerator.dto.zephyr.ZephyrTestCase;
import org.example.openapitestgenerator.exceptions.ZephyrTestCaseException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;


@Service
public class ApiZephyrService extends ZephyrService {
    private final RestClient restClient;
    private final ZephyrConfig config;

    public ApiZephyrService(ZephyrConfig config) {
        this.config = config;

        String zephyrAuth = "Bearer " + config.apiToken();

        this.restClient = RestClient.builder()
                .baseUrl(config.baseUrl())
                .defaultHeader("Authorization", zephyrAuth)
                .build();

    }

    @SuppressWarnings(value = "unchecked")
    public void generateZephyrTestCase(TestCase tc) {
        List<Step> steps = buildSteps(tc);

        ZephyrTestCase testCase =  new ZephyrTestCase(
                config.projectKey(),
                tc.name(),
                config.folderId(),
                config.ownerId(),
                config.defaultStatusName(),
                config.defaultPriorityName(),
                List.of("auto-generated"),
                buildPrecondition(tc),
                buildObjective(tc),
                new TestScript("Step by Step", steps)
        );

        log.info("Sending create test case into Zephyr Cloud");

        try {
            Map<String, Object> response = restClient.post()
                .uri("/testcases")
                .contentType(MediaType.APPLICATION_JSON)
                .body(testCase)
                .retrieve()
                .body(Map.class);

            log.info("Zephyr test case created with ID: {}", response.get("id"));
        } catch (RestClientResponseException e) {
            log.error("Zephyr returned {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ZephyrTestCaseException("Error with test-case creation in Zephyr");
        }
    }

    @Override
    protected String buildPrecondition(TestCase tc) {
        return String.join("<br />",
                "Метод: " + tc.target().method(),
                "Endpoint: " + tc.target().path()
        );
    }

    private List<Step> buildSteps(TestCase tc) {
        AtomicInteger idx = new AtomicInteger(0);
        List<Step> steps = new ArrayList<>();

        if (tc.target().uiPage() != null && !tc.target().uiPage().isEmpty()) {
            steps.add(new Step(
                    idx.getAndIncrement(),
                    "Открыть страницу " + tc.target().uiPage(),
                    tc.target().uiPage(),
                    "Страница успешно загружена"
            ));
        }

        steps.add(new Step(
                idx.getAndIncrement(),
                "Отправить " + tc.target().method() + " " + tc.target().path(),
                serializeInputDataToJson(tc.inputData()),
                "Получен HTTP-статус " + tc.expected().status()
        ));

        if (tc.expected().uiAssertText() != null && !tc.expected().uiAssertText().isBlank()) {
            steps.add(new Step(
                    idx.getAndIncrement(),
                    "Проверить текст на странице",
                    "",
                    "Отображается текст: " + tc.expected().uiAssertText()
            ));
        }

        if (tc.expected().assertions() != null) {
            tc.expected().assertions().stream()
                    .filter(Objects::nonNull)
                    .forEach(a -> steps.add(new Step(
                            idx.getAndIncrement(),
                            "Проверить утверждение",
                            a,
                            "Утверждение истинно"
                    )));
        }

        return steps;
    }
}
