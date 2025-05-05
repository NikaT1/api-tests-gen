package ru.itmo.ivt.apitestgenplugin.configGen.generator.impl;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import ru.itmo.ivt.apitestgenplugin.configGen.generator.ConfigGenerator;
import ru.itmo.ivt.apitestgenplugin.model.GenerationContext;

import java.util.Properties;

import static ru.itmo.ivt.apitestgenplugin.util.FileUtils.createFile;
import static ru.itmo.ivt.apitestgenplugin.util.FileUtils.createNestedDirectories;

public class PropertiesManagerFileGenerator implements ConfigGenerator {
    private static final String DEFAULT_PROPERTY_MANAGER_NAME = "TestPropertiesManager.java";
    private static final String DEFAULT_MANAGER_PACKAGE = "main/resources/java/manager";

    @Override
    public void prepareConfigFiles(GenerationContext context, PsiDirectory srcDir) {
        Project project = context.getProject();
        PsiDirectory managerDir = createNestedDirectories(project, srcDir, DEFAULT_MANAGER_PACKAGE);
        createPropertiesManagerFile(project, managerDir, DEFAULT_MANAGER_PACKAGE);
    }

    private void createPropertiesManagerFile(Project project,
                                             PsiDirectory directory,
                                             String packageName) {
        try {
            FileTemplate template = FileTemplateManager
                    .getInstance(project)
                    .getInternalTemplate("PropertiesHelperTemplate.java");

            Properties properties = new Properties();
            properties.setProperty("PACKAGE_NAME", packageName != null ? packageName : "");

            String fileContent = template.getText(properties);
            createFile(project, fileContent, directory, DEFAULT_PROPERTY_MANAGER_NAME, JavaFileType.INSTANCE);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create config file", e);
        }
    }
}