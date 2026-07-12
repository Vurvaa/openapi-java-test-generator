package org.example.openapitestgenerator.dto.postman;


import java.util.List;

public record PostmanCollection(
        Info info,
        List<Item> item
) {}
