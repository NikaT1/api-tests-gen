package ru.itmo.ivt.apitestgenplugin.codeGen.generator.util;

import io.swagger.v3.oas.models.Operation;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class TestFileUtil {
    public static List<String> generateTestNames(Operation operation) {
        List<String> testNames = new ArrayList<>();
        if (operation.getResponses() != null) {
            operation.getResponses().forEach((responseCode, apiResponse) -> {
                String testType = responseCode.startsWith("2") ? "happyPath" : "negativePath";
                String testName = String.format("%s_%s_shouldReturn%s",
                        operation.getOperationId(),
                        testType,
                        responseCode);
                testNames.add(testName);
            });
        }
        // обязательные тесты
        if (testNames.isEmpty()) {
            testNames.add(operation.getOperationId() + "_happyPath_shouldReturn200");
            testNames.add(operation.getOperationId() + "_negativePath_shouldReturn400");
        }
        return testNames;
    }
}
