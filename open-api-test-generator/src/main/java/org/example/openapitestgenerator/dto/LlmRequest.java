package org.example.openapitestgenerator.dto;

import java.util.List;

public record LlmRequest(String model, List<Message> messages, double temperature) {}
