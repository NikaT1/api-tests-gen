package ru.itmo.ivt.apitestgenplugin.dataGen.generators;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;

import static ru.itmo.ivt.apitestgenplugin.util.FileUtils.createFile;

public class PrimitiveDataGenFileGenerator {
    public static void createPrimitiveGeneratorFile(Project project, PsiDirectory directory) {
        try {
            FileTemplate template = FileTemplateManager
                    .getInstance(project)
                    .getInternalTemplate("PrimitiveGeneratorTemplate.java");

            String fileContent = template.getText();
            createFile(project, fileContent, directory, "PrimitiveDataGenerator.java", JavaFileType.INSTANCE);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create generator file", e);
        }
    }
}