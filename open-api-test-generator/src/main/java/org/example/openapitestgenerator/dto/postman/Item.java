package org.example.openapitestgenerator.dto.postman;


import java.util.List;

public record Item(
       String name,
       Request request,
       List<Event> event
){}
