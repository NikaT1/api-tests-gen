package ru.itmo.ivt.apitestgenplugin.dataGen;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;

import java.util.Properties;
import java.util.stream.Collectors;

import static ru.itmo.ivt.apitestgenplugin.util.StringUtils.mapToString;

public class DataGenFileGenerator {
    public static void createModelGeneratorFile(Project project,
                                            PsiDirectory directory,
                                            String packageName,
                                            DataGenMethodManager manager) {
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
}