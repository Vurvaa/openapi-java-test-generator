package org.example.openapitestgenerator.dto.testcase;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record TestCase(int id, String name, Target target, InputData inputData, Expected expected) {}
