package ru.itmo.ivt.apitestgenplugin.codeGen.generator.impl;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import io.swagger.v3.oas.models.Operation;
import ru.itmo.ivt.apitestgenplugin.codeGen.generator.CodeGenerator;
import ru.itmo.ivt.apitestgenplugin.model.GenerationContext;

import java.util.*;

import static ru.itmo.ivt.apitestgenplugin.codeGen.generator.util.TestFileUtil.generateTestNames;
import static ru.itmo.ivt.apitestgenplugin.util.FileUtils.*;
import static ru.itmo.ivt.apitestgenplugin.util.OpenApiUtils.getOperations;
import static ru.itmo.ivt.apitestgenplugin.util.StringUtils.*;

public class TestFilesGenerator implements CodeGenerator {
    private static final String DEFAULT_CLIENTS_PATH = "test/java";

    @Override
    public void generateCode(PsiDirectory directory, GenerationContext context) {
        assert context.getClientFiles() != null;
        PsiDirectory testDir = createNestedDirectories(context.getProject(), directory, DEFAULT_CLIENTS_PATH);
        List<PsiFile> createdFiles = new ArrayList<>();
        context.getOpenAPI().getPaths().forEach((path, pathItem) -> {
            Map<String, Operation> operations = getOperations(pathItem);
            operations.forEach((httpMethod, operation) -> {
                String controllerName = kebabToSnakeCase(getControllerName(operation));
                String operationName = getOperationName(operation, httpMethod);
                PsiDirectory controllerDir = createSubDirectory(context.getProject(), testDir, controllerName);
                if (controllerDir == null) return;
                List<String> tests = generateTestNames(operation);
                createdFiles.add(createTestClassFile(
                        context.getProject(),
                        controllerDir,
                        controllerName,
                        operationName,
                        tests
                ));
            });
        });
        context.setTestFiles(createdFiles);
    }

    private PsiFile createTestClassFile(Project project,
                                        PsiDirectory directory,
                                        String packageName,
                                        String operationName,
                                        List<String> tests) {
        try {
            FileTemplate template = FileTemplateManager
                    .getInstance(project)
                    .getInternalTemplate("TestClassTemplate.java");

            StringBuilder testsContent = new StringBuilder();
            tests.forEach(test -> testsContent.append(test).append("\n"));
            Properties properties = new Properties();
            properties.setProperty("PACKAGE_NAME", packageName != null ? packageName : "");
            properties.setProperty("OPERATION_NAME", operationName != null ? operationName : "Unknown");
            properties.setProperty("TESTS", testsContent.toString());

            String fileContent = template.getText(properties);
            return createFile(project, fileContent, directory, operationName + "Test.java", JavaFileType.INSTANCE);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create test file", e);
        }
    }
}