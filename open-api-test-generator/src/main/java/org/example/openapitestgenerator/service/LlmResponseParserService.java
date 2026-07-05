package org.example.openapitestgenerator.service;

import org.example.openapitestgenerator.dto.llmresponse.LlmResponse;
import org.springframework.stereotype.Service;

@Service
public class LlmResponseParserService
{
    public String parse(LlmResponse llmResponse)
    {
        String startMark = "```java";
        String endMark = "```";
        String content = llmResponse.choices().getFirst().message().content();

        int startIndex = content.indexOf(startMark);
        if (startIndex == -1)
        {
            return content.trim();
        }

        startIndex += startMark.length();

        int endIndex = content.indexOf(endMark, startIndex);
        if (endIndex == -1)
        {
            return content.substring(startIndex).trim();
        }

        return content.substring(startIndex, endIndex).trim();
    }
}
