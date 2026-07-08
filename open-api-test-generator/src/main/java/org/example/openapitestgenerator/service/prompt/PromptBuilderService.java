package org.example.openapitestgenerator.service.prompt;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.openapitestgenerator.dto.config.Config;
import org.example.openapitestgenerator.dto.endpoints.EndpointInfo;
import org.example.openapitestgenerator.exceptions.PromptBuildingException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PromptBuilderService {
    private final ObjectMapper objectMapper;
    private final Config config;

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
                        - строго следуй структуре эталонного JSON из системного промпта;
                        - устанавливай target.uiPage строго в "/swagger-ui/index.html".
                        """;

    public PromptBuilderService(Config config) {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_EMPTY);
        this.config = config;
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

    public String getSystemPrompt() {
        return config.prompts().testCasesPrompt();
    }
}