package org.example.openapitestgenerator.dto.postman;

import java.util.List;

public record Request(
        String method,
        List<Header> header,
        Body body,
        Url url
){}
