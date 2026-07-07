package org.example.openapitestgenerator.service;

import org.example.openapitestgenerator.dto.testcase.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

@Service
public class TestCaseParserService
{
    private static final Logger log = LoggerFactory.getLogger(TestCaseParserService.class);
    protected final ObjectMapper mapper = new ObjectMapper();

    public List<TestCase> testCasesDeserialization(InputStream testCases)
    {
        log.info("Deserialization has started");

        if(testCases == null)
        {
            log.error("Поток ввода равен null");
            return Collections.emptyList();
        }

        try
        {
            List<TestCase> response = mapper.readValue(testCases, new TypeReference<>() {});
            log.info("Deserialization completed successfully");
            return response;
        }
        catch (tools.jackson.core.JacksonException e)
        {
            log.error("Error deserializing JSON", e);
            return Collections.emptyList();
        }
    }
}
