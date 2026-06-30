package org.example.openapitestgenerator.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record TestCase(int id, String name, Target target, InputData inputData, Expected expected) {}
