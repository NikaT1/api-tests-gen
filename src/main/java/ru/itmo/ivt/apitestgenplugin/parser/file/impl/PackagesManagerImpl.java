package ru.itmo.ivt.apitestgenplugin.parser.file.impl;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.impl.VirtualFileImpl;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import ru.itmo.ivt.apitestgenplugin.parser.file.PackagesManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.itmo.ivt.apitestgenplugin.parser.file.ImportFixer.fixImports;

public class PackagesManagerImpl implements PackagesManager {
    @Override
    public void splitModelFilesByDirectories(Map<String, List<String>> modelsByControllers, String directory, Project project) {
        File baseDir = new File(directory);
        if (!baseDir.exists() || !baseDir.isDirectory()) {
            throw new IllegalArgumentException("Base directory does not exist or is not a directory: " + directory);
        }

        for (Map.Entry<String, List<String>> entry : modelsByControllers.entrySet()) {
            String controllerName = entry.getKey().replace("-", "_");
            List<String> modelNames = entry.getValue();

            File controllerDir = new File(baseDir, controllerName);
            if (!controllerDir.exists()) {
                if (!controllerDir.mkdir()) {
                    System.err.println("Failed to create directory: " + controllerDir.getAbsolutePath());
                    continue;
                }
            }

            for (String modelName : modelNames) {
                File sourceFile = new File(baseDir, modelName + ".java");
                File destFile = new File(controllerDir, modelName + ".java");

                if (sourceFile.exists()) {
                    try {
                        Files.move(
                                sourceFile.toPath(),
                                destFile.toPath(),
                                StandardCopyOption.REPLACE_EXISTING
                        );
                        System.out.println("Moved " + sourceFile.getName() + " to " + controllerDir.getName());
                        fixImports(project, VfsUtil.findFileByIoFile(destFile, true));
                    } catch (IOException e) {
                        System.err.println("Failed to move file " + sourceFile.getName() + ": " + e.getMessage());
                    }
                } else {
                    System.err.println("Source file not found: " + sourceFile.getAbsolutePath());
                }
            }
        }
    }

    @Override
    public void clearModelFile(File file) {
        file.delete();
    }

    @Override
    public Map<String, List<String>> getModelsByControllers(Paths paths) {
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

    private List<String> extractSchemasFromOperation(Operation operation) {
        List<String> schemas = new ArrayList<>();

        // Обрабатываем request body
        if (operation.getRequestBody() != null
                && operation.getRequestBody().getContent() != null) {
            operation.getRequestBody().getContent().values().stream()
                    .map(MediaType::getSchema)
                    .filter(Objects::nonNull)
                    .map(this::extractSchemaFromSchema)
                    .forEach(schemas::addAll);
        }

        // Обрабатываем responses
        if (operation.getResponses() != null) {
            operation.getResponses().values().stream()
                    .map(ApiResponse::getContent)
                    .filter(Objects::nonNull)
                    .flatMap(content -> content.values().stream())
                    .map(MediaType::getSchema)
                    .filter(Objects::nonNull)
                    .map(this::extractSchemaFromSchema)
                    .forEach(schemas::addAll);
        }

        return schemas.stream().distinct().toList();
    }

    private List<String> extractSchemaFromSchema(Schema schema) {
        List<String> schemas = new ArrayList<>();

        // Обрабатываем прямые ссылки
        if (schema.get$ref() != null && schema.get$ref().startsWith("#/components/schemas/")) {
            schemas.add(schema.get$ref().substring("#/components/schemas/".length()));
        }

        // Обрабатываем массивы
        if (schema instanceof ArraySchema) {
            Schema items = ((ArraySchema) schema).getItems();
            if (items != null) {
                schemas.addAll(extractSchemaFromSchema(items));
            }
        }

        return schemas;
    }
}
