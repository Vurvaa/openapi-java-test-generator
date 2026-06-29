package org.example.openapitestgenerator.service;

import org.example.openapitestgenerator.dto.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.Collections;
import java.util.List;

@Service
public class TestCaseParserService
{
    private static final Logger log = LoggerFactory.getLogger(TestCaseParserService.class);
    protected final ObjectMapper mapper = new ObjectMapper();

    public List<TestCase> testCasesDeserialization(File testCases)
    {
        log.info("Началась десериализация");

        if(testCases == null || !testCases.exists())
        {
            log.error("Файл тест-кейсов отсутствует или равен null");
            return null;
        }

        try
        {
            List<TestCase> response = mapper.readValue(testCases, new TypeReference<>() {});
            log.info("Десериализация завершена успешно");
            return response;
        }
        catch (tools.jackson.core.JacksonException e)
        {
            log.error("Ошибка при десериализации JSON", e);
            return Collections.emptyList();
        }
    }
}
