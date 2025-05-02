package ru.itmo.ivt.apitestgenplugin.codeGen;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import io.swagger.v3.oas.models.Operation;
import ru.itmo.ivt.apitestgenplugin.model.GenerationContext;
import ru.itmo.ivt.apitestgenplugin.model.PathItemWithEndPoint;

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

            return WriteCommandAction.writeCommandAction(project)
                    .compute(() -> {
                        PsiFileFactory factory = PsiFileFactory.getInstance(project);
                        String fileName = operationName + "Test.java";
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
            for (PathItemWithEndPoint path : paths) {
                Map<String, Operation> operations = getOperations(path.pathItem());
                operations.forEach((method, op) -> {
                    String operationName = op.getOperationId();
                    String pathName = camelToSnakeCase(operationName);
                    pathsWithNames.put(pathName, path.path());
                    operationsWithPathAndMethod.put(pathName, operationName + " " + method);
                    methodNames.add(operationName);
                });
            }

            Properties properties = new Properties();
            properties.setProperty("PACKAGE_NAME", DEFAULT_CLIENTS_PACKAGE);
            properties.setProperty("CONTROLLER_NAME", clientName);
            properties.setProperty("END_POINTS", mapToString(pathsWithNames));
            properties.setProperty("OPERATIONS", mapToString(operationsWithPathAndMethod));

            String fileContent = template.getText(properties);

            PsiFile createdFile = WriteCommandAction.writeCommandAction(context.getProject())
                    .compute(() -> {
                                PsiFileFactory factory = PsiFileFactory.getInstance(context.getProject());
                                String fileName = clientName + ".java";
                                PsiFile psiFile = factory.createFileFromText(
                                        fileName,
                                        JavaFileType.INSTANCE,
                                        fileContent
                                );
                                PsiFile prevFile = outputDirectory.findFile(fileName);
                                if (prevFile != null) {
                                    return prevFile;
                                }
                                return (PsiFile) outputDirectory.add(psiFile);
                            });
            context.getClientFiles().put(clientName, createdFile);
            return methodNames;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate API clients", e);
        }
    }
}