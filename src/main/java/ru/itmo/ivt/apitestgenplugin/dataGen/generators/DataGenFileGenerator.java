package ru.itmo.ivt.apitestgenplugin.dataGen.generators;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;

import java.util.Properties;
import java.util.stream.Collectors;

import static ru.itmo.ivt.apitestgenplugin.util.FileUtils.createFile;
import static ru.itmo.ivt.apitestgenplugin.util.StringUtils.mapToString;

public class DataGenFileGenerator {
    public static void createModelGeneratorFile(Project project,
                                            PsiDirectory directory,
                                            String packageName,
                                            DataGenMethodGenerator manager) {
        try {
            FileTemplate template = FileTemplateManager
                    .getInstance(project)
                    .getInternalTemplate("DataGeneratorTemplate.java");

            Properties properties = new Properties();
            properties.setProperty("PACKAGE_NAME", packageName);
            properties.setProperty("IMPORTS", String.join("\n", manager.getDataGenImports()));
            properties.setProperty("MODEL", manager.modelName());
            properties.setProperty("METHODS", mapToString(manager.generateMethods().stream()
                    .map(m -> m.name() + "=" + m.value())
                    .collect(Collectors.toSet())));

            String fileContent = template.getText(properties);
            createFile(project, fileContent, directory, manager.modelName() + "Generator.java", JavaFileType.INSTANCE);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create config file", e);
        }
    }
}