package org.example.openapitestgenerator.service;

import org.example.openapitestgenerator.dto.postman.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostmanScriptGeneratorTest {
    private PostmanScriptGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new PostmanScriptGenerator();
    }

    @Test
    void generateReturnsEmptyListWhenExpectedIsNull() {
        List<Event> result = generator.generate(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}