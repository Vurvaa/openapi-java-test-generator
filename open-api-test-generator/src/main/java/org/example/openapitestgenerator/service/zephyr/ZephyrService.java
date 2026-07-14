package org.example.openapitestgenerator.service.zephyr;
import org.example.openapitestgenerator.dto.testcase.InputData;
import org.example.openapitestgenerator.dto.testcase.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectMapper;

public abstract class ZephyrService {
    protected static final Logger log = LoggerFactory.getLogger(ZephyrService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    protected abstract String buildPrecondition(TestCase tc);

    protected String buildObjective(TestCase tc) {
        return "Проверить сценарий: " + tc.name();
    }

    protected String serializeInputDataToJson(InputData inputData) {
        if (inputData == null) {
            return "";
        }
        return objectMapper.writeValueAsString(inputData);
    }


}
