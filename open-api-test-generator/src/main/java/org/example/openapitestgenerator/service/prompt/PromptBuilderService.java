package org.example.openapitestgenerator.service.prompt;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.openapitestgenerator.dto.EndpointInfo;
import org.example.openapitestgenerator.exceptions.PromptBuildingException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PromptBuilderService {
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = """
                        Ты генератор тест-кейсов.

                        Твоя задача: по входным данным с endpoint'ами OpenAPI вернуть ТОЛЬКО JSON-массив тест-кейсов.
                        Не добавляй пояснения, markdown, комментарии или любой другой текст вне JSON.

                                                Формат данных для каждого тест-кейса:
                                                {
                                                    "id": 1,
                                                    "name": "...",
                                                    "target": {
                                                        "method": "...",
                                                        "path": "...",
                                                        "uiPage": "..."
                                                    },
                                                    "inputData": { ... },
                                                    "expected": {
                                                        "status": 200,
                                                        "uiAssertText": "...",
                                                        "assertions": ["..."]
                                                    }
                                                }

                        Правила:
                        - используй только поля id, name, target, inputData, expected;
                                                - target должен содержать method, path и, если известно, uiPage;
                                                - если у endpoint'а есть query-параметры, которые должны быть частью конкретного запроса, добавляй их прямо в target.path как query string, например: "/api/v1/users?page=1&size=10";
                                                - если query-параметры нужны для вариаций тестов, отражай их также в inputData;
                        - expected должен содержать status, если уместно uiAssertText и assertions;
                        - assertions всегда массив строк;
                        - если данных не хватает, заполни поле наиболее разумно, но не выдумывай лишние поля;
                        - сохрани русский язык для name;
                        - возвращай результат строго в виде JSON без обёрток.

                                                Дополнительные правила для качества тестов:
                                                - если endpoint поддерживает пагинацию, обязательно добавляй кейсы с первой страницей, следующей страницей, пустой страницей и граничными значениями page/size/limit;
                                                - если endpoint требует авторизацию, добавляй позитивные и негативные кейсы для валидной, отсутствующей и невалидной авторизации;
                                                - если в схеме встречаются anyOf, allOf, oneOf или not, учитывай все ветки и создавай отдельные тест-кейсы под каждую значимую комбинацию;
                                                - если есть enum, required, nullable, default, example, minimum, maximum, minLength, maxLength, pattern, format, используй их для построения inputData и assertions;
                                                - для ответов 4xx и 5xx обязательно создавай негативные кейсы, если это вытекает из спецификации;
                                                - не смешивай разные сценарии в одном тест-кейсе, если это ухудшает читаемость.
                        """;

    private static final String USER_PROMPT_INTRO = """
                        Тебе передают набор endpoint'ов OpenAPI в виде JSON.
                        Для каждого endpoint'а нужно учитывать:
                        - path и method
                        - operationId and summary
                        - tags
                        - security
                        - parameters
                        - requestBody
                        - responses

            Требования:
            - анализируй каждый endpoint отдельно;
            - учитывай ограничения схем: type, format, enum, required, nullable, default, example, min/max;
            - если данных недостаточно, явно укажи это;
            - не игнорируй response-коды и ошибки валидации;
            - верни результат в том формате, который ожидает вызывающая сторона.
            """;

    public PromptBuilderService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_EMPTY);
    }

    public String buildPrompt(List<EndpointInfo> endpoints) {
        try {
            if(endpoints == null || endpoints.isEmpty()) {
                throw new PromptBuildingException("endpoints is null or empty");
            }

            StringBuilder prompt = new StringBuilder();
            prompt.append(USER_PROMPT_INTRO.strip()).append("\n\n");
            prompt.append("ENDPOINTS:\n");

            for (int i = 0; i < endpoints.size(); i++) {
                EndpointInfo endpoint = endpoints.get(i);
                prompt.append("\n--- Endpoint ").append(i + 1).append(" ---\n");
                prompt.append(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(endpoint));
                prompt.append("\n");
            }

            return prompt.toString();
        }catch(JsonProcessingException e){
            throw new PromptBuildingException("Error building prompt: " + e.getMessage());
        }
    }
}