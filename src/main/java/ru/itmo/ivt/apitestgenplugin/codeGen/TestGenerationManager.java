package ru.itmo.ivt.apitestgenplugin.codeGen;

import com.intellij.psi.PsiDirectory;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import ru.itmo.ivt.apitestgenplugin.GenerationContext;

import java.util.*;

import static ru.itmo.ivt.apitestgenplugin.codeGen.TestFileGenerator.createClientClassFile;
import static ru.itmo.ivt.apitestgenplugin.util.FileUtils.createSubDirectory;
import static ru.itmo.ivt.apitestgenplugin.util.FileUtils.getPackageName;
import static ru.itmo.ivt.apitestgenplugin.util.OpenApiUtils.*;

public class TestGenerationManager {
    public void generateClientsAndTests(PsiDirectory srcDir, GenerationContext context) {
        PsiDirectory mainDir = createSubDirectory(context.getProject(), srcDir, "main");
        generateClientClasses(mainDir, context);
        generateTestClasses(srcDir, context);
    }

    private void generateClientClasses(PsiDirectory mainDirectory, GenerationContext context) {
        assert context.getClientFiles() != null;
        assert context.getModelFiles() != null;
        assert context.getOpenAPI() != null;

        if (context.getOpenAPI().getPaths() == null) return;

        Map<String, List<PathItem>> controllers = groupPathsByTags(context.getOpenAPI());
        PsiDirectory clientsDir = createSubDirectory(context.getProject(), mainDirectory, "clients");

        for (Map.Entry<String, List<PathItem>> entry : controllers.entrySet()) {
            createClientClassFile(context.getProject(),
                    clientsDir,
                    entry.getKey(),
                    entry.getValue());
        }
    }

    private void generateTestClasses(PsiDirectory srcDir, GenerationContext context) {
        assert context.getClientFiles() != null;
        assert context.getModelFiles() != null;
        assert context.getOpenAPI() != null;

        if (context.getOpenAPI().getPaths() == null) return;

        PsiDirectory testDir = createSubDirectory(context.getProject(), srcDir, "test");

        context.getOpenAPI().getPaths().forEach((path, pathItem) -> {
            Map<String, Operation> operations = getOperations(pathItem);

            operations.forEach((httpMethod, operation) -> {
                String controllerName = getControllerName(operation);
                String operationName = getOperationName(operation, httpMethod);

                PsiDirectory controllerDir = createSubDirectory(context.getProject(), testDir, controllerName);
                if (controllerDir == null) return;

                List<String> tests = generateTestNames(operation);

                TestFileGenerator.createTestClassFile(
                        context.getProject(),
                        controllerDir,
                        getPackageName(testDir, controllerName),
                        operationName,
                        tests
                );
            });
        });
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

        if (testNames.isEmpty()) {
            testNames.add(operation.getOperationId() + "_happyPath_shouldReturn200");
            testNames.add(operation.getOperationId() + "_negativePath_shouldReturn400");
        }

        return testNames;
    }

    private static Map<String, List<PathItem>> groupPathsByTags(OpenAPI openAPI) {
        Map<String, List<PathItem>> result = new HashMap<>();

        openAPI.getPaths().forEach((path, pathItem) -> {
            List<Operation> operations = new ArrayList<>();
            if (pathItem.getGet() != null) operations.add(pathItem.getGet());
            if (pathItem.getPost() != null) operations.add(pathItem.getPost());
            if (pathItem.getPut() != null) operations.add(pathItem.getPut());
            if (pathItem.getDelete() != null) operations.add(pathItem.getDelete());
            if (pathItem.getPatch() != null) operations.add(pathItem.getPatch());

            operations.forEach(op -> {
                if (op.getTags() != null && !op.getTags().isEmpty()) {
                    String tag = op.getTags().get(0);
                    result.computeIfAbsent(tag, k -> new ArrayList<>()).add(pathItem);
                } else {
                    result.computeIfAbsent("Default", k -> new ArrayList<>()).add(pathItem);
                }
            });
        });

        return result;
    }
}
