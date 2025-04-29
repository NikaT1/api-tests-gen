package ru.itmo.ivt.apitestgenplugin.codeGen;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;

import java.util.*;

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
}