package ru.itmo.ivt.apitestgenplugin.modelGen.file.impl;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import ru.itmo.ivt.apitestgenplugin.modelGen.file.PackagesManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.itmo.ivt.apitestgenplugin.util.FileUtils.*;

public class PackagesManagerImpl implements PackagesManager {
    @Override
    public Map<String, PsiFile> splitModelFilesByDirectories(Map<String, List<String>> modelsByControllers,
                                                             String directoryPath,
                                                             @NotNull Project project) {
        Map<String, PsiFile> modelFilesByPackages = new HashMap<>();

        PsiDirectory baseDir = createPsiDirectoryFromPath(project, directoryPath);
        if (baseDir == null) return modelFilesByPackages;

        String basePackage = getBasePackage(baseDir);

        WriteCommandAction.runWriteCommandAction(project, () -> {
            for (Map.Entry<String, List<String>> entry : modelsByControllers.entrySet()) {
                String controllerName = entry.getKey().replace("-", "_");
                List<String> modelNames = entry.getValue();

                PsiDirectory controllerDir = createSubDirectory(project, baseDir, controllerName);
                if (controllerDir == null) continue;

                for (String modelName : modelNames) {
                    String fileName = modelName + ".java";
                    PsiFile sourceFile = findPsiFile(baseDir, fileName);
                    if (sourceFile == null) continue;

                    moveAndUpdatePackage(project, sourceFile, controllerDir, basePackage, controllerName);
                    modelFilesByPackages.put(fileName, controllerDir.findFile(fileName));
                }
            }
        });

        for (PsiFile psiFile : modelFilesByPackages.values()) {
            fixImports(project, psiFile);
        }
        return modelFilesByPackages;
    }

    @Override
    public void clearModelFile(PsiFile psiFile) {
        WriteCommandAction.runWriteCommandAction(psiFile.getProject(), () -> {
            psiFile.delete();
        });
    }

    private PsiFile findPsiFile(PsiDirectory directory, String fileName) {
        PsiFile file = directory.findFile(fileName);
        if (file != null) return file;

        System.err.println("File not found: " + directory.getVirtualFile().getPath() + "/" + fileName);
        return null;
    }

    private void moveAndUpdatePackage(Project project,
                                      PsiFile sourceFile,
                                      PsiDirectory targetDir,
                                      String basePackage,
                                      String controllerName) {
        WriteCommandAction.writeCommandAction(project).compute(() -> {
            try {
                PsiFile movedFile = (PsiFile) sourceFile.copy();
                targetDir.add(movedFile);
                updatePackageInPsiFile(movedFile, basePackage, controllerName);
                sourceFile.delete();
                return movedFile;
            } catch (Exception e) {
                Logger.getInstance(getClass()).error("Error in move operation", e);
                return null;
            }
        });
    }

    private void updatePackageInPsiFile(PsiFile psiFile, String basePackage, String controllerName) {
        if (!(psiFile instanceof PsiJavaFile javaFile)) return;

        String newPackage = basePackage.isEmpty() ? controllerName : basePackage + "." + controllerName;

        WriteCommandAction.runWriteCommandAction(psiFile.getProject(), () -> {
            javaFile.setPackageName(newPackage);
        });
    }

    private String getBasePackage(PsiDirectory directory) {
        PsiFile[] files = directory.getFiles();
        for (PsiFile file : files) {
            if (file instanceof PsiJavaFile) {
                String packageName = ((PsiJavaFile) file).getPackageName();
                if (!packageName.isEmpty()) {
                    return packageName;
                }
            }
        }
        return "";
    }
}