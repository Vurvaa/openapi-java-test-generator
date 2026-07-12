package org.example.openapitestgenerator.dto.zephyr;

import java.util.List;
import java.util.Map;

public record Step(
        int index,
        String description,
        String testData,
        String expectedResult,
        Map<String, Object> customFieldValueIndex,
        List<Object> customFieldValues
) {
    public Step(int index, String description, String testData, String expectedResult) {
        this(index, description, testData, expectedResult, Map.of(), List.of());
    }
}
