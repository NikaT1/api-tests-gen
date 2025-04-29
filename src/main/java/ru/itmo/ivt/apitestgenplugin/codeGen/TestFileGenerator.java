package ru.itmo.ivt.apitestgenplugin.codeGen;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import io.swagger.v3.oas.models.PathItem;

import java.util.*;

import static ru.itmo.ivt.apitestgenplugin.util.FileUtils.getPackageName;
import static ru.itmo.ivt.apitestgenplugin.util.OpenApiUtils.processPathItem;

public class TestFileGenerator {

    public static PsiFile createTestClassFile(Project project,
                                              PsiDirectory directory,
                                              String packageName,
                                              String operationName,
                                              List<String> tests) {
        try {
            FileTemplate template = FileTemplateManager
                    .getInstance(project)
                    .getInternalTemplate("TestClassTemplate.java");

            Properties properties = new Properties();
            properties.setProperty("PACKAGE_NAME", packageName != null ? packageName : "");
            properties.setProperty("OPERATION_NAME", operationName != null ? operationName : "Unknown");

            StringBuilder testsContent = new StringBuilder();
            tests.forEach(test -> testsContent.append(test).append("\n"));
            properties.setProperty("TESTS", testsContent.toString());

            String fileContent = template.getText(properties);

            PsiFileFactory factory = PsiFileFactory.getInstance(project);
            String fileName = operationName + "Test.java";
            PsiFile psiFile = factory.createFileFromText(
                    fileName,
                    JavaFileType.INSTANCE,
                    fileContent
            );

            PsiFile createdFile = (PsiFile) directory.add(psiFile);
            JavaCodeStyleManager.getInstance(project)
                    .optimizeImports(createdFile);
            return createdFile;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create test file", e);
        }
    }

    public static void createClientClassFile(Project project,
                                             PsiDirectory outputDirectory,
                                             String controllerName,
                                             List<PathItem> paths) {
        try {
            FileTemplate template = FileTemplateManager
                    .getInstance(project)
                    .getInternalTemplate("ClientClassTemplate.java");
            String clientName = controllerName + "Client";

            Properties properties = new Properties();
            properties.setProperty("PACKAGE_NAME", getPackageName(outputDirectory, clientName));
            properties.setProperty("CONTROLLER_NAME", clientName);

            StringBuilder endPoints = new StringBuilder();
            StringBuilder operations = new StringBuilder();

            for (PathItem pathItem : paths) {
                processPathItem(pathItem, endPoints, operations);
            }

            properties.setProperty("END_POINTS", endPoints.toString());
            properties.setProperty("OPERATIONS", operations.toString());

            String fileContent = template.getText(properties);

            PsiFileFactory factory = PsiFileFactory.getInstance(project);
            String fileName = clientName + ".java";
            PsiFile psiFile = factory.createFileFromText(
                    fileName,
                    JavaFileType.INSTANCE,
                    fileContent
            );

            PsiFile createdFile = (PsiFile) outputDirectory.add(psiFile);
            JavaCodeStyleManager.getInstance(project)
                    .optimizeImports(createdFile);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate API clients", e);
        }
    }
}