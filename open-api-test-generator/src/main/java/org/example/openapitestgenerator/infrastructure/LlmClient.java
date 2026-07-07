package org.example.openapitestgenerator.infrastructure;

import org.example.openapitestgenerator.dto.config.Config;
import org.example.openapitestgenerator.dto.llmrequest.LlmRequest;
import org.example.openapitestgenerator.dto.llmrequest.MessageToLlm;
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

        log.info("Creating a REST client");

         this.restClient = RestClient.builder().baseUrl(config.model().baseUrl())
                .requestFactory(new SimpleClientHttpRequestFactory())
                .build();
    }

    public String generateResponse(List<MessageToLlm> messages)
    {
        final String name = config.model().name();
        final String modelName = config.model().modelName();
        final double temperature = config.model().temperature();
        final String uri = "/v1/chat/completions";

        LlmRequest requestBody = new LlmRequest(modelName, messages, temperature);

        log.info("Sending a request to the model {}", name);
        log.debug("Request: {}", requestBody);

        try
        {
            String response = restClient.post()
                    .uri(uri)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            log.info("Response received");

            return response;
        }
        catch (RestClientException e)
        {
            log.error("Error contacting the LLM. No response received.", e);
            throw new RuntimeException("Error contacting the LLM. No response received.", e);
        }
    }
}
