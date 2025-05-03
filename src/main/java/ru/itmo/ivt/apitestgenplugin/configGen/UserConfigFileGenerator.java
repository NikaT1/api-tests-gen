package ru.itmo.ivt.apitestgenplugin.configGen;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.lang.properties.PropertiesFileType;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import ru.itmo.ivt.apitestgenplugin.model.enums.AuthType;

import java.util.*;

public class UserConfigFileGenerator {
    private static final String DEFAULT_PROPERTY_VALUE = "true";
    private static final String DEFAULT_PROPERTIES_FILE_NAME = "application.properties";
    private static final String DEFAULT_PROPERTY_MANAGER_NAME = "TestPropertiesManager.java";
    private static final String DEFAULT_REST_ASSURED_CONFIG_NAME = "RestAssuredConfiguration.java";

    public static void createUserConfigFile(Project project,
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

    public static void createPropertiesManagerFile(Project project,
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

    public static void createRestAssuredConfigFile(Project project,
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

    private static void createFile(Project project,
                                   String fileContent,
                                   PsiDirectory directory,
                                   String fileName,
                                   FileType fileType) {
        WriteCommandAction.writeCommandAction(project)
                .compute(() -> {
                    PsiFileFactory factory = PsiFileFactory.getInstance(project);
                    PsiFile psiFile = factory.createFileFromText(
                            fileName,
                            fileType,
                            fileContent
                    );
                    PsiFile prevFile = directory.findFile(fileName);
                    if (prevFile != null) {
                        return prevFile;
                    }
                    return (PsiFile) directory.add(psiFile);
                });
    }

    private static void setAuthProperty(AuthType authType, Properties properties) {
        switch (authType) {
            case NO -> properties.setProperty("NO_AUTH", DEFAULT_PROPERTY_VALUE);
            case TOKEN -> properties.setProperty("TOKEN_AUTH", DEFAULT_PROPERTY_VALUE);
            case BASIC -> properties.setProperty("BASIC_AUTH", DEFAULT_PROPERTY_VALUE);
        }
    }
}