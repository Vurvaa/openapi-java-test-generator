package org.example.openapitestgenerator.service.multithreading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SplitterService
{
    private static final Logger log = LoggerFactory.getLogger(SplitterService.class);

    public <T> List<List<T>> split(List<T> dataToSplit, int parts)
    {
        List<List<T>> result = new ArrayList<>();

        if (dataToSplit.size() < parts)
        {
            parts = dataToSplit.size();
        }

        int chunk = (int) Math.ceil((double) dataToSplit.size() / parts);
        int listNumber = 0;
        int currentChunk = chunk;

        for (int i = 0; i < parts; i++)
        {
            result.add(new ArrayList<>());
        }

        for (int i = 0; i < dataToSplit.size(); i+=chunk)
        {
            if(i+chunk > dataToSplit.size())
            {
                currentChunk = dataToSplit.size() - i;
            }

            if (listNumber >= parts)
            {
                listNumber = parts - 1;
            }

            for(int startIndex = 0; startIndex < currentChunk; startIndex++)
            {
                result.get(listNumber).add(dataToSplit.get(i + startIndex));
            }

            listNumber++;
        }

        log.info("The data were split");

        return result;
    }
}
