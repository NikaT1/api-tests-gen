package ru.itmo.ivt.apitestgenplugin.codeGen;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import io.swagger.v3.oas.models.Operation;
import ru.itmo.ivt.apitestgenplugin.model.GenerationContext;
import ru.itmo.ivt.apitestgenplugin.model.openapi.ApiMethodParam;
import ru.itmo.ivt.apitestgenplugin.model.openapi.PathItemWithEndPoint;

import java.util.*;
import static ru.itmo.ivt.apitestgenplugin.util.OpenApiUtils.*;
import static ru.itmo.ivt.apitestgenplugin.util.StringUtils.*;

public class TestFileGenerator {
    private static final String DEFAULT_CLIENTS_PACKAGE = "clients";

    public static PsiFile createTestClassFile(Project project,
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

            return createFile(project, operationName + "Test.java", fileContent, directory);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create test file", e);
        }
    }

    public static List<String> createClientClassFile(GenerationContext context,
                                                     PsiDirectory outputDirectory,
                                                     String controllerName,
                                                     List<PathItemWithEndPoint> paths) {
        try {
            FileTemplate template = FileTemplateManager
                    .getInstance(context.getProject())
                    .getInternalTemplate("ClientClassTemplate.java");
            String clientName = kebabToCamelCase(controllerName) + "Client";

            List<String> methodNames = new ArrayList<>();
            Map<String, String> pathsWithNames = new HashMap<>();
            Map<String, String> operationsWithPathAndMethod = new HashMap<>();
            Set<String> imports = new HashSet<>();
            for (PathItemWithEndPoint path : paths) {
                Map<String, Operation> operations = getOperations(path.pathItem());
                operations.forEach((method, op) -> {
                    String operationName = op.getOperationId();
                    String pathName = camelToSnakeCase(operationName);
                    pathsWithNames.put(pathName, path.path());
                    List<ApiMethodParam> params = extractParamsForOperation(op, "models");
                    imports.addAll(params.stream().flatMap(p -> p.type().importPath().stream()).toList());
                    operationsWithPathAndMethod.put(pathName, getMethodInfo(params, method, operationName));
                    methodNames.add(operationName);
                });
            }

            Properties properties = new Properties();
            properties.setProperty("PACKAGE_NAME", DEFAULT_CLIENTS_PACKAGE);
            properties.setProperty("IMPORTS", mapToString(imports));
            properties.setProperty("CONTROLLER_NAME", clientName);
            properties.setProperty("END_POINTS", mapToString(pathsWithNames));
            properties.setProperty("OPERATIONS", mapToString(operationsWithPathAndMethod));

            String fileContent = template.getText(properties);
            PsiFile createdFile = createFile(context.getProject(), clientName + ".java", fileContent, outputDirectory);
            context.getClientFiles().put(clientName, createdFile);
            return methodNames;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate API clients", e);
        }
    }

    private static PsiFile createFile(Project project, String fileName, String fileContent, PsiDirectory directory) {
        return WriteCommandAction.writeCommandAction(project)
                .compute(() -> {
                    PsiFileFactory factory = PsiFileFactory.getInstance(project);
                    PsiFile psiFile = factory.createFileFromText(
                            fileName,
                            JavaFileType.INSTANCE,
                            fileContent
                    );
                    PsiFile prevFile = directory.findFile(fileName);
                    if (prevFile != null) {
                        return prevFile;
                    }
                    return (PsiFile) directory.add(psiFile);
                });
    }
}