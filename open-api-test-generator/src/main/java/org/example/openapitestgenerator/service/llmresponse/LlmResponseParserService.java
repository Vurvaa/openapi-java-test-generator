package org.example.openapitestgenerator.service.llmresponse;

import org.example.openapitestgenerator.dto.llmresponse.LlmResponse;
import org.springframework.stereotype.Service;

@Service
public class LlmResponseParserService
{
    public String parse(LlmResponse llmResponse)
    {
        String mark = "```";
        String content = llmResponse.choices().getFirst().message().content().trim();

        int startIndex = content.indexOf(mark);
        if (startIndex == -1)
        {
            return content.trim();
        }

        startIndex += mark.length();

        int nextNewLine = content.indexOf("\n", startIndex);
        if (nextNewLine != -1)
        {
            startIndex = nextNewLine + 1;
        }

        int endIndex = content.indexOf(mark, startIndex);
        if (endIndex == -1)
        {
            return content.substring(startIndex).trim();
        }

        return content.substring(startIndex, endIndex).trim();
    }
}
