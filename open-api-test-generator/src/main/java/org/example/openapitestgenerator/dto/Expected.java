package org.example.openapitestgenerator.dto;

import java.util.List;

public record Expected(int status, String uiAssertText, List<String> assertions) {}
