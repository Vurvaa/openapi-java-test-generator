package org.example.openapitestgenerator.dto.postman;

import java.util.List;

public record Script(
        String type,
        List<String> exec
){}
