package org.example.openapitestgenerator.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record TestCase(int id, String name, List<String> preconditions, List<String> steps,
                       List<String> expectedResults, List<String> test_data)
{}
