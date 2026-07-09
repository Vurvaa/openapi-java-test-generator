package org.example.openapitestgenerator.service;

import org.example.openapitestgenerator.dto.testcase.Expected;
import org.example.openapitestgenerator.dto.postman.Event;
import org.example.openapitestgenerator.dto.postman.Script;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PostmanScriptGenerator {

    private static final String TEST_EVENT = "test";
    private static final String SCRIPT_TYPE = "text/javascript";

    public List<Event> generate(Expected expected) {
        if (expected == null) {
            return List.of();
        }

        List<String> scriptLines = new ArrayList<>();

        addStatusAssertion(scriptLines, expected.status());
        addBodyAssertions(scriptLines, expected.assertions());

        return List.of(
                new Event(
                        TEST_EVENT,
                        new Script(SCRIPT_TYPE, scriptLines)
                )
        );
    }

    private void addStatusAssertion(List<String> scriptLines, int status) {
        scriptLines.add("pm.test(\"Status code is " + status + "\", function () {");
        scriptLines.add("    pm.response.to.have.status(" + status + ");");
        scriptLines.add("});");
    }

    private void addBodyAssertions(List<String> scriptLines, List<String> assertions) {
        if (assertions == null || assertions.isEmpty()) {
            return;
        }

        scriptLines.add("");
        scriptLines.add("let jsonData = {};");

        scriptLines.add("try {");
        scriptLines.add("    jsonData = pm.response.json();");
        scriptLines.add("} catch (e) {");
        scriptLines.add("    console.warn(\"Response body is not valid JSON\");");
        scriptLines.add("}");

        for (String assertion : assertions) {
            addBodyAssertion(scriptLines, assertion);
        }
    }

    private void addBodyAssertion(List<String> scriptLines, String assertion) {
        scriptLines.add("");
        scriptLines.add("pm.test(\"" + escapeForJavaScript(assertion) + "\", function () {");

        if (assertion.startsWith("response.body.") && assertion.endsWith("!= null")) {
            String field = assertion
                    .replace("response.body.", "")
                    .replace("!= null", "")
                    .trim();

            scriptLines.add("    pm.expect(jsonData." + field + ").to.not.be.null;");
            scriptLines.add("    pm.expect(jsonData." + field + ").to.not.be.undefined;");
        } else {
            scriptLines.add("    // Unsupported assertion format: " + escapeForJavaScript(assertion));
        }

        scriptLines.add("});");
    }

    private String escapeForJavaScript(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}