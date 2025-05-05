package ru.itmo.ivt.apitestgenplugin.configGen.generator.impl;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.lang.properties.PropertiesFileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import ru.itmo.ivt.apitestgenplugin.configGen.generator.ConfigGenerator;
import ru.itmo.ivt.apitestgenplugin.model.GenerationContext;
import ru.itmo.ivt.apitestgenplugin.model.UserInput;
import ru.itmo.ivt.apitestgenplugin.model.enums.AuthType;

import java.util.Properties;

import static ru.itmo.ivt.apitestgenplugin.configGen.util.AuthUtil.setAuthProperty;
import static ru.itmo.ivt.apitestgenplugin.util.FileUtils.createFile;
import static ru.itmo.ivt.apitestgenplugin.util.FileUtils.createNestedDirectories;

public class PropertiesConfigFileGenerator implements ConfigGenerator {
    private static final String DEFAULT_PROPERTIES_FILE_NAME = "application.properties";
    private static final String DEFAULT_RESOURCES_PACKAGE = "main/resources";

    @Override
    public void prepareConfigFiles(GenerationContext context, PsiDirectory srcDir) {
        Project project = context.getProject();
        UserInput userInput = context.getUserInput();
        PsiDirectory resourcesDir = createNestedDirectories(project, srcDir, DEFAULT_RESOURCES_PACKAGE);
        createUserConfigFile(project, resourcesDir, userInput.baseUrl(), userInput.authType());
    }

    private void createUserConfigFile(Project project,
                                      PsiDirectory directory,
                                      String baseUrl,
                                      AuthType authType) {
        try {
            FileTemplate template = FileTemplateManager
                    .getInstance(project)
                    .getInternalTemplate("PropertiesTemplate.properties");

            Properties properties = new Properties();
            if (baseUrl != null && !baseUrl.isEmpty()) {
                properties.setProperty("BASE_URL", baseUrl);
            }
            setAuthProperty(authType, properties);

            String fileContent = template.getText(properties);
            createFile(project, fileContent, directory, DEFAULT_PROPERTIES_FILE_NAME, PropertiesFileType.INSTANCE);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create config file", e);
        }
    }
}