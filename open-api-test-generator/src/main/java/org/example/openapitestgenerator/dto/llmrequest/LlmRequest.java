package org.example.openapitestgenerator.dto.llmrequest;

import java.util.List;

public record LlmRequest(String model, List<MessageToLlm> messages, double temperature) {}
