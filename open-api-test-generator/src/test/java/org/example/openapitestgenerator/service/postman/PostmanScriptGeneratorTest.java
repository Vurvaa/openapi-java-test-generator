package org.example.openapitestgenerator.service.postman;

import org.example.openapitestgenerator.dto.postman.Event;
import org.example.openapitestgenerator.dto.testcase.Expected;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class PostmanScriptGeneratorTest {

    private PostmanScriptGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new PostmanScriptGenerator();
    }

    @Test
    void generateShouldReturnEmptyListWhenExpectedIsNull() {
        List<Event> events = generator.generate(null);

        assertTrue(events.isEmpty());
    }

    @Test
    void generateShouldCreateStatusAssertionOnlyWhenAssertionsAreNull() {
        Expected expected = new Expected(200, "test",null);

        List<Event> events = generator.generate(expected);

        assertEquals(1, events.size());

        Event event = events.getFirst();

        assertEquals("test", event.listen());
        assertEquals("text/javascript", event.script().type());

        assertEquals(
                List.of(
                        "pm.test(\"Status code is 200\", function () {",
                        "    pm.response.to.have.status(200);",
                        "});"
                ),
                event.script().exec()
        );
    }

    @Test
    void generateShouldCreateStatusAssertionOnlyWhenAssertionsAreEmpty() {
        Expected expected = new Expected(404, "test", List.of());

        List<Event> events = generator.generate(expected);

        assertEquals(1, events.size());

        Event event = events.getFirst();

        assertEquals("test", event.listen());
        assertEquals("text/javascript", event.script().type());

        assertEquals(
                List.of(
                        "pm.test(\"Status code is 404\", function () {",
                        "    pm.response.to.have.status(404);",
                        "});"
                ),
                event.script().exec()
        );
    }

    @Test
    void generateShouldCreateBodyAssertionForNotNullField() {
        Expected expected = new Expected(
                200,
                "test",
                List.of("response.body.id != null")
        );

        List<Event> events = generator.generate(expected);

        assertEquals(1, events.size());

        Event event = events.getFirst();

        assertEquals("test", event.listen());
        assertEquals("text/javascript", event.script().type());
    }

    @Test
    void generateShouldCreateBodyAssertionsForSeveralFields() {
        Expected expected = new Expected(
                201,
                "test",
                List.of(
                        "response.body.id != null",
                        "response.body.name != null"
                )
        );

        List<Event> events = generator.generate(expected);

        List<String> scriptLines = events.getFirst().script().exec();

        assertTrue(scriptLines.contains("    pm.expect(jsonData.id).to.not.be.null;"));
        assertTrue(scriptLines.contains("    pm.expect(jsonData.id).to.not.be.undefined;"));
        assertTrue(scriptLines.contains("    pm.expect(jsonData.name).to.not.be.null;"));
        assertTrue(scriptLines.contains("    pm.expect(jsonData.name).to.not.be.undefined;"));
    }

    @Test
    void generateShouldAddCommentForUnsupportedAssertion() {
        Expected expected = new Expected(
                200,
                "test",
                List.of("response.body.id == 1")
        );

        List<Event> events = generator.generate(expected);

        List<String> scriptLines = events.getFirst().script().exec();

        assertTrue(scriptLines.contains("    // Unsupported assertion format: response.body.id == 1"));
    }

    @Test
    void generateShouldEscapeQuotesInAssertionName() {
        Expected expected = new Expected(
                200,
                "test",
                List.of("response.body.\"id\" == 1")
        );

        List<Event> events = generator.generate(expected);

        List<String> scriptLines = events.getFirst().script().exec();

        assertTrue(scriptLines.contains("pm.test(\"response.body.\\\"id\\\" == 1\", function () {"));
    }

    @Test
    void generateShouldEscapeBackslashesInAssertionName() {
        Expected expected = new Expected(
                200,
                "test",
                List.of("response.body.path\\value == 1")
        );

        List<Event> events = generator.generate(expected);

        List<String> scriptLines = events.getFirst().script().exec();

        assertTrue(scriptLines.contains("pm.test(\"response.body.path\\\\value == 1\", function () {"));
        assertTrue(scriptLines.contains("    // Unsupported assertion format: response.body.path\\\\value == 1"));
    }
}