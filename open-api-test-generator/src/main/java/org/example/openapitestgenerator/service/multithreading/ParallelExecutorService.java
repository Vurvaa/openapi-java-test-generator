package org.example.openapitestgenerator.service.multithreading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;

@Service
public class ParallelExecutorService
{
    private static final Logger log = LoggerFactory.getLogger(ParallelExecutorService.class);
    private final ExecutorService executor;
    private final SplitterService splitter;
    private static final int THREADS_NUMBER = 2;

    ParallelExecutorService(SplitterService splitter)
    {
        this.executor = Executors.newFixedThreadPool(THREADS_NUMBER);
        this.splitter = splitter;
    }

    public <InputT, OutputT> List<OutputT> generateParallel(List<InputT> dataToSplit, int parts, Function<List<InputT>, List<OutputT>> processor)
    {
        List<List<InputT>> splittedData = splitter.split(dataToSplit, parts);
        List<Future<List<OutputT>>> futureList = new ArrayList<>();

        for (List<InputT> chunk : splittedData)
        {
            if (chunk == null || chunk.isEmpty())
            {
                continue;
            }

            Callable<List<OutputT>> task = () -> processor.apply(chunk);

            log.info("Size of chunk: {}", chunk.size());

            futureList.add(executor.submit(task));
        }

        List<OutputT> resultList = new ArrayList<>();

        for (Future<List<OutputT>> future : futureList)
        {
            try
            {
                resultList.addAll(future.get());
            }
            catch (InterruptedException e)
            {
                log.error("Thread execution was interrupted while waiting for the result", e);
                Thread.currentThread().interrupt();
                throw new RuntimeException("Test case generation was interrupted", e);
            }
            catch (ExecutionException e)
            {
                log.error("An error occurred during task execution inside the thread");
                throw new RuntimeException("Parallel generation task failed", e);
            }
        }

        log.info("The executor has finished its work successfully");
        return resultList;
    }
}
