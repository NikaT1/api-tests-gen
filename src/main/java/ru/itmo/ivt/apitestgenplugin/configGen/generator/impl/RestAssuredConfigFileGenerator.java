package ru.itmo.ivt.apitestgenplugin.configGen.generator.impl;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import ru.itmo.ivt.apitestgenplugin.configGen.generator.ConfigGenerator;
import ru.itmo.ivt.apitestgenplugin.model.GenerationContext;
import ru.itmo.ivt.apitestgenplugin.model.UserInput;
import ru.itmo.ivt.apitestgenplugin.model.enums.AuthType;

import java.util.*;

import static ru.itmo.ivt.apitestgenplugin.configGen.util.AuthUtil.setAuthProperty;
import static ru.itmo.ivt.apitestgenplugin.util.FileUtils.*;

public class RestAssuredConfigFileGenerator implements ConfigGenerator {
    private static final String DEFAULT_REST_ASSURED_CONFIG_NAME = "RestAssuredConfiguration.java";
    private static final String DEFAULT_CONFIG_PACKAGE = "main/java/config";
    private static final String DEFAULT_CONFIG_PACKAGE_NAME = "config";

    @Override
    public void prepareConfigFiles(GenerationContext context, PsiDirectory srcDir) {
        Project project = context.getProject();
        UserInput userInput = context.getUserInput();
        PsiDirectory configDir = createNestedDirectories(project, srcDir, DEFAULT_CONFIG_PACKAGE);

        createRestAssuredConfigFile(project, configDir, DEFAULT_CONFIG_PACKAGE_NAME, userInput.authType());
    }

    private void createRestAssuredConfigFile(Project project,
                                             PsiDirectory directory,
                                             String packageName,
                                             AuthType authType) {
        try {
            FileTemplate template = FileTemplateManager
                    .getInstance(project)
                    .getInternalTemplate("RestAssuredConfigTemplate.java");

            Properties properties = new Properties();
            properties.setProperty("PACKAGE_NAME", packageName != null ? packageName : "");
            setAuthProperty(authType, properties);

            String fileContent = template.getText(properties);
            createFile(project, fileContent, directory, DEFAULT_REST_ASSURED_CONFIG_NAME, JavaFileType.INSTANCE);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create config file", e);
        }
    }
}