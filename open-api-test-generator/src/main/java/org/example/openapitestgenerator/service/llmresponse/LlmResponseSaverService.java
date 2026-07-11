package org.example.openapitestgenerator.service.llmresponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class LlmResponseSaverService
{
    private static final Logger log = LoggerFactory.getLogger(LlmResponseSaverService.class);

    public void save(String llmResponse, String outputPath)
    {
        Path path = Path.of(outputPath);

        try
        {
            Path parentDir = path.getParent();

            if (parentDir != null)
            {
                Files.createDirectories(parentDir);
            }

            Files.writeString(path, llmResponse);

            log.info("The test file has been saved.");
        }
        catch (IOException e)
        {
            log.error("Failed to save the file", e);
            throw new RuntimeException("Failed to save the file", e);
        }
    }
}
