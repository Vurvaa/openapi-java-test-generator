package org.example.openapitestgenerator.dto.zephyr;

import java.util.List;

public record TestScript(
        String type,
        List<Step> steps
) {}
