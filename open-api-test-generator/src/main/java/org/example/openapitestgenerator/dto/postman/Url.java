package org.example.openapitestgenerator.dto.postman;

import java.util.List;

public record Url(
        String raw,
        List<String> host,
        List<String> path
) {}