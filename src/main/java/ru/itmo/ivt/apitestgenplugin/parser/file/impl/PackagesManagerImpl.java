package ru.itmo.ivt.apitestgenplugin.parser.file.impl;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
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

import static ru.itmo.ivt.apitestgenplugin.util.FileUtils.fixImports;

public class PackagesManagerImpl implements PackagesManager {
    @Override
    public List<File> splitModelFilesByDirectories(Map<String, List<String>> modelsByControllers, String directory, Project project) {
        File baseDir = new File(directory);
        if (!baseDir.exists() || !baseDir.isDirectory()) {
            throw new IllegalArgumentException("Base directory does not exist or is not a directory: " + directory);
        }

        String basePackage = getBasePackage(baseDir);
        List<File> createdFiles = new ArrayList<>();

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
                        updatePackageInFile(destFile, basePackage, controllerName);
                        createdFiles.add(destFile);
                        System.out.println("Moved " + sourceFile.getName() + " to " + controllerDir.getName());
                    } catch (IOException e) {
                        System.err.println("Failed to move file " + sourceFile.getName() + ": " + e.getMessage());
                    }
                } else {
                    System.err.println("Source file not found: " + sourceFile.getAbsolutePath());
                }
            }
        }
        if (createdFiles.isEmpty()) {
            System.out.println("No files were created or moved.");
        } else {
            for (File file : createdFiles) {
                fixImports(project, VfsUtil.findFileByIoFile(file, true));
            }
            System.out.println("Total files created/moved: " + createdFiles.size());
        }
        return createdFiles;
    }

    private String getBasePackage(File baseDir) {
        File[] files = baseDir.listFiles((dir, name) -> name.endsWith(".java"));
        if (files != null && files.length > 0) {
            try {
                String content = Files.readString(files[0].toPath());
                int packageStart = content.indexOf("package ");
                if (packageStart >= 0) {
                    int packageEnd = content.indexOf(";", packageStart);
                    if (packageEnd > packageStart) {
                        return content.substring(packageStart + 8, packageEnd).trim();
                    }
                }
            } catch (IOException e) {
                System.err.println("Failed to read file to determine base package: " + e.getMessage());
            }
        }
        return "";
    }

    private void updatePackageInFile(File file, String basePackage, String controllerName) throws IOException {
        String content = Files.readString(file.toPath());
        String newPackage = basePackage.isEmpty() ? controllerName : basePackage + "." + controllerName;
        content = content.replaceFirst("package\\s+.*?;", "package " + newPackage + ";");
        Files.writeString(file.toPath(), content);
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
}
