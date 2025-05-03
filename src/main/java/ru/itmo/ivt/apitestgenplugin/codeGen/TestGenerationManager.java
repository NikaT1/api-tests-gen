package ru.itmo.ivt.apitestgenplugin.codeGen;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import io.swagger.v3.oas.models.Operation;
import ru.itmo.ivt.apitestgenplugin.model.GenerationContext;
import ru.itmo.ivt.apitestgenplugin.model.openapi.PathItemWithEndPoint;

import java.util.*;

import static ru.itmo.ivt.apitestgenplugin.codeGen.TestFileGenerator.createClientClassFile;
import static ru.itmo.ivt.apitestgenplugin.util.FileUtils.createSubDirectory;
import static ru.itmo.ivt.apitestgenplugin.util.OpenApiUtils.*;
import static ru.itmo.ivt.apitestgenplugin.util.StringUtils.*;

public class TestGenerationManager {
    public void generateClientsAndTests(PsiDirectory srcDir, GenerationContext context) {
        assert context.getModelFilesByPackages() != null;
        assert context.getOpenAPI() != null;
        assert context.getOpenAPI().getPaths() != null;

        PsiDirectory mainDir = createSubDirectory(context.getProject(), srcDir, "main");
        PsiDirectory javaDir = createSubDirectory(context.getProject(), mainDir, "java");
        PsiDirectory testMainDir = createSubDirectory(context.getProject(), srcDir, "test");
        PsiDirectory testDir = createSubDirectory(context.getProject(), testMainDir, "java");

        generateClientClasses(javaDir, context);
        generateTestClasses(testDir, context);
    }

    private void generateClientClasses(PsiDirectory mainDirectory, GenerationContext context) {
        Map<String, List<PathItemWithEndPoint>> controllers = groupPathsByTags(context.getOpenAPI());
        PsiDirectory clientsDir = createSubDirectory(context.getProject(), mainDirectory, "clients");

        Map<String, List<String>> methodNamesByClients = new HashMap<>();
        for (Map.Entry<String, List<PathItemWithEndPoint>> entry : controllers.entrySet()) {
            List<String> methodNames = createClientClassFile(context,
                    clientsDir,
                    entry.getKey(),
                    entry.getValue());
            methodNamesByClients.put(entry.getKey(), methodNames);
        }
        context.setMethodNamesByClients(methodNamesByClients);
    }

    private void generateTestClasses(PsiDirectory testDir, GenerationContext context) {
        assert context.getClientFiles() != null;

        List<PsiFile> createdFiles = new ArrayList<>();
        context.getOpenAPI().getPaths().forEach((path, pathItem) -> {
            Map<String, Operation> operations = getOperations(pathItem);

            operations.forEach((httpMethod, operation) -> {
                String controllerName = kebabToSnakeCase(getControllerName(operation));
                String operationName = getOperationName(operation, httpMethod);

                PsiDirectory controllerDir = createSubDirectory(context.getProject(), testDir, controllerName);
                if (controllerDir == null) return;

                List<String> tests = generateTestNames(operation);

                createdFiles.add(TestFileGenerator.createTestClassFile(
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

    private List<String> generateTestNames(Operation operation) {
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
