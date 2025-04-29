package ru.itmo.ivt.apitestgenplugin.util;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import lombok.experimental.UtilityClass;

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
}
