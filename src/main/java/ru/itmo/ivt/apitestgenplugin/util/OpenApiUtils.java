package ru.itmo.ivt.apitestgenplugin.util;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import lombok.experimental.UtilityClass;
import ru.itmo.ivt.apitestgenplugin.model.openapi.ApiMethodParam;
import ru.itmo.ivt.apitestgenplugin.model.openapi.PathItemWithEndPoint;

import java.util.*;
import java.util.stream.Collectors;

import static ru.itmo.ivt.apitestgenplugin.util.SchemaUtils.extractObjectTypeFromBody;
import static ru.itmo.ivt.apitestgenplugin.util.SchemaUtils.mapSchemaToJavaType;

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

    public static Map<String, List<PathItemWithEndPoint>> groupPathsByTags(OpenAPI openAPI) {
        Map<String, List<PathItemWithEndPoint>> result = new HashMap<>();

        openAPI.getPaths().forEach((path, pathItem) -> {
            PathItemWithEndPoint pathItemWithEndPoint = new PathItemWithEndPoint(pathItem, path);
            Map<String, Operation> operations = getOperations(pathItem);
            operations.values().forEach(op -> {
                if (op.getTags() != null && !op.getTags().isEmpty()) {
                    String tag = op.getTags().get(0);
                    result.computeIfAbsent(tag, k -> new ArrayList<>()).add(pathItemWithEndPoint);
                } else {
                    result.computeIfAbsent("Default", k -> new ArrayList<>()).add(pathItemWithEndPoint);
                }
            });
        });
        return result;
    }

    public static Map<String, List<String>> getModelsByControllers(Paths paths) {
        return paths.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getValue().readOperations().stream()
                                .map(Operation::getTags)
                                .filter(Objects::nonNull)
                                .flatMap(List::stream)
                                .findFirst()
                                .orElse("common"),
                        entry -> entry.getValue().readOperations().stream()
                                .flatMap(op -> extractSchemasFromOperation(op).stream())
                                .distinct()
                                .toList(),
                        (existing, replacement) -> {
                            // Объединяем списки для дубликатов тегов
                            List<String> merged = new ArrayList<>(existing);
                            merged.addAll(replacement);
                            return merged.stream().distinct().toList();
                        }
                ));
    }

    public static Map<String, Schema> getModelsByNames (OpenAPI openAPI) {
        if (openAPI == null || openAPI.getComponents() == null || openAPI.getComponents().getSchemas() == null) {
            return Map.of();
        }
        return openAPI.getComponents().getSchemas();
    }

    public static List<String> extractSchemasFromOperation(Operation operation) {
        List<String> schemas = new ArrayList<>();

        if (operation.getRequestBody() != null
                && operation.getRequestBody().getContent() != null) {
            operation.getRequestBody().getContent().values().stream()
                    .map(MediaType::getSchema)
                    .filter(Objects::nonNull)
                    .map(OpenApiUtils::extractSchemaFromSchema)
                    .forEach(schemas::addAll);
        }

        if (operation.getResponses() != null) {
            operation.getResponses().values().stream()
                    .map(ApiResponse::getContent)
                    .filter(Objects::nonNull)
                    .flatMap(content -> content.values().stream())
                    .map(MediaType::getSchema)
                    .filter(Objects::nonNull)
                    .map(OpenApiUtils::extractSchemaFromSchema)
                    .forEach(schemas::addAll);
        }

        return schemas.stream().distinct().toList();
    }

    public static List<String> extractSchemaFromSchema(Schema schema) {
        List<String> schemas = new ArrayList<>();

        if (schema.get$ref() != null && (schema.get$ref().startsWith("#/components/schemas/"))) {
            schemas.add(schema.get$ref().substring("#/components/schemas/".length()));
        }

        if (schema.get$ref() != null && (schema.get$ref().startsWith("#/$defs/"))) {
            schemas.add(schema.get$ref().substring("#/$defs/".length()));
        }

        if (schema instanceof ArraySchema) {
            Schema items = ((ArraySchema) schema).getItems();
            if (items != null) {
                schemas.addAll(extractSchemaFromSchema(items));
            }
        }

        return schemas;
    }

    public static List<ApiMethodParam> extractParamsForOperation(Operation operation, String modelsPackage) {
        List<ApiMethodParam> params = new ArrayList<>();
        if (operation.getParameters() != null) {
            operation.getParameters().stream()
                    .filter(p -> "path".equals(p.getIn()))
                    .forEach(p -> params.add(new ApiMethodParam(
                            mapSchemaToJavaType(p.getSchema(), modelsPackage),
                            p.getName(),
                            true, false, false
                    )));
            operation.getParameters().stream()
                    .filter(p -> "query".equals(p.getIn()))
                    .forEach(p -> params.add(new ApiMethodParam(
                            mapSchemaToJavaType(p.getSchema(), modelsPackage),
                            p.getName(),
                            false, true, false
                    )));
        }
        if (operation.getRequestBody() != null) {
            params.add(new ApiMethodParam(
                    extractObjectTypeFromBody(operation.getRequestBody(), modelsPackage),
                    "requestBody",
                    false, false, true
            ));
        }

        return params;
    }
}
