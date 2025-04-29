package ru.itmo.ivt.apitestgenplugin.util;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static ru.itmo.ivt.apitestgenplugin.util.FileUtils.capitalize;

@UtilityClass
public class OpenApiUtils {
    public static Map<String, Operation> getOperations(PathItem pathItem) {
        Map<String, Operation> operations = new LinkedHashMap<>();
        if (pathItem.getGet() != null) operations.put("get", pathItem.getGet());
        if (pathItem.getPost() != null) operations.put("post", pathItem.getPost());
        if (pathItem.getPut() != null) operations.put("put", pathItem.getPut());
        if (pathItem.getDelete() != null) operations.put("delete", pathItem.getDelete());
        if (pathItem.getPatch() != null) operations.put("patch", pathItem.getPatch());
        return operations;
    }

    public static String getControllerName(Operation operation) {
        if (operation.getTags() != null && !operation.getTags().isEmpty()) {
            return operation.getTags().get(0).replaceAll("[^a-zA-Z0-9]", "");
        }
        return "DefaultController";
    }

    public static String getOperationName(Operation operation, String httpMethod) {
        if (operation.getOperationId() != null) {
            return capitalize(operation.getOperationId().replaceAll("[^a-zA-Z0-9]", ""));
        }
        return capitalize(httpMethod) + "Operation";
    }

    public static String extractHttpMethod(String operation) {
        return operation.split(" ")[0].toLowerCase();
    }

    public static String extractPath(String endPoint) {
        return endPoint.split(" ")[1];
    }

    public static String extractMethodName(String operation) {
        return operation.split("=")[1];
    }

    public static void processPathItem(PathItem pathItem, StringBuilder endPoints, StringBuilder operations) {
        processOperation(pathItem.getGet(), "GET", endPoints, operations);
        processOperation(pathItem.getPost(), "POST", endPoints, operations);
        processOperation(pathItem.getPut(), "PUT", endPoints, operations);
        processOperation(pathItem.getDelete(), "DELETE", endPoints, operations);
        processOperation(pathItem.getPatch(), "PATCH", endPoints, operations);
    }

    public static void processOperation(Operation operation, String method,
                                         StringBuilder endPoints, StringBuilder operations) {
        if (operation != null) {
            String path = operation.getOperationId();
            String methodName = convertToMethodName(operation.getOperationId(), method);

            endPoints.append(method).append(" ").append(path).append("\n");

            operations.append(method)
                    .append(" ")
                    .append(path)
                    .append("=")
                    .append(methodName)
                    .append("\n");
        }
    }

    public static String convertToMethodName(String operationId, String httpMethod) {
        if (operationId == null || operationId.isEmpty()) {
            return httpMethod.toLowerCase() + "Request";
        }

        return Arrays.stream(operationId.split("[^a-zA-Z0-9]"))
                .filter(word -> !word.isEmpty())
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .reduce("", String::concat)
                .replaceFirst("^.", String.valueOf(Character.toLowerCase(operationId.charAt(0))));
    }
}
