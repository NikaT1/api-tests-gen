package ru.itmo.ivt.apitestgenplugin.codeGen.generator.impl;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import io.swagger.v3.oas.models.Operation;
import ru.itmo.ivt.apitestgenplugin.codeGen.generator.CodeGenerator;
import ru.itmo.ivt.apitestgenplugin.model.GenerationContext;
import ru.itmo.ivt.apitestgenplugin.model.openapi.ApiMethodParam;
import ru.itmo.ivt.apitestgenplugin.model.openapi.PathItemWithEndPoint;

import java.util.*;

import static ru.itmo.ivt.apitestgenplugin.util.FileUtils.*;
import static ru.itmo.ivt.apitestgenplugin.util.OpenApiUtils.*;
import static ru.itmo.ivt.apitestgenplugin.util.StringUtils.*;

public class ClientFilesGenerator implements CodeGenerator {
    private static final String DEFAULT_CLIENTS_PATH = "main/java/clients";
    private static final String DEFAULT_CLIENTS_PACKAGE = "clients";

    @Override
    public void generateCode(PsiDirectory directory, GenerationContext context) {
        Map<String, List<PathItemWithEndPoint>> controllers = groupPathsByTags(context.getOpenAPI());
        PsiDirectory clientsDir = createNestedDirectories(context.getProject(), directory, DEFAULT_CLIENTS_PATH);

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

    private List<String> createClientClassFile(GenerationContext context,
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
            PsiFile createdFile = createFile(context.getProject(), fileContent, outputDirectory, clientName + ".java", JavaFileType.INSTANCE);
            context.getClientFiles().put(clientName, createdFile);
            return methodNames;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate API clients", e);
        }
    }
}