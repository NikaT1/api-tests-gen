package ru.itmo.ivt.apitestgenplugin.codeGen;

import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import io.swagger.v3.oas.models.Operation;
import lombok.RequiredArgsConstructor;
import ru.itmo.ivt.apitestgenplugin.GenerationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ru.itmo.ivt.apitestgenplugin.util.FileUtils.createControllerDirectory;
import static ru.itmo.ivt.apitestgenplugin.util.FileUtils.getPackageName;
import static ru.itmo.ivt.apitestgenplugin.util.OpenApiUtils.*;

@RequiredArgsConstructor
public class TestGenerationManager {
    private final GenerationContext context;

    public void generateClientsAndTests() {
        PsiDirectory testDir = prepareDirForTests();
        generateClientClasses();
        generateTestClasses(testDir);
    }

    private void generateClientClasses() {
        return;
    }

    private void generateTestClasses(PsiDirectory baseTestDir) {
        assert context.getClientFiles() != null;
        assert context.getModelFiles() != null;
        assert context.getOpenAPI() != null;

        if (context.getOpenAPI().getPaths() == null) return;

        context.getOpenAPI().getPaths().forEach((path, pathItem) -> {
            Map<String, Operation> operations = getOperations(pathItem);

            operations.forEach((httpMethod, operation) -> {
                String controllerName = getControllerName(operation);
                String operationName = getOperationName(operation, httpMethod);

                PsiDirectory controllerDir = createControllerDirectory(context.getProject(), baseTestDir, controllerName);
                if (controllerDir == null) return;

                List<String> tests = generateTestNames(operation);

                TestFileGenerator.createTestClassFile(
                        context.getProject(),
                        controllerDir,
                        getPackageName(baseTestDir, controllerName),
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

    private PsiDirectory prepareDirForTests() {
        return PsiManager.getInstance(context.getProject())
                .findDirectory(
                        ProjectRootManager.getInstance(context.getProject())
                                .getContentSourceRoots()[0]
                )
                .createSubdirectory("test");
    }
}
