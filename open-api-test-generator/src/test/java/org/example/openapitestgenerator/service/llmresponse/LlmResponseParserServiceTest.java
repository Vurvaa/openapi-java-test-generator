package org.example.openapitestgenerator.service.llmresponse;

import org.example.openapitestgenerator.dto.llmresponse.Choice;
import org.example.openapitestgenerator.dto.llmresponse.LlmResponse;
import org.example.openapitestgenerator.dto.llmresponse.Message;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class LlmResponseParserServiceTest {

    private final LlmResponseParserService parserService = new LlmResponseParserService();

    @Test
    void parseShouldReturnCodeFromJavaBlock() {
        LlmResponse response = response("""
                ```java
                public class Test {
                    void test() {
                    }
                }
                ```
                """);

        String result = parserService.parse(response);

        assertThat(result).isEqualTo("""
                public class Test {
                    void test() {
                    }
                }""");
    }

    @Test
    void parseShouldReturnTrimmedContentWhenJavaBlockNotFound() {
        LlmResponse response = response("public class Test {}");

        String result = parserService.parse(response);

        assertThat(result).isEqualTo("public class Test {}");
    }

    @Test
    void parseShouldIgnoreTextBeforeAndAfterJavaBlock() {
        LlmResponse response = response("""
                Test text:
                ```java
                class UserTest {}
                ```
                Test text.
                """);

        String result = parserService.parse(response);

        assertThat(result).isEqualTo("class UserTest {}");
    }

    @Test
    void parseShouldReturnEmptyStringWhenJavaBlockIsEmpty() {
        String expectedMessage = "java";
        LlmResponse response = response("```java```");

        String result = parserService.parse(response);

        assertThat(result).isEqualTo(expectedMessage);
    }

    private LlmResponse response(String content) {
        Choice choice = new Choice(new Message(content));
        return new LlmResponse(List.of(choice));
    }
}