package org.example.openapitestgenerator.dto.testcase;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public class InputData
{
    private final Map<String, Object> params = new HashMap<>();

    @JsonAnySetter
    public void add(String key, Object value)
    {
        params.put(key, value);
    }

    @JsonValue
    public Map<String, Object> getParams()
    {
        return params;
    }
}