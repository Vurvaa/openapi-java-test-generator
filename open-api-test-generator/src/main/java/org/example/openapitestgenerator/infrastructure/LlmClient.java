package org.example.openapitestgenerator.infrastructure;

import org.example.openapitestgenerator.dto.Config;
import org.example.openapitestgenerator.dto.LlmRequest;
import org.example.openapitestgenerator.dto.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
public class LlmClient
{
    private static final Logger log = LoggerFactory.getLogger(LlmClient.class);
    
    private final Config config;
    private final RestClient restClient;

    LlmClient(Config config)
    {
        this.config = config;

        log.info("Создаем rest клиент");

         this.restClient = RestClient.builder().baseUrl(config.model().baseUrl())
                .requestFactory(new SimpleClientHttpRequestFactory())
                .build();
    }

    public String generateResponse(List<Message> messages)
    {
        final String name = config.model().name();
        final String modelName = config.model().modelName();
        final double temperature = config.model().temperature();
        final String uri = "/v1/chat/completions";

        LlmRequest requestBody = new LlmRequest(modelName, messages, temperature);

        log.info("Отправка запроса в модель {}", name);
        log.debug("Запрос: {}", requestBody);

        try
        {
            String response = restClient.post()
                    .uri(uri)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            log.info("Ответ получен");

            return response;
        }
        catch (RestClientException e)
        {
            log.error("Ошибка при обращении к LLM. Ответ не получен", e);
            throw new RuntimeException("Ошибка при обращении к LLM. Ответ не получен", e);
        }
    }
}
